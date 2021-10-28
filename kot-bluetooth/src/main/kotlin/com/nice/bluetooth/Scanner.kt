@file:Suppress("MissingPermission", "BlockingMethodInNonBlockingContext", "UNUSED", "DEPRECATION")

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

fun Scanner(buildAction: ScannerBuilder.() -> Unit): Scanner {
    val builder = ScannerBuilder().apply(buildAction)
    return when (builder.type) {
        ScannerType.System -> AndroidSystemScanner(builder.filterServices)
        ScannerType.High -> HighVersionBleScanner(builder.filterServices, builder.settings)
        ScannerType.Low -> LowVersionBleScanner(builder.filterServices)
    }
}

fun Scanner(type: ScannerType): Scanner = Scanner {
    this.type = type
}

enum class ScannerType {
    System,
    High,
    Low
}

class ScannerBuilder {

    internal var filterServices: MutableList<UUID>? = null

    var type: ScannerType = ScannerType.Low

    var settings: ScanSettings = ScanSettings.Builder().build()

    fun addFilterService(uuid: UUID) {
        (filterServices ?: mutableListOf<UUID>().also { filterServices = it }).add(uuid)
    }

}

class ScanFailedException internal constructor(
    errorCode: Int
) : IllegalStateException("Bluetooth scan failed with error code $errorCode")

internal class AndroidSystemScanner(private val filterServices: List<UUID>?) : Scanner {

    private val bluetoothAdapter = Bluetooth.adapter

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
internal class HighVersionBleScanner(
    private val filterServices: List<UUID>?,
    private val settings: ScanSettings
) : Scanner {

    private val bluetoothAdapter = Bluetooth.adapter

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
                for (result in results) {
                    trySendBlocking(AndroidAdvertisement(BluetoothScanResult(result)))
                        .onFailure {
                            Log.w(
                                TAG,
                                "Unable to deliver batch scan results due to failure in flow or premature closing."
                            )
                        }
                        .getOrNull() ?: break
                }
            }

            override fun onScanFailed(errorCode: Int) {
                cancel("Bluetooth scan failed", ScanFailedException(errorCode))
            }
        }

        bluetoothAdapter.bluetoothLeScanner.startScan(
            filterServices
                ?.map { ScanFilter.Builder().setServiceUuid(ParcelUuid(it)).build() }
                ?.toList(),
            settings,
            callback
        )

        awaitClose {
            bluetoothAdapter.bluetoothLeScanner?.stopScan(callback)
        }
    }
}

internal class LowVersionBleScanner internal constructor(private val filterServices: List<UUID>?) :
    Scanner {

    private val bluetoothAdapter = Bluetooth.adapter

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