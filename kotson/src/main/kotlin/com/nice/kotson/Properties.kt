package com.nice.kotson

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


operator fun JsonObject.getValue(thisRef: Any?, property: KProperty<*>): JsonElement = obj[property.name]

operator fun JsonObject.setValue(thisRef: Any?, property: KProperty<*>, value: JsonElement) {
    obj[property.name] = value
}

class JsonObjectDelegate<T : Any>(
    private val target: JsonObject,
    private val getValue: (JsonElement) -> T,
    private val setValue: (T) -> JsonElement,
    private val key: String? = null,
    private val defaultValue: (() -> T)? = null
) : ReadWriteProperty<Any?, T> {

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val element = target[key ?: property.name]
        if (element === null) {
            val default = defaultValue
            if (default === null)
                throw NoSuchElementException("'$key' not found")
            else
                return default.invoke()
        }
        return getValue(element)
    }

    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        target[key ?: property.name] = setValue(value)
    }

}

class NullableJsonObjectDelegate<T : Any?>(
    private val target: JsonObject,
    private val getValue: (JsonElement) -> T?,
    private val setValue: (T?) -> JsonElement,
    private val key: String? = null,
    private val defaultValue: (() -> T)? = null
) : ReadWriteProperty<Any?, T?> {

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        val element = target[key ?: property.name]
        if (element === null)
            return defaultValue?.invoke()
        return getValue(element)
    }

    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        target[key ?: property.name] = setValue(value)
    }

}

class JsonArrayDelegate<T : Any>(
    private val target: JsonArray,
    private val getValue: (JsonElement) -> T,
    private val setValue: (T) -> JsonElement,
    private val index: Int
) : ReadWriteProperty<Any?, T> {

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T = getValue(target[index])

    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        target[index] = setValue(value)
    }

}

val JsonElement.byString: JsonObjectDelegate<String> get() = JsonObjectDelegate(this.obj, { it.string }, { it.toJson() })
val JsonElement.byBool: JsonObjectDelegate<Boolean> get() = JsonObjectDelegate(this.obj, { it.bool }, { it.toJson() })
val JsonElement.byByte: JsonObjectDelegate<Byte> get() = JsonObjectDelegate(this.obj, { it.byte }, { it.toJson() })
val JsonElement.byChar: JsonObjectDelegate<Char> get() = JsonObjectDelegate(this.obj, { it.char }, { it.toJson() })
val JsonElement.byShort: JsonObjectDelegate<Short> get() = JsonObjectDelegate(this.obj, { it.short }, { it.toJson() })
val JsonElement.byInt: JsonObjectDelegate<Int> get() = JsonObjectDelegate(this.obj, { it.int }, { it.toJson() })
val JsonElement.byLong: JsonObjectDelegate<Long> get() = JsonObjectDelegate(this.obj, { it.long }, { it.toJson() })
val JsonElement.byFloat: JsonObjectDelegate<Float> get() = JsonObjectDelegate(this.obj, { it.float }, { it.toJson() })
val JsonElement.byDouble: JsonObjectDelegate<Double> get() = JsonObjectDelegate(this.obj, { it.double }, { it.toJson() })
val JsonElement.byNumber: JsonObjectDelegate<Number> get() = JsonObjectDelegate(this.obj, { it.number }, { it.toJson() })
val JsonElement.byBigInteger: JsonObjectDelegate<BigInteger> get() = JsonObjectDelegate(this.obj, { it.bigInteger }, { it.toJson() })
val JsonElement.byBigDecimal: JsonObjectDelegate<BigDecimal> get() = JsonObjectDelegate(this.obj, { it.bigDecimal }, { it.toJson() })
val JsonElement.byArray: JsonObjectDelegate<JsonArray> get() = JsonObjectDelegate(this.obj, { it.arr }, { it })
val JsonElement.byObject: JsonObjectDelegate<JsonObject> get() = JsonObjectDelegate(this.obj, { it.obj }, { it })

