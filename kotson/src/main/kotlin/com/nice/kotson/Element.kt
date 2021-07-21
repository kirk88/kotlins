@file:Suppress("unused")

package com.nice.kotson

import com.google.gson.*
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*

private fun <T : Any> JsonElement?.nullOr(getNotNull: JsonElement.() -> T): T? = if (this == null || isJsonNull) null else getNotNull()

val JsonElement.string: String get() = asString
val JsonElement?.stringOrNull: String? get() = nullOr { string }

val JsonElement.bool: Boolean get() = asBoolean
val JsonElement?.boolOrNull: Boolean? get() = nullOr { bool }

val JsonElement.byte: Byte get() = asByte
val JsonElement?.byteOrNull: Byte? get() = nullOr { byte }

val JsonElement.char: Char get() = asString.first()
val JsonElement?.charOrNull: Char? get() = nullOr { char }

val JsonElement.short: Short get() = asShort
val JsonElement?.shortOrNull: Short? get() = nullOr { short }

val JsonElement.int: Int get() = asInt
val JsonElement?.intOrNull: Int? get() = nullOr { int }

val JsonElement.long: Long get() = asLong
val JsonElement?.longOrNull: Long? get() = nullOr { long }

val JsonElement.float: Float get() = asFloat
val JsonElement?.floatOrNull: Float? get() = nullOr { float }

val JsonElement.double: Double get() = asDouble
val JsonElement?.doubleOrNull: Double? get() = nullOr { double }

val JsonElement.number: Number get() = asNumber
val JsonElement?.numberOrNull: Number? get() = nullOr { number }

val JsonElement.bigInteger: BigInteger get() = asBigInteger
val JsonElement?.bigIntegerOrNull: BigInteger? get() = nullOr { bigInteger }

val JsonElement.bigDecimal: BigDecimal get() = asBigDecimal
val JsonElement?.bigDecimalOrNull: BigDecimal? get() = nullOr { bigDecimal }

val JsonElement.arr: JsonArray get() = asJsonArray
val JsonElement?.arrOrNull: JsonArray? get() = nullOr { arr }

val JsonElement.obj: JsonObject get() = asJsonObject
val JsonElement?.objOrNull: JsonObject? get() = nullOr { obj }

val NullJson: JsonNull = JsonNull.INSTANCE

operator fun JsonElement.get(key: String): JsonElement = obj.getValue(key)
operator fun JsonElement.get(index: Int): JsonElement = arr.get(index)

fun JsonObject.getValue(key: String): JsonElement = get(key) ?: throw NoSuchElementException("'$key' is not found")

operator fun JsonObject.contains(key: String): Boolean = has(key)
val JsonObject.size: Int get() = entrySet().size
fun JsonObject.isEmpty(): Boolean = entrySet().isEmpty()
fun JsonObject.isNotEmpty(): Boolean = entrySet().isNotEmpty()
fun JsonObject.keys(): Collection<String> = entrySet().map { it.key }
fun JsonObject.forEach(operation: (String, JsonElement) -> Unit) = entrySet().forEach { operation(it.key, it.value) }
fun JsonObject.forEachIndexed(operation: (Int, String, JsonElement) -> Unit) = entrySet().forEachIndexed { index, entry ->
    operation(index, entry.key, entry.value)
}

fun JsonObject.toMap(): Map<String, JsonElement> = entrySet().associateBy({ it.key }, { it.value })

fun JsonObject.addProperty(property: String, value: JsonElement?) = add(property, value)
fun JsonObject.addProperty(property: String, value: Any?, context: JsonSerializationContext) = add(property, context.serialize(value))
fun JsonObject.addPropertyIfNotNull(property: String, value: String?) = value?.let { addProperty(property, value) }
fun JsonObject.addPropertyIfNotNull(property: String, value: Char?) = value?.let { addProperty(property, value) }
fun JsonObject.addPropertyIfNotNull(property: String, value: Boolean?) = value?.let { addProperty(property, value) }
fun JsonObject.addPropertyIfNotNull(property: String, value: Number?) = value?.let { addProperty(property, value) }
fun JsonObject.addPropertyIfNotNull(property: String, value: JsonElement?) = value?.let { addProperty(property, value) }
fun JsonObject.addPropertyIfNotNull(property: String, value: Any?, context: JsonSerializationContext) = value?.let { addProperty(property, value, context) }

val JsonArray.size: Int get() = size()
fun JsonArray.isNotEmpty(): Boolean = size() > 0
fun JsonArray.contains(value: Any): Boolean = contains(value.toJsonElement())
