package com.nice.bluetooth

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresApi
import com.nice.bluetooth.common.Advertisement
import com.nice.bluetooth.common.BluetoothScanResult
import com.nice.bluetooth.common.Scanner
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.*

@SuppressLint("NewApi")
fun Scanner(level: ScannerLevel = ScannerLevel.Low, services: List<UUID>? = null): Scanner =
    when (level) {
        ScannerLevel.System -> AndroidSystemScanner(services)
        ScannerLevel.High -> HighVersionBleScanner(services)
        ScannerLevel.Low -> LowVersionBleScanner(services)
    }

enum class ScannerLevel {
    System,

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    High,
    Low
}

class ScanFailedException internal constructor(
    errorCode: Int
) : IllegalStateException("Bluetooth scan failed with error code $errorCode")

internal class AndroidSystemScanner(private val filterServices: List<UUID>?) : Scanner {

    private val bluetoothAdapter = defaultBluetoothAdapter
        ?: error("Bluetooth not supported")

    @OptIn(ExperimentalCoroutinesApi::class)
    override val advertisements: Flow<Advertisement> = callbackFlow {
        check(bluetoothAdapter.isEnabled) { "Bluetooth is disabled" }

        val receiver = registerBluetoothScannerReceiver(
            filterServices,
            onScanResult = { result ->
                trySendBlocking(AndroidAdvertisement(result))
                    .onFailure {
                        Log.w(
                            TAG,
                            "Unable to deliver scan result due to failure in flow or premature closing."
                        )
                    }
            },
            onScanFinished = {
                cancel()
            }
        )

        bluetoothAdapter.startDiscovery()

        awaitClose {
            receiver.unregister()
            bluetoothAdapter.cancelDiscovery()
        }
    }

}

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
internal class HighVersionBleScanner(private val filterServices: List<UUID>?) : Scanner {

    private val bluetoothAdapter = defaultBluetoothAdapter
        ?: error("Bluetooth not supported")

    @OptIn(ExperimentalCoroutinesApi::class)
    override val advertisements: Flow<Advertisement> = callbackFlow {
        check(bluetoothAdapter.isEnabled) { "Bluetooth is disabled" }

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                trySendBlocking(AndroidAdvertisement(BluetoothScanResult(result)))
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
                        trySendBlocking(AndroidAdvertisement(BluetoothScanResult(it))).getOrThrow()
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
            bluetoothAdapter.bluetoothLeScanner?.stopScan(callback)
        }
    }
}

internal class LowVersionBleScanner internal constructor(private val filterServices: List<UUID>?) :
    Scanner {

    private val bluetoothAdapter = defaultBluetoothAdapter
        ?: error("Bluetooth not supported")

    @OptIn(ExperimentalCoroutinesApi::class)
    override val advertisements: Flow<Advertisement> = callbackFlow {
        check(bluetoothAdapter.isEnabled) { "Bluetooth is disabled" }

        val callback = BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
            trySendBlocking(AndroidAdvertisement(BluetoothScanResult(device, rssi, scanRecord)))
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