fun JsonElement.byString(key: String? = null, default: (() -> String)? = null): JsonObjectDelegate<String> = JsonObjectDelegate(this.obj, { it.string }, { it.toJson() }, key, default)
fun JsonElement.byBool(key: String? = null, default: (() -> Boolean)? = null): JsonObjectDelegate<Boolean> = JsonObjectDelegate(this.obj, { it.bool }, { it.toJson() }, key, default)
fun JsonElement.byByte(key: String? = null, default: (() -> Byte)? = null): JsonObjectDelegate<Byte> = JsonObjectDelegate(this.obj, { it.byte }, { it.toJson() }, key, default)
fun JsonElement.byChar(key: String? = null, default: (() -> Char)? = null): JsonObjectDelegate<Char> = JsonObjectDelegate(this.obj, { it.char }, { it.toJson() }, key, default)
fun JsonElement.byShort(key: String? = null, default: (() -> Short)? = null): JsonObjectDelegate<Short> = JsonObjectDelegate(this.obj, { it.short }, { it.toJson() }, key, default)
fun JsonElement.byInt(key: String? = null, default: (() -> Int)? = null): JsonObjectDelegate<Int> = JsonObjectDelegate(this.obj, { it.int }, { it.toJson() }, key, default)
fun JsonElement.byLong(key: String? = null, default: (() -> Long)? = null): JsonObjectDelegate<Long> = JsonObjectDelegate(this.obj, { it.long }, { it.toJson() }, key, default)
fun JsonElement.byFloat(key: String? = null, default: (() -> Float)? = null): JsonObjectDelegate<Float> = JsonObjectDelegate(this.obj, { it.float }, { it.toJson() }, key, default)
fun JsonElement.byDouble(key: String? = null, default: (() -> Double)? = null): JsonObjectDelegate<Double> = JsonObjectDelegate(this.obj, { it.double }, { it.toJson() }, key, default)
fun JsonElement.byNumber(key: String? = null, default: (() -> Number)? = null): JsonObjectDelegate<Number> = JsonObjectDelegate(this.obj, { it.number }, { it.toJson() }, key, default)
fun JsonElement.byBigInteger(key: String? = null, default: (() -> BigInteger)? = null): JsonObjectDelegate<BigInteger> = JsonObjectDelegate(this.obj, { it.bigInteger }, { it.toJson() }, key, default)
fun JsonElement.byBigDecimal(key: String? = null, default: (() -> BigDecimal)? = null): JsonObjectDelegate<BigDecimal> = JsonObjectDelegate(this.obj, { it.bigDecimal }, { it.toJson() }, key, default)
fun JsonElement.byArray(key: String? = null, default: (() -> JsonArray)? = null): JsonObjectDelegate<JsonArray> = JsonObjectDelegate(this.obj, { it.arr }, { it }, key, default)
fun JsonElement.byObject(key: String? = null, default: (() -> JsonObject)? = null): JsonObjectDelegate<JsonObject> = JsonObjectDelegate(this.obj, { it.obj }, { it }, key, default)

