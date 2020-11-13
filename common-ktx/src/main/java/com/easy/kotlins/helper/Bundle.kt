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
            is ArrayList<*> -> when (value.firstOrNull()) {
                is String -> bundle.putStringArrayList(key, value as ArrayList<String>)
                is CharSequence -> bundle.putCharSequenceArrayList(key, value as ArrayList<CharSequence>)
                is Parcelable -> bundle.putParcelableArrayList(key, value as ArrayList<out Parcelable>)
            }
            is LargeData -> bundle.putLargeData(key, value)
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

fun Bundle.putLargeData(key: String, value: LargeData) {
    putString(key, LargeDataBag.set(value))
}

@Suppress("UNCHECKED_CAST")
fun <T> Bundle.getLargeDataValue(key: String): T? {
    return getString(key)?.let { name -> LargeDataBag.get(name)?.value as T? }
}

fun Bundle.getLargeData(key: String): LargeData? {
    return getString(key)?.let { name -> LargeDataBag.get(name) }
}

fun Intent.putLargeData(key: String, value: LargeData) {
    putExtra(key, LargeDataBag.set(value))
}

fun Intent.getLargeData(key: String): LargeData? {
    return getStringExtra(key)?.let { name -> LargeDataBag.get(name) }
}

@Suppress("UNCHECKED_CAST")
fun <T> Intent.getLargeDataValue(key: String): T? {
    return getStringExtra(key)?.let { name -> LargeDataBag.get(name)?.value as T? }
}

fun largeDataOf(name: String, value: Any?) = LargeData(name, value)

fun largeDataOf(value: Any?) = LargeData(System.currentTimeMillis().toString(), value)

data class LargeData(val name: String, val value: Any?)

object LargeDataBag {

    private val dataMap: MutableMap<String, LargeData?> by lazy { mutableMapOf() }

    fun set(largeData: LargeData): String {
        dataMap[largeData.name] = largeData
        return largeData.name
    }

    fun get(name: String): LargeData? {
        return dataMap[name]
    }

}

