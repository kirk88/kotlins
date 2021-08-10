@file:Suppress("unused")

package com.nice.sqlite

import android.database.Cursor
import android.database.sqlite.SQLiteException

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

fun <T : Any> Cursor.parseSingle(parser: RowParser<T>): T = use {
    if (count != 1)
        throw SQLiteException("parseSingle accepts only cursors with a single entry")
    moveToFirst()
    return parser.parseRow(readColumnsArray(this))
}

fun <T : Any> Cursor.parseSingleOrNull(parser: RowParser<T>): T? = use {
    if (count > 1)
        throw SQLiteException("parseSingle accepts only cursors with a single entry or empty cursors")
    if (count == 0)
        return null
    moveToFirst()
    return parser.parseRow(readColumnsArray(this))
}

fun <T : Any> Cursor.parseList(parser: RowParser<T>): List<T> = use {
    val list = ArrayList<T>(count)
    moveToFirst()
    while (!isAfterLast) {
        list.add(parser.parseRow(readColumnsArray(this)))
        moveToNext()
    }
    return list
}

fun <T : Any> Cursor.parseSingle(parser: MapRowParser<T>): T = use {
    if (count != 1)
        throw SQLiteException("parseSingle accepts only cursors with getCount() == 1")
    moveToFirst()
    return parser.parseRow(readColumnsMap(this))
}

fun <T : Any> Cursor.parseSingleOrNull(parser: MapRowParser<T>): T? = use {
    if (count > 1)
        throw SQLiteException("parseSingle accepts only cursors with getCount() == 1 or empty cursors")
    if (count == 0)
        return null
    moveToFirst()
    return parser.parseRow(readColumnsMap(this))
}

fun <T : Any> Cursor.parseList(parser: MapRowParser<T>): List<T> = use {
    val list = ArrayList<T>(count)
    moveToFirst()
    while (!isAfterLast) {
        list.add(parser.parseRow(readColumnsMap(this)))
        moveToNext()
    }
    return list
}

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

    fun asInt(defaultValue: Int = 0) = (value as? Long)?.toInt() ?: defaultValue

    fun asLong(defaultValue: Long = 0.toLong()) = value as? Long ?: defaultValue

    fun asShort(defaultValue: Short = 0.toShort()) = (value as? Long)?.toShort() ?: defaultValue

    fun asDouble(defaultValue: Double = 0.toDouble()) = value as? Double ?: defaultValue

    fun asFloat(defaultValue: Float = 0.toFloat()) = (value as? Double)?.toFloat() ?: defaultValue

    fun asString(defaultValue: String = ""): String = value?.toString() ?: defaultValue

    fun asBlob(defaultValue: ByteArray = byteArrayOf()) = value as? ByteArray ?: defaultValue

    override fun toString(): String {
        return value.toString()
    }
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
        return value.encodeToByteArray()
    }

    throw IllegalArgumentException("Value $value of type ${value::class.java} can't be cast to ${type.canonicalName}")
}