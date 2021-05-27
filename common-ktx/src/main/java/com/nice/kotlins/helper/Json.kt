@file:Suppress("unused")

package com.nice.kotlins.helper

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import java.io.Reader
import java.lang.reflect.Type
import java.math.BigDecimal
import java.math.BigInteger

object GsonProvider {

    @Volatile
    private var globalGson = Gson()
    val gson: Gson
        get() = globalGson

    fun setGson(gson: Gson) {
        globalGson = gson
    }

}

fun <T> String.parseAsJson(type: Type): T {
    return GsonProvider.gson.fromJson(this, type)
}

fun <T> String.parseAsJson(clazz: Class<T>): T {
    return GsonProvider.gson.fromJson(this, clazz)
}

inline fun <reified T> String.parseAsJson(): T {
    return GsonProvider.gson.fromJson(this, object : TypeToken<T>() {}.type)
}

inline fun <reified T> Reader.parseAsJson(): T {
    return GsonProvider.gson.fromJson(this, object : TypeToken<T>() {}.type)
}

inline fun <reified T> JsonReader.parse(): T {
    return GsonProvider.gson.fromJson(this, object : TypeToken<T>() {}.type)
}

inline fun <reified T> JsonElement.parse(): T {
    return GsonProvider.gson.fromJson(this, object : TypeToken<T>() {}.type)
}

fun Any.toJsonElement(): JsonElement = JsonParser.parseString(toJson())

fun Any.toJsonObject(): JsonObject = toJsonElement().asJsonObject

fun Any.toJsonArray(): JsonArray = toJsonElement().asJsonArray

fun Any?.toJsonOrNull(): String? = this?.runCatching { toJson() }?.getOrNull()

fun Any.toJson(): String = if (this is String) this else GsonProvider.gson.toJson(this)

fun JsonObject.getOrNull(name: String): JsonElement? =
    this[name]?.let { if (it is JsonNull) null else it }

fun JsonArray.getOrNull(index: Int): JsonElement? =
    this[index]?.let { if (it is JsonNull) null else it }

fun JsonObject.getString(name: String, defaultValue: String = ""): String {
    val element = getOrNull(name) ?: return defaultValue
    return when (element) {
        is JsonPrimitive -> element.asString
        else -> element.toString()
    }
}

fun JsonObject.getBoolean(name: String, defaultValue: Boolean = false): Boolean {
    val element = getOrNull(name)
    if (element is JsonPrimitive) {
        if (element.isBoolean) {
            return element.asBoolean
        }

        if (element.isString) {
            return element.asString.let {
                if (it.equals("true", true)
                    || it.equals("false", true)
                ) {
                    it.toBoolean()
                } else defaultValue
            }
        }
    }
    return defaultValue
}

fun JsonObject.getNumber(name: String, defaultValue: Number = 0): Number =
    getOrNull(name)?.let {
        if (it is JsonPrimitive && it.isNumber) it.asNumber else defaultValue
    } ?: defaultValue

fun JsonObject.getDouble(name: String, defaultValue: Double = 0.toDouble()): Double =
    getOrNull(name)?.let {
        if (it is JsonPrimitive && it.isNumber) it.asDouble else defaultValue
    } ?: defaultValue

fun JsonObject.getFloat(name: String, defaultValue: Float = 0.toFloat()): Float =
    getOrNull(name)?.let {
        if (it is JsonPrimitive && it.isNumber) it.asFloat else defaultValue
    } ?: defaultValue

fun JsonObject.getLong(name: String, defaultValue: Long = 0.toLong()): Long =
    getOrNull(name)?.let {
        if (it is JsonPrimitive && it.isNumber) it.asLong else defaultValue
    } ?: defaultValue

fun JsonObject.getInt(name: String, defaultValue: Int = 0): Int =
    getOrNull(name)?.let {
        if (it is JsonPrimitive && it.isNumber) it.asInt else defaultValue
    } ?: defaultValue

fun JsonObject.getShort(name: String, defaultValue: Short = 0.toShort()): Short =
    getOrNull(name)?.let {
        if (it is JsonPrimitive && it.isNumber) it.asShort else defaultValue
    } ?: defaultValue

fun JsonObject.getByte(name: String, defaultValue: Byte = 0.toByte()): Byte =
    getOrNull(name)?.let {
        if (it is JsonPrimitive && it.isNumber) it.asByte else defaultValue
    } ?: defaultValue

fun JsonObject.getBigDecimal(
    name: String,
    defaultValue: BigDecimal = 0.toBigDecimal(),
): BigDecimal =
    getOrNull(name)?.let {
        if (it is JsonPrimitive && it.isNumber) it.asBigDecimal else defaultValue
    } ?: defaultValue

fun JsonObject.getBigInteger(
    name: String,
    defaultValue: BigInteger = 0.toBigInteger(),
): BigInteger =
    getOrNull(name)?.let {
        if (it is JsonPrimitive && it.isNumber) it.asBigInteger else defaultValue
    } ?: defaultValue

fun JsonArray.isEmpty() = size() == 0

fun JsonArray.isNotEmpty() = size() != 0

val JsonArray.size: Int get() = size()

fun JsonObject.isEmpty() = size() == 0

fun JsonObject.isNotEmpty() = size() != 0

val JsonObject.size: Int get() = size()

operator fun JsonObject.iterator(): Iterator<MutableMap.MutableEntry<String, JsonElement>> =
    entrySet().iterator()

inline fun JsonObject.forEach(block: (name: String, value: JsonElement) -> Unit) {
    for ((name, element) in entrySet()) {
        block(name, element)
    }
}

inline fun JsonObject.forEachIndexed(block: (index: Int, key: String, value: JsonElement) -> Unit) {
    for ((index, entry) in entrySet().withIndex()) {
        block(index, entry.key, entry.value)
    }
}