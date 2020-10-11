package com.easy.kotlins.helper

import java.lang.ref.WeakReference
import kotlin.reflect.KProperty

fun <T> weak(initializer: () -> T) = Weak(initializer)

class Weak<T>(initializer: () -> T?) {
    private var weakReference = WeakReference<T?>(initializer())

    constructor() : this({ null })

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return weakReference.get()
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        weakReference = WeakReference(value)
    }

}