@file:Suppress("unused")

package com.easy.kotlins.helper

import com.google.gson.*
import com.google.gson.internal.`$Gson$Types`
import java.lang.reflect.Type

@PublishedApi
internal val gson = Gson()

@PublishedApi
internal fun typeOf(parametrized: Class<*>, vararg parameterClasses: Class<*>): Type? {
    return `$Gson$Types`.canonicalize(
        `$Gson$Types`.newParameterizedTypeWithOwner(
            null,
            parametrized,
            *parameterClasses
        )
    )
}

inline fun <reified T> String.parseJsonObject(): T {
    return gson.fromJson(this, T::class.java)
}

inline fun <reified T> String.parseJsonArray(): List<T> {
    return gson.fromJson(this, typeOf(ArrayList::class.java, T::class.java))
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

val JsonArray.values: List<JsonElement>
    get() {
        if (size() == 0) {
            return emptyList()
        }
        return this.toList()
    }

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