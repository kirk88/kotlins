@file:Suppress("unused")

package com.nice.kotlins.helper

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import java.io.Reader
import java.lang.reflect.Type

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

fun Any.toJsonObject(): JsonObject = JsonParser.parseString(toJson()).asJsonObject

fun Any.toJsonArray(): JsonArray = JsonParser.parseString(toJson()).asJsonArray

fun Any?.toJsonOrNull(): String? = if (this == null) null else runCatching { toJson() }.getOrNull()

fun Any.toJson(): String = GsonProvider.gson.toJson(this)

fun JsonArray.isEmpty() = size() == 0

fun JsonArray.isNotEmpty() = size() != 0

val JsonArray.size: Int get() = size()

fun JsonObject.isEmpty() = size() == 0

fun JsonObject.isNotEmpty() = size() != 0

val JsonObject.size: Int get() = size()

operator fun JsonObject.iterator(): Iterator<MutableMap.MutableEntry<String, JsonElement>> =
    entrySet().iterator()

inline fun JsonObject.forEach(block: (name: String, value: JsonElement) -> Unit) {
    this[""]
    for ((name, element) in entrySet()) {
        block(name, element)
    }
}

inline fun JsonObject.forEachIndexed(block: (index: Int, key: String, value: JsonElement) -> Unit) {
    for ((index, entry) in entrySet().withIndex()) {
        block(index, entry.key, entry.value)
    }
}