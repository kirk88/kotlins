package com.nice.bluetooth

import android.annotation.TargetApi
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import com.nice.bluetooth.common.Advertisement
import com.nice.bluetooth.common.AndroidScanResult
import com.nice.bluetooth.common.Scanner
import com.nice.bluetooth.common.toAndroidScanResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.*

class ScanFailedException internal constructor(
    val errorCode: Int
) : IllegalStateException("Bluetooth scan failed with error code $errorCode")

fun Scanner(services: List<UUID>? = null): Scanner =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        AndroidScannerV21(services)
    } else {
        AndroidScanner(services)
    }

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class AndroidScannerV21 internal constructor(private val filterServices: List<UUID>?) : Scanner {

    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        ?: error("Bluetooth not supported")


    @OptIn(ExperimentalCoroutinesApi::class)
    override val advertisements: Flow<Advertisement> = callbackFlow {
        check(bluetoothAdapter.isEnabled) { "Bluetooth is disabled" }

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                trySendBlocking(AndroidAdvertisement(result.toAndroidScanResult()))
                    .onFailure {
                        Log.w(
                            TAG,
                            "Unable to deliver scan result due to failure in flow or premature closing."
                        )
                    }
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>) {
                runCatching {
                    results.forEach {
                        trySendBlocking(AndroidAdvertisement(it.toAndroidScanResult())).getOrThrow()
                    }
                }.onFailure {
                    Log.w(
                        TAG,
                        "Unable to deliver batch scan results due to failure in flow or premature closing."
                    )
                }
            }

            override fun onScanFailed(errorCode: Int) {
                cancel("Bluetooth scan failed", ScanFailedException(errorCode))
            }
        }

        val scanFilter = filterServices
            ?.map { ScanFilter.Builder().setServiceUuid(ParcelUuid(it)).build() }
            ?.toList()
        bluetoothAdapter.bluetoothLeScanner.startScan(
            scanFilter,
            ScanSettings.Builder().build(),
            callback
        )

        awaitClose {
            bluetoothAdapter.bluetoothLeScanner.stopScan(callback)
        }
    }
}

class AndroidScanner internal constructor(private val filterServices: List<UUID>?) : Scanner {

    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        ?: error("Bluetooth not supported")

    @OptIn(ExperimentalCoroutinesApi::class)
    override val advertisements: Flow<Advertisement> = callbackFlow {
        check(bluetoothAdapter.isEnabled) { "Bluetooth is disabled" }

        val callback = BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
            trySendBlocking(AndroidAdvertisement(AndroidScanResult(device, rssi, scanRecord)))
                .onFailure {
                    Log.w(
                        TAG,
                        "Unable to deliver scan result due to failure in flow or premature closing."
                    )
                }
        }

        if (filterServices == null) {
            bluetoothAdapter.startLeScan(callback)
        } else {
            bluetoothAdapter.startLeScan(filterServices.toTypedArray(), callback)
        }

        awaitClose {
            bluetoothAdapter.stopLeScan(callback)
        }
    }
}

