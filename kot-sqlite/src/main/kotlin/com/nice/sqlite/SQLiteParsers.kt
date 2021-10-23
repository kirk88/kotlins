@file:Suppress("UNUSED")

package com.nice.sqlite

import android.database.Cursor
import android.database.sqlite.SQLiteException
import java.nio.charset.StandardCharsets

fun interface RowParser<out T> {
    fun parseRow(row: Array<ColumnValue>): T
}

fun interface MapRowParser<out T> {
    fun parseRow(row: Map<String, ColumnValue>): T
}

private class SingleColumnParser<out T>(val modifier: (ColumnValue) -> T) : RowParser<T> {
    override fun parseRow(row: Array<ColumnValue>): T {
        if (row.size != 1)
            throw SQLiteException("Invalid row: row for SingleColumnParser must contain exactly one column")
        @Suppress("UNCHECKED_CAST")
        return modifier(row[0])
    }
}

val ShortParser: RowParser<Short> = SingleColumnParser(modifier = ColumnValue::asShort)
val IntParser: RowParser<Int> = SingleColumnParser(modifier = ColumnValue::asInt)
val LongParser: RowParser<Long> = SingleColumnParser(modifier = ColumnValue::asLong)
val FloatParser: RowParser<Float> = SingleColumnParser(modifier = ColumnValue::asFloat)
val DoubleParser: RowParser<Double> = SingleColumnParser(modifier = ColumnValue::asDouble)
val StringParser: RowParser<String> = SingleColumnParser(modifier = ColumnValue::asString)
val BlobParser: RowParser<ByteArray> = SingleColumnParser(modifier = ColumnValue::asBlob)

fun <T : Any> Cursor.single(parser: RowParser<T>): T = use {
    if (it.count != 1)
        throw SQLiteException("single accepts only cursors with getCount() == 1")
    it.moveToFirst()
    return parser.parseRow(readColumnsArray(it))
}

fun <T : Any> Cursor.singleOrNull(parser: RowParser<T>): T? = use {
    if (it.count > 1)
        throw SQLiteException("singleOrNull accepts only cursors with getCount() == 1 or empty cursors")
    if (it.count == 0)
        return null
    it.moveToFirst()
    return parser.parseRow(readColumnsArray(it))
}

fun <T : Any> Cursor.toList(parser: RowParser<T>): List<T> = use {
    val list = ArrayList<T>(it.count)
    it.moveToFirst()
    while (!it.isAfterLast) {
        list.add(parser.parseRow(readColumnsArray(it)))
        it.moveToNext()
    }
    return list
}

fun <T : Any> Cursor.single(parser: MapRowParser<T>): T = use {
    if (it.count != 1)
        throw SQLiteException("single accepts only cursors with getCount() == 1")
    it.moveToFirst()
    return parser.parseRow(readColumnsMap(it))
}

fun <T : Any> Cursor.singleOrNull(parser: MapRowParser<T>): T? = use {
    if (it.count > 1)
        throw SQLiteException("singleOrNull accepts only cursors with getCount() == 1 or empty cursors")
    if (it.count == 0)
        return null
    it.moveToFirst()
    return parser.parseRow(readColumnsMap(it))
}

fun <T : Any> Cursor.toList(parser: MapRowParser<T>): List<T> = use {
    val list = ArrayList<T>(it.count)
    it.moveToFirst()
    while (!it.isAfterLast) {
        list.add(parser.parseRow(readColumnsMap(it)))
        it.moveToNext()
    }
    return list
}

inline fun <reified T : Any> Cursor.single(): T = single(classParser())

inline fun <reified T : Any> Cursor.singleOrNull(): T? = singleOrNull(classParser())

inline fun <reified T : Any> Cursor.toList(): List<T> = toList(classParser())

fun Cursor.asSequence(): Sequence<Array<ColumnValue>> {
    return CursorSequence(this)
}

fun Cursor.asMapSequence(): Sequence<Map<String, ColumnValue>> {
    return CursorMapSequence(this)
}

fun Cursor.getColumnValue(index: Int): ColumnValue {
    val value = if (isNull(index)) null
    else when (getType(index)) {
        Cursor.FIELD_TYPE_INTEGER -> getLong(index)
        Cursor.FIELD_TYPE_FLOAT -> getDouble(index)
        Cursor.FIELD_TYPE_STRING -> getString(index)
        Cursor.FIELD_TYPE_BLOB -> getBlob(index)
        else -> null
    }
    return ColumnValue(value)
}

private fun readColumnsArray(cursor: Cursor): Array<ColumnValue> {
    val count = cursor.columnCount
    val list = ArrayList<ColumnValue>(count)
    for (index in 0 until count) {
        list.add(cursor.getColumnValue(index))
    }
    return list.toTypedArray()
}

