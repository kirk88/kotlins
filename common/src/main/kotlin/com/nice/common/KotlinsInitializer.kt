@file:Suppress("unused")

package com.nice.common

import android.content.Context
import androidx.startup.Initializer
import com.nice.common.app.ScreenAdaptation
import com.nice.common.helper.application

object Kotlins

class KotlinsInitializer: Initializer<Kotlins> {
    override fun create(context: Context): Kotlins {
        val application = requireNotNull(context.application) {
            "Can not get application from context $context"
        }
        ApplicationContextHolder.init(application)
        ScreenAdaptation.init(application)
        return Kotlins
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

}

internal object ApplicationContextHolder {

    lateinit var applicationContext: Context
        private set

    fun init(context: Context) {
        applicationContext = context.applicationContext
    }

}

val applicationContext: Context
    get() = ApplicationContextHolder.applicationContext