val JsonElement.byNullableString: NullableJsonObjectDelegate<String?> get() = NullableJsonObjectDelegate(this.obj, { it.string }, { it?.toJson() ?: NullJson })
val JsonElement.byNullableBool: NullableJsonObjectDelegate<Boolean?> get() = NullableJsonObjectDelegate(this.obj, { it.bool }, { it?.toJson() ?: NullJson })
val JsonElement.byNullableByte: NullableJsonObjectDelegate<Byte?> get() = NullableJsonObjectDelegate(this.obj, { it.byte }, { it?.toJson() ?: NullJson })
val JsonElement.byNullableChar: NullableJsonObjectDelegate<Char?> get() = NullableJsonObjectDelegate(this.obj, { it.char }, { it?.toJson() ?: NullJson })
val JsonElement.byNullableShort: NullableJsonObjectDelegate<Short?> get() = NullableJsonObjectDelegate(this.obj, { it.short }, { it?.toJson() ?: NullJson })
val JsonElement.byNullableInt: NullableJsonObjectDelegate<Int?> get() = NullableJsonObjectDelegate(this.obj, { it.int }, { it?.toJson() ?: NullJson })
val JsonElement.byNullableLong: NullableJsonObjectDelegate<Long?> get() = NullableJsonObjectDelegate(this.obj, { it.long }, { it?.toJson() ?: NullJson })
val JsonElement.byNullableFloat: NullableJsonObjectDelegate<Float?> get() = NullableJsonObjectDelegate(this.obj, { it.float }, { it?.toJson() ?: NullJson })
val JsonElement.byNullableDouble: NullableJsonObjectDelegate<Double?> get() = NullableJsonObjectDelegate(this.obj, { it.double }, { it?.toJson() ?: NullJson })
val JsonElement.byNullableNumber: NullableJsonObjectDelegate<Number?> get() = NullableJsonObjectDelegate(this.obj, { it.number }, { it?.toJson() ?: NullJson })
val JsonElement.byNullableBigInteger: NullableJsonObjectDelegate<BigInteger?> get() = NullableJsonObjectDelegate(this.obj, { it.bigInteger }, { it?.toJson() ?: NullJson })
val JsonElement.byNullableBigDecimal: NullableJsonObjectDelegate<BigDecimal?> get() = NullableJsonObjectDelegate(this.obj, { it.bigDecimal }, { it?.toJson() ?: NullJson })
val JsonElement.byNullableArray: NullableJsonObjectDelegate<JsonArray?> get() = NullableJsonObjectDelegate(this.obj, { it.arr }, { it ?: NullJson })
val JsonElement.byNullableObject: NullableJsonObjectDelegate<JsonObject?> get() = NullableJsonObjectDelegate(this.obj, { it.obj }, { it ?: NullJson })

fun JsonElement.byNullableString(key: String? = null, default: (() -> String)? = null): NullableJsonObjectDelegate<String?> =
    NullableJsonObjectDelegate(this.obj, { it.string }, { it?.toJson() ?: NullJson }, key, default)

fun JsonElement.byNullableBool(key: String? = null, default: (() -> Boolean)? = null): NullableJsonObjectDelegate<Boolean?> =
    NullableJsonObjectDelegate(this.obj, { it.bool }, { it?.toJson() ?: NullJson }, key, default)

fun JsonElement.byNullableByte(key: String? = null, default: (() -> Byte)? = null): NullableJsonObjectDelegate<Byte?> =
    NullableJsonObjectDelegate(this.obj, { it.byte }, { it?.toJson() ?: NullJson }, key, default)

fun JsonElement.byNullableChar(key: String? = null, default: (() -> Char)? = null): NullableJsonObjectDelegate<Char?> =
    NullableJsonObjectDelegate(this.obj, { it.char }, { it?.toJson() ?: NullJson }, key, default)

fun JsonElement.byNullableShort(key: String? = null, default: (() -> Short)? = null): NullableJsonObjectDelegate<Short?> =
    NullableJsonObjectDelegate(this.obj, { it.short }, { it?.toJson() ?: NullJson }, key, default)

fun JsonElement.byNullableInt(key: String? = null, default: (() -> Int)? = null): NullableJsonObjectDelegate<Int?> =
    NullableJsonObjectDelegate(this.obj, { it.int }, { it?.toJson() ?: NullJson }, key, default)

fun JsonElement.byNullableLong(key: String? = null, default: (() -> Long)? = null): NullableJsonObjectDelegate<Long?> =
    NullableJsonObjectDelegate(this.obj, { it.long }, { it?.toJson() ?: NullJson }, key, default)

fun JsonElement.byNullableFloat(key: String? = null, default: (() -> Float)? = null): NullableJsonObjectDelegate<Float?> =
    NullableJsonObjectDelegate(this.obj, { it.float }, { it?.toJson() ?: NullJson }, key, default)

