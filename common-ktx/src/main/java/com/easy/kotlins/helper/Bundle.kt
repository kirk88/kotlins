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
            is LargeExtra -> bundle.putLargeExtra(key, value)
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
            is ArrayList<*> -> when (value.firstOrNull()) {
                is String -> bundle.putStringArrayList(key, value as ArrayList<String>)
                is Int -> bundle.putIntegerArrayList(key, value as ArrayList<Int>)
                is CharSequence -> bundle.putCharSequenceArrayList(key, value as ArrayList<CharSequence>)
                is Parcelable -> bundle.putParcelableArrayList(key, value as ArrayList<out Parcelable>)
            }
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
    putString(key, LargeExtrasBag.set(value))
}

fun Bundle.putLargeExtra(key: String, value: Any?) {
    putString(key, LargeExtrasBag.set(LargeExtra(value)))
}

@Suppress("UNCHECKED_CAST")
fun <T> Bundle.getLargeExtra(key: String): T? {
    return getString(key)?.let { dataKey -> LargeExtrasBag.get(dataKey) as? T? }
}

fun <T> Bundle.getLargeExtra(key: String, defaultValue: T): T {
    return getLargeExtra<T>(key) ?: defaultValue
}

fun <T> Bundle.getLargeExtra(key: String, defaultValue: () -> T): T {
    return getLargeExtra<T>(key) ?: defaultValue()
}

fun Intent.putLargeExtra(key: String, value: LargeExtra) {
    putExtra(key, LargeExtrasBag.set(LargeExtra(value)))
}

fun Intent.putLargeExtra(key: String, value: Any?) {
    putExtra(key, LargeExtrasBag.set(LargeExtra(value)))
}

@Suppress("UNCHECKED_CAST")
fun <T> Intent.getLargeExtra(key: String): T? {
    return getStringExtra(key)?.let { dataKey -> LargeExtrasBag.get(dataKey) as? T? }
}

fun <T> Intent.getLargeExtra(key: String, defaultValue: T): T {
    return getLargeExtra<T>(key) ?: defaultValue
}

fun <T> Intent.getLargeExtra(key: String, defaultValue: () -> T): T {
    return getLargeExtra<T>(key) ?: defaultValue()
}

fun largeExtraOf(value: Any?): LargeExtra = LargeExtra(value)

class LargeExtra(val value: Any?) {
    val name: String by lazy { System.currentTimeMillis().toString() }
}

private object LargeExtrasBag {

    private val dataMap: MutableMap<String, Any?> by lazy { mutableMapOf() }

    fun set(extra: LargeExtra): String {
        return extra.name.also {
            dataMap[it] = extra.value
        }
    }

    fun get(key: String): Any? {
        return dataMap.remove(key)
    }

}

