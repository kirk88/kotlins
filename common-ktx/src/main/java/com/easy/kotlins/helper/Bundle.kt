@file:Suppress("unused")

package com.easy.kotlins.helper

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
            is Reachable -> bundle.putReachable(key, value)
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
    for (key in this.keySet()) {
        map[key] = this.get(key)
    }
    map
}

fun Bundle.putReachable(key: String, value: Reachable) {
    ReachableManager.addTo(this, key, value)
}

fun <T : Reachable> Bundle.getReachable(key: String): T? {
    return ReachableManager.getFrom(this, key)
}

fun <T : Reachable> Bundle.getReachable(key: String, defaultValue: () -> T): T {
    return getReachable(key) ?: defaultValue()
}

fun Intent.putReachable(key: String, value: Reachable) {
    ReachableManager.addTo(this, key, value)
}

fun <T : Reachable> Intent.getReachable(key: String): T? {
    return ReachableManager.getFrom(this, key)
}

fun <T : Reachable> Intent.getReachable(key: String, defaultValue: () -> T): T {
    return getReachable(key) ?: defaultValue()
}


interface Reachable


private object ReachableManager {

    private val dataMap: MutableMap<String, Reachable> by lazy { mutableMapOf() }

    fun addTo(intent: Intent, key: String, reachable: Reachable) {
        val name = System.nanoTime().toString()
        dataMap[name] = reachable
        intent.putExtra(key, name)
    }

    fun addTo(bundle: Bundle, key: String, reachable: Reachable) {
        val name = System.nanoTime().toString()
        dataMap[name] = reachable
        bundle.putString(key, name)
    }

    fun <T : Reachable> getFrom(intent: Intent, key: String): T? {
        val name = intent.getStringExtra(key) ?: return null
        @Suppress("UNCHECKED_CAST")
        return dataMap.remove(name) as T?
    }

    fun <T : Reachable> getFrom(bundle: Bundle, key: String): T? {
        val name = bundle.getString(key) ?: return null
        @Suppress("UNCHECKED_CAST")
        return dataMap.remove(name) as T?
    }

}