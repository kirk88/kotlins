@file:Suppress("unused")

package com.easy.kotlins.sqlite.db

import android.database.Cursor
import android.database.sqlite.SQLiteException

fun interface RowParser<out T> {
    fun parseRow(row: Array<SqlColumnValue>): T
}

fun interface MapRowParser<out T> {
    fun parseRow(row: Map<String, SqlColumnValue>): T
}

private class SingleColumnParser<out T>(val modifier: (SqlColumnValue) -> T) : RowParser<T> {
    override fun parseRow(row: Array<SqlColumnValue>): T {
        if (row.size != 1)
            throw SQLiteException("Invalid row: row for SingleColumnParser must contain exactly one column")
        @Suppress("UNCHECKED_CAST")
        return modifier(row[0])
    }
}

val ShortParser: RowParser<Short> = SingleColumnParser(modifier = SqlColumnValue::asShort)
val IntParser: RowParser<Int> = SingleColumnParser(modifier = SqlColumnValue::asInt)
val LongParser: RowParser<Long> = SingleColumnParser(modifier = SqlColumnValue::asLong)
val FloatParser: RowParser<Float> = SingleColumnParser(modifier = SqlColumnValue::asFloat)
val DoubleParser: RowParser<Double> = SingleColumnParser(modifier = SqlColumnValue::asDouble)
val StringParser: RowParser<String> = SingleColumnParser(modifier = SqlColumnValue::asString)
val BlobParser: RowParser<ByteArray> = SingleColumnParser(modifier = SqlColumnValue::asBlob)

fun <T : Any> Cursor.parseSingle(parser: RowParser<T>): T = use {
    if (count != 1)
        throw SQLiteException("parseSingle accepts only cursors with a single entry")
    moveToFirst()
    return parser.parseRow(readColumnsArray(this))
}

fun <T : Any> Cursor.parseOpt(parser: RowParser<T>): T? = use {
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

fun <T : Any> Cursor.parseOpt(parser: MapRowParser<T>): T? = use {
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

fun Cursor.asSequence(): Sequence<Array<SqlColumnValue>> {
    return CursorSequence(this)
}

fun Cursor.asMapSequence(): Sequence<Map<String, SqlColumnValue>> {
    return CursorMapSequence(this)
}

fun Cursor.getColumnValue(index: Int): SqlColumnValue {
    val value = if (isNull(index)) null
    else when (getType(index)) {
        Cursor.FIELD_TYPE_INTEGER -> getLong(index)
        Cursor.FIELD_TYPE_FLOAT -> getDouble(index)
        Cursor.FIELD_TYPE_STRING -> getString(index)
        Cursor.FIELD_TYPE_BLOB -> getBlob(index)
        else -> null
    }
    return SqlColumnValue(value)
}

private fun readColumnsArray(cursor: Cursor): Array<SqlColumnValue> {
    val count = cursor.columnCount
    val list = ArrayList<SqlColumnValue>(count)
    for (i in 0 until count) {
        list.add(cursor.getColumnValue(i))
    }
    return list.toTypedArray()
}

private fun readColumnsMap(cursor: Cursor): Map<String, SqlColumnValue> {
    val count = cursor.columnCount
    val map = hashMapOf<String, SqlColumnValue>()
    for (i in 0 until count) {
        map[cursor.getColumnName(i)] = cursor.getColumnValue(i)
    }
    return map
}

private class CursorMapSequence(val cursor: Cursor) : Sequence<Map<String, SqlColumnValue>> {
    override fun iterator() = CursorMapIterator(cursor)
}

private class CursorSequence(val cursor: Cursor) : Sequence<Array<SqlColumnValue>> {
    override fun iterator() = CursorIterator(cursor)
}

private class CursorIterator(val cursor: Cursor) : Iterator<Array<SqlColumnValue>> {
    override fun hasNext() = cursor.position < cursor.count - 1

    override fun next(): Array<SqlColumnValue> {
        cursor.moveToNext()
        return readColumnsArray(cursor)
    }
}

private class CursorMapIterator(val cursor: Cursor) : Iterator<Map<String, SqlColumnValue>> {
    override fun hasNext() = cursor.position < cursor.count - 1

    override fun next(): Map<String, SqlColumnValue> {
        cursor.moveToNext()
        return readColumnsMap(cursor)
    }
}

class SqlColumnValue internal constructor(internal val value: Any?) {
    fun isNull(): Boolean = value == null

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> asTyped(type: Class<T>): T? = castValue(value, type) as T?

    fun asString(defaultValue: String = ""): String = value?.toString() ?: defaultValue

    fun asBlob(defaultValue: ByteArray = byteArrayOf()) = value as? ByteArray ?: defaultValue

    fun asLong(defaultValue: Long = 0.toLong()) = value as? Long ?: defaultValue

    fun asShort(defaultValue: Short = 0.toShort()) = (value as? Long)?.toShort() ?: defaultValue

    fun asInt(defaultValue: Int = 0) = (value as? Long)?.toInt() ?: defaultValue

    fun asDouble(defaultValue: Double = 0.toDouble()) = value as? Double ?: defaultValue

    fun asFloat(defaultValue: Float = 0.toFloat()) = (value as? Double)?.toFloat() ?: defaultValue

    override fun toString(): String {
        return "SqlColumnValue(value=$value)"
    }

}

inline fun <reified T : Any> SqlColumnValue.asTyped(): T? = asTyped(T::class.java)

@Suppress("RemoveRedundantQualifierName")
private fun castValue(value: Any?, type: Class<*>): Any? {
    if (value == null && type.isPrimitive) {
        throw IllegalArgumentException("null can't be converted to the value of primitive type ${type.canonicalName}")
    }

    if (value == null || type == Any::class.java || type.isInstance(value)) {
        return value
    }

    if (type.isPrimitive && JavaSqlitePrimitives.PRIMITIVES_TO_WRAPPERS[type] == value::class.java) {
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
            java.lang.Character.TYPE, java.lang.Character::class.java -> return value.toChar()
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
        return value[0]
    }

    if (value is ByteArray && (type == ByteArray::class.java)) {
        return value
    }

    throw IllegalArgumentException("Value $value of type ${value::class.java} can't be cast to ${type.canonicalName}")
}