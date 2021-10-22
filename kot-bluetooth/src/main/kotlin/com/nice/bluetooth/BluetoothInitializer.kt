@file:Suppress("unused", "MissingPermission")

package com.nice.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.startup.Initializer
import com.nice.bluetooth.common.BluetoothState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BluetoothInitializer : Initializer<Bluetooth> {

    override fun create(context: Context): Bluetooth {
        applicationContext = context.applicationContext
        return Bluetooth
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

}

internal lateinit var applicationContext: Context
    private set

object Bluetooth {

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        (applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
    }

    internal val adapter: BluetoothAdapter
        get() = bluetoothAdapter ?: error("Bluetooth not supported")

    private val receiver = registerBluetoothStateReceiver {
        bluetoothState.value = it.toBluetoothState()
    }

    private val bluetoothState = MutableStateFlow(
        if (isEnabled) BluetoothState.Opened else BluetoothState.Closed
    )
    val state: Flow<BluetoothState> = bluetoothState.asStateFlow()

    val isSupported: Boolean
        get() = bluetoothAdapter != null

    val isLeSupported: Boolean
        get() = applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)

    var isEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled ?: false
        set(value) {
            if (value) bluetoothAdapter?.enable() else bluetoothAdapter?.disable()
        }

    val bondedDevices: Set<BluetoothDevice>?
        get() = bluetoothAdapter?.bondedDevices

    val permissions: Array<String> = mutableListOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_SCAN)
            add(Manifest.permission.BLUETOOTH_CONNECT)
        }
    }.toTypedArray()

}

private fun Int.toBluetoothState(): BluetoothState = when (this) {
    BluetoothAdapter.STATE_ON -> BluetoothState.Opened
    BluetoothAdapter.STATE_TURNING_ON -> BluetoothState.Opening
    BluetoothAdapter.STATE_OFF -> BluetoothState.Closed
    BluetoothAdapter.STATE_TURNING_OFF -> BluetoothState.Closing
    else -> error("Unknown bluetooth state: $this")
}
