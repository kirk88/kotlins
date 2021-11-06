@file:Suppress("UNUSED")

package com.nice.sqlite

import android.database.Cursor
import android.database.sqlite.SQLiteException
import java.nio.charset.StandardCharsets

fun interface RowParser<out T> {
    fun parse(row: Row): T
}

private class SingleColumnParser<out T>(val modifier: (ColumnElement) -> T) : RowParser<T> {
    override fun parse(row: Row): T {
        if (row.size != 1)
            throw SQLiteException("Invalid row: row for SingleColumnParser must contain exactly one column")
        @Suppress("UNCHECKED_CAST")
        return modifier(row[0])
    }
}

val ShortParser: RowParser<Short> = SingleColumnParser(modifier = ColumnElement::asShort)
val IntParser: RowParser<Int> = SingleColumnParser(modifier = ColumnElement::asInt)
val LongParser: RowParser<Long> = SingleColumnParser(modifier = ColumnElement::asLong)
val FloatParser: RowParser<Float> = SingleColumnParser(modifier = ColumnElement::asFloat)
val DoubleParser: RowParser<Double> = SingleColumnParser(modifier = ColumnElement::asDouble)
val StringParser: RowParser<String> = SingleColumnParser(modifier = ColumnElement::asString)
val BlobParser: RowParser<ByteArray> = SingleColumnParser(modifier = ColumnElement::asBlob)

fun <T : Any> Cursor.single(parser: RowParser<T>): T = use {
    if (it.count != 1)
        throw SQLiteException("single accepts only cursors with getCount() == 1")
    it.moveToFirst()
    return parser.parse(it.readRow())
}

fun <T : Any> Cursor.singleOrNull(parser: RowParser<T>): T? = use {
    if (it.count > 1)
        throw SQLiteException("singleOrNull accepts only cursors with getCount() == 1 or empty cursors")
    if (it.count == 0)
        return null
    it.moveToFirst()
    return parser.parse(it.readRow())
}

fun <T : Any> Cursor.toList(parser: RowParser<T>): List<T> = use {
    val list = ArrayList<T>(it.count)
    it.moveToFirst()
    while (!it.isAfterLast) {
        list.add(parser.parse(it.readRow()))
        it.moveToNext()
    }
    return list
}

inline fun <reified T : Any> Cursor.single(): T = single(classParser())

inline fun <reified T : Any> Cursor.singleOrNull(): T? = singleOrNull(classParser())

inline fun <reified T : Any> Cursor.toList(): List<T> = toList(classParser())

fun Cursor.rowSequence(): Sequence<Row> = CursorRowSequence(this).constrainOnce()

private fun Cursor.readRow(): Row {
    val size = columnCount
    val elements = ArrayList<ColumnElement>(size)
    for (index in 0 until size) {
        elements.add(getColumnElement(index))
    }
    return Row(elements)
}

private fun Cursor.getColumnElement(index: Int): ColumnElement {
    val name = getColumnName(index)
    val value = if (isNull(index)) null
    else when (getType(index)) {
        Cursor.FIELD_TYPE_INTEGER -> getLong(index)
        Cursor.FIELD_TYPE_FLOAT -> getDouble(index)
        Cursor.FIELD_TYPE_STRING -> getString(index)
        Cursor.FIELD_TYPE_BLOB -> getBlob(index)
        else -> null
    }
    return ColumnElement(name, value)
}

private class CursorRowSequence(val cursor: Cursor) : Sequence<Row> {
    override fun iterator() = object : Iterator<Row> {
        override fun hasNext() = cursor.position < cursor.count - 1

        override fun next(): Row {
            cursor.moveToNext()
            return cursor.readRow()
        }
    }
}


class Row internal constructor(
    private val elements: List<ColumnElement>
) : Iterable<ColumnElement> {

    val size: Int = elements.size

    override fun iterator(): Iterator<ColumnElement> = elements.iterator()

    operator fun get(index: Int): ColumnElement = elements[index]

    operator fun get(name: String): ColumnElement = elements.first { it.name == name }

    override fun toString(): String = elements.toString()

}

class ColumnElement internal constructor(
    val name: String,
    val value: Any?
) {
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

    override fun toString(): String = "$name=$value"
}

operator fun ColumnElement.component1(): String = name
operator fun ColumnElement.component2(): Any? = value

inline fun <reified T : Any> ColumnElement.asTyped(): T? = asTyped(T::class.java)

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