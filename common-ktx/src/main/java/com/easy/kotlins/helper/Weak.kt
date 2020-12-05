package com.easy.kotlins.helper

import java.lang.ref.WeakReference
import kotlin.reflect.KProperty

fun <T> weak(initializer: () -> T) = Weak(initializer)

class Weak<T>(initializer: () -> T) {
    private var reference = WeakReference(initializer())

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return reference.get()
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        reference = WeakReference(value)
    }
}