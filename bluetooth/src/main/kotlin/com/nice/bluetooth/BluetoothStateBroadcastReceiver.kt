package com.nice.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

private val bluetoothStateIntentFilter: IntentFilter =
    IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)

internal class BluetoothStateBroadcastReceiver(private val action: (state: Int) -> Unit) : BroadcastReceiver() {

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


internal inline fun registerBluetoothStateBroadcastReceiver(
    crossinline action: (state: Int) -> Unit
): BluetoothStateBroadcastReceiver = BluetoothStateBroadcastReceiver {
    action.invoke(it)
}.also { it.register() }