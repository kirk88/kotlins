@file:Suppress("unused")

package com.nice.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import androidx.startup.Initializer
import com.nice.bluetooth.common.BluetoothState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal lateinit var applicationContext: Context
    private set

internal val defaultBluetoothAdapter: BluetoothAdapter? by lazy {
    (applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
}

object Bluetooth {

    private val receiver = registerBluetoothStateReceiver {
        bluetoothState.value = it.toBluetoothState()
    }

    private val bluetoothState = MutableStateFlow(
        if (isEnabled) BluetoothState.Opened else BluetoothState.Closed
    )
    val state: Flow<BluetoothState> = bluetoothState.asStateFlow()

    val isSupported: Boolean
        get() = defaultBluetoothAdapter != null

    var isEnabled: Boolean
        get() = defaultBluetoothAdapter?.isEnabled ?: false
        set(value) {
            defaultBluetoothAdapter?.apply {
                if (value) enable() else disable()
            }
        }

    val permissions: Array<String> by lazy {
        mutableListOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }.toTypedArray()
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
    else -> error("Unknown bluetooth state: $this")
}