fun JsonElement.byNullableDouble(key: String? = null, default: (() -> Double)? = null): NullableJsonObjectDelegate<Double?> =
    NullableJsonObjectDelegate(this.obj, { it.double }, { it?.toJson() ?: NullJson }, key, default)

fun JsonElement.byNullableNumber(key: String? = null, default: (() -> Number)? = null): NullableJsonObjectDelegate<Number?> =
    NullableJsonObjectDelegate(this.obj, { it.number }, { it?.toJson() ?: NullJson }, key, default)

fun JsonElement.byNullableBigInteger(key: String? = null, default: (() -> BigInteger)? = null): NullableJsonObjectDelegate<BigInteger?> =
    NullableJsonObjectDelegate(this.obj, { it.bigInteger }, { it?.toJson() ?: NullJson }, key, default)

fun JsonElement.byNullableBigDecimal(key: String? = null, default: (() -> BigDecimal)? = null): NullableJsonObjectDelegate<BigDecimal?> =
    NullableJsonObjectDelegate(this.obj, { it.bigDecimal }, { it?.toJson() ?: NullJson }, key, default)

fun JsonElement.byNullableArray(key: String? = null, default: (() -> JsonArray)? = null): NullableJsonObjectDelegate<JsonArray?> =
    NullableJsonObjectDelegate(this.obj, { it.arr }, { it ?: NullJson }, key, default)

fun JsonElement.byNullableObject(key: String? = null, default: (() -> JsonObject)? = null): NullableJsonObjectDelegate<JsonObject?> =
    NullableJsonObjectDelegate(this.obj, { it.obj }, { it ?: NullJson }, key, default)

fun JsonElement.byString(index: Int): JsonArrayDelegate<String> = JsonArrayDelegate(this.arr, { it.string }, { it.toJson() }, index)
fun JsonElement.byBool(index: Int): JsonArrayDelegate<Boolean> = JsonArrayDelegate(this.arr, { it.bool }, { it.toJson() }, index)
fun JsonElement.byByte(index: Int): JsonArrayDelegate<Byte> = JsonArrayDelegate(this.arr, { it.byte }, { it.toJson() }, index)
fun JsonElement.byChar(index: Int): JsonArrayDelegate<Char> = JsonArrayDelegate(this.arr, { it.char }, { it.toJson() }, index)
fun JsonElement.byShort(index: Int): JsonArrayDelegate<Short> = JsonArrayDelegate(this.arr, { it.short }, { it.toJson() }, index)
fun JsonElement.byInt(index: Int): JsonArrayDelegate<Int> = JsonArrayDelegate(this.arr, { it.int }, { it.toJson() }, index)
fun JsonElement.byLong(index: Int): JsonArrayDelegate<Long> = JsonArrayDelegate(this.arr, { it.long }, { it.toJson() }, index)
fun JsonElement.byFloat(index: Int): JsonArrayDelegate<Float> = JsonArrayDelegate(this.arr, { it.float }, { it.toJson() }, index)
fun JsonElement.byDouble(index: Int): JsonArrayDelegate<Double> = JsonArrayDelegate(this.arr, { it.double }, { it.toJson() }, index)
fun JsonElement.byNumber(index: Int): JsonArrayDelegate<Number> = JsonArrayDelegate(this.arr, { it.number }, { it.toJson() }, index)
fun JsonElement.byBigInteger(index: Int): JsonArrayDelegate<BigInteger> = JsonArrayDelegate(this.arr, { it.bigInteger }, { it.toJson() }, index)
fun JsonElement.byBigDecimal(index: Int): JsonArrayDelegate<BigDecimal> = JsonArrayDelegate(this.arr, { it.bigDecimal }, { it.toJson() }, index)
fun JsonElement.byArray(index: Int): JsonArrayDelegate<JsonArray> = JsonArrayDelegate(this.arr, { it.arr }, { it }, index)
fun JsonElement.byObject(index: Int): JsonArrayDelegate<JsonObject> = JsonArrayDelegate(this.arr, { it.obj }, { it }, index)
