package com.nice.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import androidx.startup.Initializer

internal lateinit var applicationContext: Context
    private set

object Kable {

    val isBluetoothSupported: Boolean
        get() = applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)

    val isBluetoothOpened: Boolean
        get() = BluetoothAdapter.getDefaultAdapter()?.isEnabled ?: false

}

class KableInitializer : Initializer<Kable> {

    override fun create(context: Context): Kable {
        applicationContext = context.applicationContext
        return Kable
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

}
