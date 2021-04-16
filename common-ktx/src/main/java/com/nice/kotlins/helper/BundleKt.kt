@file:Suppress("unused")

package com.nice.kotlins.helper

import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import java.io.Serializable

fun Array<out Pair<String, Any?>>.toBundle(): Bundle = run {
    return@run bundleOf(*this)
}

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

fun <T : Stable> Bundle.getStable(key: String): T? {
    return StableManager.getFrom(this, key)
}

fun <T : Stable> Bundle.getStable(key: String, defaultValue: () -> T): T {
    return getStable(key) ?: defaultValue()
}

fun <T : Stable> Intent.getStable(key: String): T? {
    return StableManager.getFrom(this, key)
}

fun <T : Stable> Intent.getStable(key: String, defaultValue: () -> T): T {
    return getStable(key) ?: defaultValue()
}

interface Stable : Serializable {

    val name: String

}

internal class StableImpl : Stable {
    override val name: String = System.nanoTime().toString()
}

private object StableManager {

    private val DATA_MAP: MutableMap<String, Stable> by lazy { mutableMapOf() }

    fun addTo(intent: Intent, key: String, stable: Stable) {
        DATA_MAP[stable.name] = stable
        intent.putExtra(key, stable)
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