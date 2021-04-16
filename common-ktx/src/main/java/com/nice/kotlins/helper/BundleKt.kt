@file:Suppress("unused")

package com.nice.kotlins.helper

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable

fun Array<out Pair<String, Any?>>.toBundle(): Bundle = run {
    val bundle = Bundle()
    for ((key, value) in this) {
        when (value) {
            is Boolean -> bundle.putBoolean(key, value)
            is Double -> bundle.putDouble(key, value)
            is Float -> bundle.putFloat(key, value)
            is Int -> bundle.putInt(key, value)
            is Long -> bundle.putLong(key, value)
            is Short -> bundle.putShort(key, value)
            is String -> bundle.putString(key, value)
            is CharSequence -> bundle.putCharSequence(key, value)
            is Stable -> bundle.putStable(key, value)
            is Parcelable -> bundle.putParcelable(key, value)
            is Serializable -> bundle.putSerializable(key, value)
        }
    }
    return@run bundle
}

fun Bundle(vararg values: Pair<String, Any?>): Bundle = Bundle(values.toBundle())

fun Bundle.putAll(vararg values: Pair<String, Any?>) {
    putAll(values.toBundle())
}

fun Bundle.toMap(): Map<String, Any?> = run {
    val map = mutableMapOf<String, Any?>()
    for (key in keySet()) {
        map[key] = get(key)
    }
    map
}

fun Bundle.toMap(destination: MutableMap<String, Any?>): Map<String, Any?> = run {
    for (key in keySet()) {
        destination[key] = get(key)
    }
    destination
}

fun Bundle.putStable(key: String, value: Stable) {
    StableManager.addTo(this, key, value)
}

fun <T : Stable> Bundle.getStable(key: String): T? {
    return StableManager.getFrom(this, key)
}

fun <T : Stable> Bundle.getStable(key: String, defaultValue: () -> T): T {
    return getStable(key) ?: defaultValue()
}

fun Intent.putStable(key: String, value: Stable) {
    StableManager.addTo(this, key, value)
}

fun <T : Stable> Intent.getStable(key: String): T? {
    return StableManager.getFrom(this, key)
}

fun <T : Stable> Intent.getStable(key: String, defaultValue: () -> T): T {
    return getStable(key) ?: defaultValue()
}

interface Stable

private object StableManager {

    private val DATA_MAP: MutableMap<String, Stable> by lazy { mutableMapOf() }

    fun addTo(intent: Intent, key: String, stable: Stable) {
        val name = System.nanoTime().toString()
        DATA_MAP[name] = stable
        intent.putExtra(key, name)
    }

    fun addTo(bundle: Bundle, key: String, stable: Stable) {
        val name = System.nanoTime().toString()
        DATA_MAP[name] = stable
        bundle.putString(key, name)
    }

    fun <T : Stable> getFrom(intent: Intent, key: String): T? {
        val name = intent.getStringExtra(key) ?: return null
        @Suppress("UNCHECKED_CAST")
        return DATA_MAP.remove(name) as T?
    }

    fun <T : Stable> getFrom(bundle: Bundle, key: String): T? {
        val name = bundle.getString(key) ?: return null
        @Suppress("UNCHECKED_CAST")
        return DATA_MAP.remove(name) as T?
    }

}