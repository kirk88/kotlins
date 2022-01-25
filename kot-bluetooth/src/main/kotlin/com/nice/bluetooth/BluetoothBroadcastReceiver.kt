package com.nice.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.nice.bluetooth.common.BluetoothScanResult
import java.util.*

private val BLUETOOTH_STATE_INTENT_FILTER: IntentFilter =
    IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)

internal class BluetoothStateReceiver(private val action: (state: Int) -> Unit) :
    BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)
        action.invoke(state)
    }

    fun register() {
        applicationContext.registerReceiver(this, BLUETOOTH_STATE_INTENT_FILTER)
    }

    fun unregister() {
        applicationContext.unregisterReceiver(this)
    }

}


internal inline fun registerBluetoothStateReceiver(
    crossinline action: (state: Int) -> Unit
): BluetoothStateReceiver = BluetoothStateReceiver {
    action.invoke(it)
}.also { it.register() }


private val BLUETOOTH_SCANNER_INTENT_FILTER: IntentFilter =
    IntentFilter().apply {
        addAction(BluetoothDevice.ACTION_FOUND)
        addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
    }

internal class BluetoothScannerReceiver(
    private val onScanResult: (device: BluetoothDevice, rssi: Int) -> Unit,
    private val onScanFinished: () -> Unit
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            BluetoothDevice.ACTION_FOUND -> {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        ?: return
                val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)
                onScanResult.invoke(device, rssi.toInt())
            }

            BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                onScanFinished.invoke()
            }
        }
    }

    fun register() {
        applicationContext.registerReceiver(this, BLUETOOTH_SCANNER_INTENT_FILTER)
    }

    fun unregister() {
        applicationContext.unregisterReceiver(this)
    }

}

internal inline fun registerBluetoothScannerReceiver(
    serviceUuids: List<UUID>?,
    crossinline onScanResult: (result: BluetoothScanResult) -> Unit,
    crossinline onScanFinished: () -> Unit
): BluetoothScannerReceiver = BluetoothScannerReceiver(
    onScanResult = { device, rssi ->
        val result = BluetoothScanResult(device, rssi)
        if (serviceUuids == null) {
            onScanResult.invoke(result)
        } else {
            //TODO filter by service uuids
            if (result.scanRecord?.serviceUuids?.any { serviceUuids.contains(it) } == true) {
                onScanResult.invoke(result)
            }
        }
    },
    onScanFinished = {
        onScanFinished.invoke()
    }
).also { it.register() }