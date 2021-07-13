package com.nice.kotlins.helper

import android.os.Build
import android.os.Handler
import android.os.Looper
import java.lang.reflect.Constructor

fun Looper.asHandler(async: Boolean = false): Handler {
    if (!async || Build.VERSION.SDK_INT < 16) {
        return Handler(this)
    }

    if (Build.VERSION.SDK_INT >= 28) {
        val factoryMethod = Handler::class.java.getDeclaredMethod("createAsync", Looper::class.java)
        return factoryMethod.invoke(null, this) as Handler
    }

    val constructor: Constructor<Handler>
    try {
        constructor = Handler::class.java.getDeclaredConstructor(
            Looper::class.java,
            Handler.Callback::class.java, Boolean::class.javaPrimitiveType
        )
    } catch (ignored: NoSuchMethodException) {
        // Hidden constructor absent. Fall back to non-async constructor.
        return Handler(this)
    }
    return constructor.newInstance(this, null, true)
}
