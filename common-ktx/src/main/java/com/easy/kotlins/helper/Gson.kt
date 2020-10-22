package com.easy.kotlins.helper

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type
import java.math.BigDecimal
import java.math.BigInteger

val gson: Gson = Gson()

inline fun <reified T> parseJsonObject(json: String): T {
    return gson.fromJson(json, T::class.java)
}

inline fun <reified T> parseJsonArray(json: String): List<T> {
    return gson.fromJson(json, listType(T::class.java))
}

fun <T> parseJsonObject(json: String, type: Type): T {
    return gson.fromJson(json, type)
}

fun <T> parseJsonArray(json: String, type: Type): List<T> {
    return gson.fromJson(json, listType(type))
}

inline fun <reified T> JsonElement.parseAsObject(): T {
    return asJsonObject.parse()
}

inline fun <reified T> JsonElement.parseAsArray(): List<T> {
    return asJsonArray.parse()
}

inline fun <reified T> JsonObject.parse(): T{
    return gson.fromJson(this, T::class.java)
}

inline fun <reified T> JsonArray.parse(): List<T>{
    return gson.fromJson(this, listType(T::class.java))
}

fun String.toJsonObject(): JsonObject {
    return parseJsonObject(this)
}

fun String.toJsonArray(): JsonArray {
    return  parseJsonObject(this)
}

fun String.toJsonElement(): JsonElement? {
    return  gson.fromJson(this, JsonElement::class.java)
}

fun Any.toJsonObject(): JsonObject = gson.fromJson(this.toJson(), JsonObject::class.java)

fun Any.toJsonArray(): JsonArray =  gson.fromJson(this.toJson(), JsonArray::class.java)

fun Any?.toJsonOrNull(): String? = this?.let { gson.toJson(it) }

fun Any.toJson(): String = gson.toJson(this)

inline fun <reified T> JsonObject.getAsObject(name: String? = null): T {
    return gson.fromJson(if (name != null) this.get(name) else this, T::class.java)
}

inline fun <reified T> JsonObject.getAsArray(name: String): List<T>? {
    return gson.fromJson(this.get(name), listType(T::class.java))
}

fun JsonObject.getAsString(name: String, defaultValue: String? = null): String? = this.get(name)?.asString ?: defaultValue

fun JsonObject.getAsNumber(name: String, defaultValue: Number? = null): Number? = this.get(name)?.asNumber ?: defaultValue

fun JsonObject.getAsDouble(name: String, defaultValue: Double = 0.D): Double = this.get(name)?.asDouble ?: defaultValue

fun JsonObject.getAsFloat(name: String, defaultValue: Float = 0.F): Float = this.get(name)?.asFloat ?: defaultValue

fun JsonObject.getAsLong(name: String, defaultValue: Long = 0.L): Long = this.get(name)?.asLong ?: defaultValue

fun JsonObject.getAsInt(name: String, defaultValue: Int = 0.I): Int = this.get(name)?.asInt ?: defaultValue

fun JsonObject.getAsShort(name: String, defaultValue: Short = 0.S): Short = this.get(name)?.asShort ?: defaultValue

fun JsonObject.getAsByte(name: String, defaultValue: Byte? = null): Byte? = this.get(name)?.asByte ?: defaultValue

fun JsonObject.getAsChar(name: String, defaultValue: Char? = null): Char? = this.get(name)?.asCharacter ?: defaultValue

fun JsonObject.getAsBigDecimal(name: String, defaultValue: BigDecimal = BigDecimal.ZERO): BigDecimal = this.get(name)?.asBigDecimal ?: defaultValue

fun JsonObject.getAsBigInteger(name: String, defaultValue: BigInteger = BigInteger.ZERO): BigInteger = this.get(name)?.asBigInteger ?: defaultValue

fun JsonObject.getAsBoolean(name: String, defaultValue: Boolean = false): Boolean = this.get(name)?.asBoolean ?: defaultValue

inline fun JsonObject.forEach(action: (entry: MutableMap.MutableEntry<String, JsonElement?>) -> Unit) = this.entrySet()?.forEach(action)

inline fun JsonObject.forEach(action: (key: String, element: JsonElement?) -> Unit) = this.entrySet()?.forEach {
    action.invoke(it.key, it.value)
}

inline fun JsonObject.forEachIndexed(action: (index: Int, entry: MutableMap.MutableEntry<String, JsonElement?>) -> Unit) = this.entrySet()?.forEachIndexed(action)

inline fun JsonObject.forEachIndexed(action: (index: Int, key: String, element: JsonElement?) -> Unit) = this.entrySet()?.forEachIndexed { index, entry ->
    action.invoke(index, entry.key, entry.value)
}