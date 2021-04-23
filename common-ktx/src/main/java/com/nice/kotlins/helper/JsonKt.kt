@file:Suppress("unused")

package com.nice.kotlins.helper

import com.google.gson.*
import com.google.gson.internal.`$Gson$Types`
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

@PublishedApi
internal val gson = Gson()

@PublishedApi
internal fun typeOf(parametrized: Class<*>, vararg parameterClasses: Class<*>): Type {
    return `$Gson$Types`.canonicalize(
        `$Gson$Types`.newParameterizedTypeWithOwner(
            null,
            parametrized,
            *parameterClasses
        )
    )
}

fun <T> parseJson(json: String, type: Type): T {
    return gson.fromJson(json, type)
}

fun <T> parseJson(json: String, clazz: Class<T>): T {
    return gson.fromJson(json, clazz)
}

inline fun <reified T> parseJson(json: String): T {
    return gson.fromJson(json, object : TypeToken<T>() {}.type)
}

inline fun <reified T> JsonObject.parse(): T {
    return gson.fromJson(this, T::class.java)
}

inline fun <reified T> JsonArray.parse(): List<T> {
    return gson.fromJson(this, typeOf(ArrayList::class.java, T::class.java))
}

fun Any.toJsonObject(): JsonObject = JsonParser.parseString(toJson()).asJsonObject

fun Any.toJsonArray(): JsonArray = JsonParser.parseString(toJson()).asJsonArray

fun Any?.toJsonOrNull(): String? = if (this == null) null else runCatching { toJson() }.getOrNull()

fun Any.toJson(): String = gson.toJson(this)

fun JsonObject.isEmpty() = size() == 0

fun JsonObject.isNotEmpty() = size() != 0

operator fun JsonObject.iterator(): Iterator<MutableMap.MutableEntry<String, JsonElement>> = entrySet().iterator()

fun JsonObject.forEach(block: (name: String, value: JsonElement) -> Unit) {
    for ((name, element) in entrySet()) {
        block(name, element)
    }
}

fun JsonObject.forEachIndexed(block: (index: Int, key: String, value: JsonElement) -> Unit) {
    for ((index, entry) in entrySet().withIndex()) {
        block(index, entry.key, entry.value)
    }
}