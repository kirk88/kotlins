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

inline fun <reified T> parseJsonObject(json: String): T {
    return objectMapper.readValue(json, T::class.java)
}

inline fun <reified T> parseJsonArray(json: String): List<T> {
    return objectMapper.readValue(json, typeOfList(T::class.java))
}

inline fun <reified T> JSONObject.parse(): T {
    return objectMapper.readValue(this.toString(), T::class.java)
}

inline fun <reified T> JSONArray.parse(): List<T> {
    return objectMapper.readValue(this.toString(), typeOfList(T::class.java))
}

fun Any.toJsonObject(): JSONObject =
    JSONObject(if (this is String) this else objectMapper.writeValueAsString(this))

fun Any.toJsonArray(): JSONArray =
    JSONArray(if (this is String) this else objectMapper.writeValueAsString(this))

fun Any?.toJsonOrNull(): String? = if (this == null) null else objectMapper.writeValueAsString(this)

fun Any.toJson(): String = objectMapper.writeValueAsString(this)

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