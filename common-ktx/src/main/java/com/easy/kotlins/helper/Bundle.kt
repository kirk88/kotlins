package com.easy.kotlins.helper

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable

/**
 * Create by LiZhanPing on 2020/9/23
 */
@Suppress("UNCHECKED_CAST")
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
            is Parcelable -> bundle.putParcelable(key, value)
            is Serializable -> bundle.putSerializable(key, value)
            is LargeExtra -> bundle.putLargeExtra(key, value)
        }
    }
    return@run bundle
}

fun Bundle.toMap(): Map<String, Any?> = run {
    val map = mutableMapOf<String, Any?>()
    for (key in this.keySet()) {
        map[key] = this.get(key)
    }
    map
}

fun Bundle.putLargeExtra(key: String, value: LargeExtra) {
    putString(key, LargeExtras.put(value))
}

fun <T> Bundle.getLargeExtra(key: String): LargeExtra? {
    return getString(key)?.let { name -> LargeExtras.get(name) }
}

fun <T> Bundle.getLargeExtraValue(key: String): T? {
    @Suppress("UNCHECKED_CAST")
    return getString(key)?.let { name -> LargeExtras.get(name) as T? }
}

fun <T> Bundle.getLargeExtraValue(key: String, defaultValue: T): T {
    return getLargeExtraValue<T>(key) ?: defaultValue
}

fun <T> Bundle.getLargeExtraValue(key: String, defaultValue: () -> T): T {
    return getLargeExtraValue<T>(key) ?: defaultValue()
}

fun Intent.putLargeExtra(key: String, value: LargeExtra) {
    putExtra(key, LargeExtras.put(value))
}

fun <T> Intent.getLargeExtra(key: String): LargeExtra? {
    return getStringExtra(key)?.let { name -> LargeExtras.get(name) }
}

fun <T> Intent.getLargeExtraValue(key: String): T? {
    @Suppress("UNCHECKED_CAST")
    return getStringExtra(key)?.let { name -> LargeExtras.get(name) as T? }
}

fun <T> Intent.getLargeExtraValue(key: String, defaultValue: T): T {
    return getLargeExtraValue<T>(key) ?: defaultValue
}

fun <T> Intent.getLargeExtraValue(key: String, defaultValue: () -> T): T {
    return getLargeExtraValue<T>(key) ?: defaultValue()
}

fun LargeExtra(value: Any?): LargeExtra = LargeExtraImpl(value)

interface LargeExtra {
    val name: String

    val value: Any?
}

private class LargeExtraImpl(override val value: Any?) : LargeExtra {

    override val name: String = System.nanoTime().toString()

}

private object LargeExtras {

    private val dataMap: MutableMap<String, LargeExtra> by lazy { mutableMapOf() }

    fun put(extra: LargeExtra): String {
        return extra.name.also {
            dataMap[it] = extra
        }
    }

    fun get(name: String): LargeExtra? {
        return dataMap.remove(name)
    }

}

