package com.nice.bluetooth

import android.content.Context
import androidx.startup.Initializer

internal lateinit var applicationContext: Context
    private set

object Kable

class KableInitializer : Initializer<Kable> {

    override fun create(context: Context): Kable {
        applicationContext = context.applicationContext
        return Kable
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
