package com.easy.kotlins.helper

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

@PublishedApi
internal val objectMapper = JsonMapper.builder()
    .addModule(KotlinModule(strictNullChecks = true))
    .build()

@PublishedApi
internal fun type(parametrized: Class<*>, vararg parameterClasses: Class<*>): JavaType? {
    return objectMapper.typeFactory.constructParametricType(parametrized, *parameterClasses)
}

inline fun <reified T> String.parseAsJSONObject(): T {
    return objectMapper.readValue(this, T::class.java)
}

inline fun <reified T> String.parseAsJSONArray(): List<T> {
    return objectMapper.readValue(this, type(ArrayList::class.java, T::class.java))
}

inline fun <reified T> JSONObject.parse(): T {
    return objectMapper.readValue(this.toString(), T::class.java)
}

inline fun <reified T> JSONArray.parse(): List<T> {
    return objectMapper.readValue(this.toString(), type(ArrayList::class.java, T::class.java))
}

fun Any.toJSONObject(): JSONObject =
    JSONObject(if (this is String) this else objectMapper.writeValueAsString(this))

fun Any.toJSONArray(): JSONArray =
    JSONArray(if (this is String) this else objectMapper.writeValueAsString(this))

fun Any?.toJSONOrNull(): String? = if (this == null) null else objectMapper.writeValueAsString(this)

fun Any.toJSON(): String = objectMapper.writeValueAsString(this)

@Throws(JSONException::class)
fun JSONObject.getValue(name: String): JSONValue = JSONValue(this.get(name))

fun JSONObject.optValue(name: String): JSONValue = JSONValue(this.opt(name))

val JSONArray.values: List<JSONValue>
    get() {
        val values = mutableListOf<JSONValue>()
        for (index in 0 until this.length()) {
            values.add(JSONValue(this.opt(index)))
        }
        return values
    }

fun JSONObject.forEach(block: (key: String, value: JSONValue) -> Unit) {
    for (key in keys()) {
        block(key, JSONValue(opt(key)))
    }
}

fun JSONObject.forEachIndexed(block: (index: Int, key: String, value: JSONValue) -> Unit) {
    for ((index, key) in keys().withIndex()) {
        block(index, key, JSONValue(opt(key)))
    }
}

fun JSONArray.forEach(block: (value: JSONValue) -> Unit) {
    for (index in 0 until length()) {
        block(JSONValue(opt(index)))
    }
}

fun JSONArray.forEachIndexed(block: (index: Int, value: JSONValue) -> Unit) {
    for (index in 0 until length()) {
        block(index, JSONValue(opt(index)))
    }
}

class JSONValue(val value: Any?) {

    val isJSONNull: Boolean
        get() = value == null

    val isJSONArray: Boolean
        get() = value is JSONArray

    val isJSONObject: Boolean
        get() = value is JSONObject

    val isJSONPrimitive: Boolean
        get() = value?.javaClass?.isPrimitive == true

    fun asJSONArray(): JSONArray {
        if (isJSONArray) {
            return value as JSONArray
        }
        throw IllegalStateException("Not a JSON Array: $value")
    }

    fun asJSONObject(): JSONObject {
        if (isJSONObject) {
            return value as JSONObject
        }
        throw IllegalStateException("Not a JSON Object: $value")
    }

    fun asString(fallback: String? = null): String? = value?.toString() ?: fallback

    fun asBoolean(fallback: Boolean = false): Boolean {
        return when (value) {
            is Boolean -> value
            is String -> value.toBoolean()
            else -> fallback
        }
    }

    fun asDouble(fallback: Double = 0.toDouble()): Double {
        return when (value) {
            is Double -> value
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: fallback
            else -> fallback
        }
    }

    fun asFloat(fallback: Float = 0.toFloat()): Float {
        return when (value) {
            is Float -> value
            is Number -> value.toFloat()
            is String -> value.toFloatOrNull() ?: fallback
            else -> fallback
        }
    }

    fun asInt(fallback: Int = 0): Int {
        return when (value) {
            is Int -> value
            is Number -> value.toInt()
            is String -> value.toIntOrNull() ?: fallback
            else -> fallback
        }
    }

    fun asLong(fallback: Long = 0.toLong()): Long {
        return when (value) {
            is Long -> value
            is Number -> value.toLong()
            is String -> value.toLongOrNull() ?: fallback
            else -> fallback
        }
    }
}