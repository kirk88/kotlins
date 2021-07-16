@file:Suppress("unused")

package com.nice.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.os.Process
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
        if (isOpened) BluetoothState.Opened else BluetoothState.Closed
    )
    val state: Flow<BluetoothState> = bluetoothState.asStateFlow()

    val isSupported: Boolean
        get() = BluetoothAdapter.getDefaultAdapter() != null

    val isOpened: Boolean
        get() = BluetoothAdapter.getDefaultAdapter()?.isEnabled ?: false

    val permissions = arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION)

    val isPermissionsGranted: Boolean
        get() = permissions.all {
            applicationContext.checkPermission(it, Process.myPid(), Process.myUid()) == PackageManager.PERMISSION_GRANTED
        }

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