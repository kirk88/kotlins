package com.easy.kotlins.helper

import org.codehaus.jackson.JsonParser
import org.codehaus.jackson.map.ObjectMapper
import org.codehaus.jackson.type.JavaType
import org.json.JSONArray
import org.json.JSONObject

@PublishedApi
internal val objectMapper = ObjectMapper().also {
    it.configure(JsonParser.Feature.ALLOW_COMMENTS, true)
    it.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
    it.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
}

inline fun <reified T> String.parseAsJSONObject(): T {
    return objectMapper.readValue(this, T::class.java)
}

inline fun <reified T> String.parseAsJSONArray(): List<T> {
    return objectMapper.readValue(this, typeOfList(T::class.java))
}

inline fun <reified T> JSONObject.parse(): T {
    return objectMapper.readValue(this.toString(), T::class.java)
}

inline fun <reified T> JSONArray.parse(): List<T> {
    return objectMapper.readValue(this.toString(), typeOfList(T::class.java))
}

fun Any.toJSONObject(): JSONObject =
    JSONObject(if (this is String) this else objectMapper.writeValueAsString(this))

fun Any.toJSONArray(): JSONArray =
    JSONArray(if (this is String) this else objectMapper.writeValueAsString(this))

fun Any?.toJSONOrNull(): String? = if (this == null) null else objectMapper.writeValueAsString(this)

fun Any.toJSON(): String = objectMapper.writeValueAsString(this)

fun typeOfList(type: Class<*>): JavaType? {
    return type(ArrayList::class.java, type)
}

fun typeOfSet(type: Class<*>): JavaType? {
    return type(HashSet::class.java, type)
}

fun typeOfMap(keyType: Class<*>, valueType: Class<*>): JavaType? {
    return type(HashMap::class.java, keyType, valueType)
}

fun typeOfArray(type: Class<*>): JavaType? {
    return objectMapper.typeFactory.constructArrayType(type)
}

fun type(rawType: Class<*>, vararg typeArguments: Class<*>): JavaType? {
    return objectMapper.typeFactory.constructParametricType(rawType, *typeArguments)
}

fun JSONObject.forEach(block: (key: String, element: JSONElement) -> Unit) {
    for (key in keys()) {
        block(key, JSONElement(opt(key)))
    }
}

fun JSONObject.forEachIndexed(block: (index: Int, key: String, element: JSONElement) -> Unit) {
    for ((index, key) in keys().withIndex()) {
        block(index, key, JSONElement(opt(key)))
    }
}

fun JSONArray.forEach(block: (element: JSONElement) -> Unit) {
    for (index in 0 until length()) {
        block(JSONElement(opt(index)))
    }
}

fun JSONArray.forEachIndexed(block: (index: Int, element: JSONElement) -> Unit) {
    for (index in 0 until length()) {
        block(index, JSONElement(opt(index)))
    }
}

class JSONElement(private val value: Any?) {

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
