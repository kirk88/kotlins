@file:Suppress("unused")

package com.nice.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import androidx.startup.Initializer
import com.nice.bluetooth.common.BluetoothState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal lateinit var applicationContext: Context
    private set

object Bluetooth {

    private val receiver = registerBluetoothStateBroadcastReceiver(applicationContext) {
        bluetoothState.value = it.toBluetoothState()
    }

    private val bluetoothState = MutableStateFlow(
        if (isEnabled) BluetoothState.Opened else BluetoothState.Closed
    )
    val state: Flow<BluetoothState> = bluetoothState.asStateFlow()

    val isSupported: Boolean
        get() = BluetoothAdapter.getDefaultAdapter() != null

    var isEnabled: Boolean
        get() = BluetoothAdapter.getDefaultAdapter()?.isEnabled ?: false
        set(value) {
            BluetoothAdapter.getDefaultAdapter()?.apply {
                if (value) enable() else disable()
            }
        }

    val permissions = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

}

class BluetoothInitializer : Initializer<Bluetooth> {

    override fun create(context: Context): Bluetooth {
        applicationContext = context.applicationContext
        return Bluetooth
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

}

private fun Int.toBluetoothState(): BluetoothState = when (this) {
    BluetoothAdapter.STATE_ON -> BluetoothState.Opened
    BluetoothAdapter.STATE_TURNING_ON -> BluetoothState.Opening
    BluetoothAdapter.STATE_OFF -> BluetoothState.Closed
    BluetoothAdapter.STATE_TURNING_OFF -> BluetoothState.Closing
    else -> throw IllegalArgumentException("Unknown state $this")
}