private fun readColumnsMap(cursor: Cursor): Map<String, ColumnValue> {
    val count = cursor.columnCount
    val map = mutableMapOf<String, ColumnValue>()
    for (index in 0 until count) {
        map[cursor.getColumnName(index)] = cursor.getColumnValue(index)
    }
    return map
}

private class CursorMapSequence(val cursor: Cursor) : Sequence<Map<String, ColumnValue>> {
    override fun iterator() = object : Iterator<Map<String, ColumnValue>> {
        override fun hasNext() = cursor.position < cursor.count - 1

        override fun next(): Map<String, ColumnValue> {
            cursor.moveToNext()
            return readColumnsMap(cursor)
        }
    }
}

private class CursorSequence(val cursor: Cursor) : Sequence<Array<ColumnValue>> {
    override fun iterator() = object : Iterator<Array<ColumnValue>> {
        override fun hasNext() = cursor.position < cursor.count - 1

        override fun next(): Array<ColumnValue> {
            cursor.moveToNext()
            return readColumnsArray(cursor)
        }
    }
}

class ColumnValue internal constructor(internal val value: Any?) {
    fun isNull(): Boolean = value == null

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> asTyped(type: Class<T>): T? = castValue(value, type) as T?

    fun asInt(defaultValue: Int = 0) = asTyped() ?: defaultValue

    fun asLong(defaultValue: Long = 0.toLong()) = asTyped() ?: defaultValue

    fun asShort(defaultValue: Short = 0.toShort()) = asTyped() ?: defaultValue

    fun asDouble(defaultValue: Double = 0.toDouble()) = asTyped() ?: defaultValue

    fun asFloat(defaultValue: Float = 0.toFloat()) = asTyped() ?: defaultValue

    fun asString(defaultValue: String = ""): String = asTyped() ?: defaultValue

    fun asBlob(defaultValue: ByteArray = byteArrayOf()) = asTyped() ?: defaultValue

    fun asBoolean(defaultValue: Boolean = false) = asTyped() ?: defaultValue

    override fun toString(): String = value.toString()
}

inline fun <reified T : Any> ColumnValue.asTyped(): T? = asTyped(T::class.java)

@Suppress("RemoveRedundantQualifierName")
private fun castValue(value: Any?, type: Class<*>): Any? {
    if (value == null && type.isPrimitive) {
        throw IllegalArgumentException("null can't be converted to the value of primitive type ${type.canonicalName}")
    }

    if (value == null || type == Any::class.java || type.isInstance(value)) {
        return value
    }

    if (type.isPrimitive && SQLitePrimitives.PRIMITIVES_TO_WRAPPERS[type] == value::class.java) {
        return value
    }

    if (value is Double && (type == java.lang.Float.TYPE || type == java.lang.Float::class.java)) {
        return value.toFloat()
    }

    if (value is Float && (type == java.lang.Double.TYPE || type == java.lang.Double::class.java)) {
        return value.toDouble()
    }

    if (value is Char && CharSequence::class.java.isAssignableFrom(type)) {
        return value.toString()
    }

    if (value is Long) {
        when (type) {
            java.lang.Integer.TYPE, java.lang.Integer::class.java -> return value.toInt()
            java.lang.Short.TYPE, java.lang.Short::class.java -> return value.toShort()
            java.lang.Byte.TYPE, java.lang.Byte::class.java -> return value.toByte()
            java.lang.Boolean.TYPE, java.lang.Boolean::class.java -> return value != 0L
            java.lang.Character.TYPE, java.lang.Character::class.java -> return value.toInt()
                .toChar()
        }
    }

    if (value is Int) {
        when (type) {
            java.lang.Long.TYPE, java.lang.Long::class.java -> return value.toLong()
            java.lang.Short.TYPE, java.lang.Short::class.java -> return value.toShort()
            java.lang.Byte.TYPE, java.lang.Byte::class.java -> return value.toByte()
            java.lang.Boolean.TYPE, java.lang.Boolean::class.java -> return value != 0L
            java.lang.Character.TYPE, java.lang.Character::class.java -> return value.toChar()
        }
    }

    if (value is String && value.length == 1
        && (type == java.lang.Character.TYPE || type == java.lang.Character::class.java)
    ) {
        return value.first()
    }

    if (value is ByteArray && (type == ByteArray::class.java)) {
        return value
    }

    if (value is String && (type == ByteArray::class.java)) {
        return value.toByteArray(StandardCharsets.UTF_8)
    }

    throw IllegalArgumentException("Value $value of type ${value::class.java} can't be cast to ${type.canonicalName}")
}