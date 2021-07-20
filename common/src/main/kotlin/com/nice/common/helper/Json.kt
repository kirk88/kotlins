@file:Suppress("unused")

package com.nice.common.helper

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import java.io.Reader
import java.lang.reflect.Type
import java.math.BigDecimal
import java.math.BigInteger

object GlobalGson {

    @Volatile
    private var GSON = Gson()

    fun setGson(gson: Gson) {
        GSON = gson
    }

    fun toJson(src: Any): String = GSON.toJson(src)

    fun toJson(src: JsonElement): String = GSON.toJson(src)

    fun <T> fromJson(json: String, classOfT: Class<T>): T = GSON.fromJson(json, classOfT)

    fun <T> fromJson(json: JsonElement, classOfT: Class<T>): T = GSON.fromJson(json, classOfT)

    fun <T> fromJson(reader: JsonReader, classOfT: Class<T>): T = GSON.fromJson(reader, classOfT)

    fun <T> fromJson(reader: Reader, classOfT: Class<T>): T = GSON.fromJson(reader, classOfT)

    fun <T> fromJson(json: String, type: Type): T = GSON.fromJson(json, type)

    fun <T> fromJson(json: JsonElement, type: Type): T = GSON.fromJson(json, type)

    fun <T> fromJson(reader: JsonReader, type: Type): T = GSON.fromJson(reader, type)

    fun <T> fromJson(reader: Reader, type: Type): T = GSON.fromJson(reader, type)

}

fun <T> String.parseAsJson(type: Type): T {
    return GlobalGson.fromJson(this, type)
}

fun <T> String.parseAsJson(clazz: Class<T>): T {
    return GlobalGson.fromJson(this, clazz)
}

inline fun <reified T> String.parseAsJson(): T {
    return GlobalGson.fromJson(this, object : TypeToken<T>() {}.type)
}

inline fun <reified T> Reader.parseAsJson(): T {
    return GlobalGson.fromJson(this, object : TypeToken<T>() {}.type)
}

inline fun <reified T> JsonReader.parse(): T {
    return GlobalGson.fromJson(this, object : TypeToken<T>() {}.type)
}

inline fun <reified T> JsonElement.parse(): T {
    return GlobalGson.fromJson(this, object : TypeToken<T>() {}.type)
}

fun Any.toJsonElement(): JsonElement = JsonParser.parseString(toJson())

fun Any.toJsonObject(): JsonObject = toJsonElement().asJsonObject

fun Any.toJsonArray(): JsonArray = toJsonElement().asJsonArray

fun Any?.toJsonOrNull(): String? = this?.runCatching { toJson() }?.getOrNull()

fun Any.toJson(): String = if (this is String) this else GlobalGson.toJson(this)

fun JsonObject.getOrNull(name: String): JsonElement? = this[name]?.let { if (it is JsonNull) null else it }

fun JsonArray.getOrNull(index: Int): JsonElement? = this[index]?.let { if (it is JsonNull) null else it }

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

fun JsonObject.getNumber(name: String, defaultValue: Number = 0): Number = getOrNull(name)?.let {
            if (it is JsonPrimitive && it.isNumber) it.asNumber else defaultValue
        } ?: defaultValue

fun JsonObject.getDouble(name: String, defaultValue: Double = 0.toDouble()): Double = getOrNull(name)?.let {
            if (it is JsonPrimitive && it.isNumber) it.asDouble else defaultValue
        } ?: defaultValue

fun JsonObject.getFloat(name: String, defaultValue: Float = 0.toFloat()): Float = getOrNull(name)?.let {
            if (it is JsonPrimitive && it.isNumber) it.asFloat else defaultValue
        } ?: defaultValue

fun JsonObject.getLong(name: String, defaultValue: Long = 0.toLong()): Long = getOrNull(name)?.let {
            if (it is JsonPrimitive && it.isNumber) it.asLong else defaultValue
        } ?: defaultValue

fun JsonObject.getInt(name: String, defaultValue: Int = 0): Int = getOrNull(name)?.let {
            if (it is JsonPrimitive && it.isNumber) it.asInt else defaultValue
        } ?: defaultValue

fun JsonObject.getShort(name: String, defaultValue: Short = 0.toShort()): Short = getOrNull(name)?.let {
            if (it is JsonPrimitive && it.isNumber) it.asShort else defaultValue
        } ?: defaultValue

fun JsonObject.getByte(name: String, defaultValue: Byte = 0.toByte()): Byte = getOrNull(name)?.let {
            if (it is JsonPrimitive && it.isNumber) it.asByte else defaultValue
        } ?: defaultValue

fun JsonObject.getBigDecimal(
        name: String,
        defaultValue: BigDecimal = 0.toBigDecimal()
): BigDecimal = getOrNull(name)?.let {
            if (it is JsonPrimitive && it.isNumber) it.asBigDecimal else defaultValue
        } ?: defaultValue

fun JsonObject.getBigInteger(
        name: String,
        defaultValue: BigInteger = 0.toBigInteger()
): BigInteger = getOrNull(name)?.let {
            if (it is JsonPrimitive && it.isNumber) it.asBigInteger else defaultValue
        } ?: defaultValue

val JsonArray.size: Int get() = size()

val JsonObject.size: Int get() = size()

operator fun JsonObject.iterator(): Iterator<MutableMap.MutableEntry<String, JsonElement>> = entrySet().iterator()

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

fun main() {
    val json = """[{"id":10,"name":"jack"},{"id":11,"name":"tom"}]"""

    println(json.parseAsJson<List<Item>>())
}

open class Data<T> {

    val code: Int = 0
    val dataList: T? = null
    val message: String? = null

    override fun toString(): String {
        return "Data(code=$code, dataList=$dataList, message=$message)"
    }


}

data class Item(
        val id: Int,
        val name: String
)