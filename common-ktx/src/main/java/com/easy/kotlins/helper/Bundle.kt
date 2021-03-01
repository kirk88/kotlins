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
    LargeExtras.add(this, key, value)
}

fun Bundle.getLargeExtra(key: String): LargeExtra? {
    return LargeExtras.get(this, key)
}

fun <T> Bundle.getLargeExtraValue(key: String): T? {
    @Suppress("UNCHECKED_CAST")
    return getLargeExtra(key)?.value as T?
}

fun <T> Bundle.getLargeExtraValue(key: String, defaultValue: T): T {
    return getLargeExtraValue<T>(key) ?: defaultValue
}

fun <T> Bundle.getLargeExtraValue(key: String, defaultValue: () -> T): T {
    return getLargeExtraValue<T>(key) ?: defaultValue()
}

fun Intent.putLargeExtra(key: String, value: LargeExtra) {
    LargeExtras.add(this, key, value)
}

fun Intent.getLargeExtra(key: String): LargeExtra? {
    return LargeExtras.get(this, key)
}

fun <T> Intent.getLargeExtraValue(key: String): T? {
    @Suppress("UNCHECKED_CAST")
    return getLargeExtra(key)?.value as T?
}

fun <T> Intent.getLargeExtraValue(key: String, defaultValue: T): T {
    return getLargeExtraValue<T>(key) ?: defaultValue
}

fun <T> Intent.getLargeExtraValue(key: String, defaultValue: () -> T): T {
    return getLargeExtraValue<T>(key) ?: defaultValue()
}

fun largeExtraOf(value: Any?): LargeExtra = LargeExtraImpl(value)

interface LargeExtra {
    val name: String

    val value: Any?
}

private class LargeExtraImpl(override val value: Any?) : LargeExtra {

    override val name: String = System.nanoTime().toString()

}

private object LargeExtras {

    private val dataMap: MutableMap<String, LargeExtra> by lazy { mutableMapOf() }

    fun add(intent: Intent, key: String, extra: LargeExtra) {
        val name = extra.name
        dataMap[name] = extra
        intent.putExtra(key, name)
    }

    fun add(bundle: Bundle, key: String, extra: LargeExtra) {
        val name = extra.name
        dataMap[name] = extra
        bundle.putString(key, name)
    }

    fun get(intent: Intent, key: String): LargeExtra? {
        val name = intent.getStringExtra(key) ?: return null
        return dataMap.remove(name)
    }

    fun get(bundle: Bundle, key: String): LargeExtra? {
        val name = bundle.getString(key) ?: return null
        return dataMap.remove(name)
    }

}

