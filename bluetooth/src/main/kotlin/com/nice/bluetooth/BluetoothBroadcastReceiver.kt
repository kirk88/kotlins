package com.nice.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

private val bluetoothStateIntentFilter: IntentFilter =
    IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)

internal class BluetoothStateReceiver(private val action: (state: Int) -> Unit) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
        action.invoke(state)
    }

    fun register() {
        applicationContext.registerReceiver(this, bluetoothStateIntentFilter)
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


internal class BluetoothScannerReceiver(): BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

    }
}