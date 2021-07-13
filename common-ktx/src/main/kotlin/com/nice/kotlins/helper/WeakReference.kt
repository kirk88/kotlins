package com.nice.kotlins.helper

import java.lang.ref.WeakReference
import kotlin.reflect.KProperty

fun <T> weak(initializer: () -> T? = { null }): Weak<T> = WeakImpl(initializer)

interface Weak<T> {

    val value: T?

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? = value

}

internal class WeakImpl<T>(initializer: () -> T?) : Weak<T> {

    private var initializer: (() -> T?)? = initializer
    private var reference: WeakReference<T>? = null

    override val value: T?
        get() {
            if (reference == null) {
                reference = WeakReference(initializer!!())
                initializer = null
            }
            return reference!!.get()
        }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        reference = WeakReference(value)
    }

}