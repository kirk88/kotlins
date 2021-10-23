@file:Suppress("UNUSED")

package com.nice.kotson

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

fun Number.toJson(): JsonPrimitive = JsonPrimitive(this)

fun Char.toJson(): JsonPrimitive = JsonPrimitive(this)

fun Boolean.toJson(): JsonPrimitive = JsonPrimitive(this)

fun String.toJson(): JsonPrimitive = JsonPrimitive(this)

internal fun Any?.toJsonElement(): JsonElement {
    if (this == null)
        return NullJson

    return when (this) {
        is JsonElement -> this
        is String -> toJson()
        is Number -> toJson()
        is Char -> toJson()
        is Boolean -> toJson()
        else -> throw IllegalArgumentException("$this cannot be converted to JSON")
    }
}

private fun jsonArrayOf(values: Iterator<Any?>): JsonArray {
    val array = JsonArray()
    for (value in values)
        array.add(value.toJsonElement())
    return array
}

fun jsonArray(vararg values: Any?) = jsonArrayOf(values.iterator())
fun jsonArray(values: Iterable<*>) = jsonArrayOf(values.iterator())
fun jsonArray(values: Sequence<*>) = jsonArrayOf(values.iterator())

fun Iterable<*>.toJsonArray() = jsonArray(this)
fun Sequence<*>.toJsonArray() = jsonArray(this)

private fun jsonObjectOf(values: Iterator<Pair<String, *>>): JsonObject {
    val obj = JsonObject()
    for ((key, value) in values) {
        obj.add(key, value.toJsonElement())
    }
    return obj
}

fun jsonObject(vararg values: Pair<String, *>) = jsonObjectOf(values.iterator())
fun jsonObject(values: Iterable<Pair<String, *>>) = jsonObjectOf(values.iterator())
fun jsonObject(values: Sequence<Pair<String, *>>) = jsonObjectOf(values.iterator())

fun Iterable<Pair<String, *>>.toJsonObject() = jsonObject(this)
fun Sequence<Pair<String, *>>.toJsonObject() = jsonObject(this)

fun JsonObject.shallowCopy(): JsonObject = JsonObject().apply { this@shallowCopy.entrySet().forEach { put(it) } }
fun JsonArray.shallowCopy(): JsonArray = JsonArray().apply { addAll(this@shallowCopy) }