package com.easy.kotlins.helper

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type
import java.math.BigDecimal
import java.math.BigInteger

val gson: Gson = Gson()

inline fun <reified T> parseJsonObject(json: String?): T? {
    return json?.let { gson.fromJson(it, T::class.java) }
}

inline fun <reified T> parseJsonArray(json: String?): List<T>? {
    return json?.let { gson.fromJson(it, listType(T::class.java)) }
}

fun <T> parseJsonObject(json: String?, type: Type): T? {
    return json?.let { gson.fromJson(it, type) }
}

fun <T> parseJsonArray(json: String?, type: Type): List<T>? {
    return json?.let { gson.fromJson(it, listType(type)) }
}

inline fun <reified T> JsonElement?.parseAsObject(): T? {
    return this?.let { gson.fromJson(it, T::class.java) }
}

inline fun <reified T> JsonElement?.parseAsArray(): List<T>? {
    return this?.let { gson.fromJson(it, listType(T::class.java)) }
}

fun String?.toJsonObject(): JsonObject {
    return this?.let {
        parseJsonObject<JsonObject>(it)
    } ?: JsonObject()
}

fun String?.toJsonArray(): JsonArray {
    return this?.let {
        parseJsonObject<JsonArray>(it)
    } ?: JsonArray()
}

fun String?.toJsonElement(): JsonElement? {
    return this?.let {
        gson.fromJson(it, JsonElement::class.java)
    }
}

fun Any?.asJsonObject(): JsonObject =
    this?.let { gson.fromJson(it.toJson(), JsonObject::class.java) }
        ?: JsonObject()

fun Any?.asJsonArray(): JsonArray = this?.let { gson.fromJson(it.toJson(), JsonArray::class.java) }
    ?: JsonArray()

fun Any?.toJsonOrNull(): String? = this?.let { gson.toJson(it) }

fun Any.toJson(): String = gson.toJson(this)

inline fun <reified T> JsonObject?.getObjectAs(name: String? = null): T? {
    return this?.let { gson.fromJson(if (name != null) it.get(name) else it, T::class.java) }
}

inline fun <reified T> JsonObject?.getArrayAs(name: String): List<T>? {
    return this?.let { gson.fromJson(it.getAsJsonArray(name), listType(T::class.java)) }
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