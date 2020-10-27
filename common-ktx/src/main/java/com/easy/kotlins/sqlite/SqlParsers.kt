/*
 * Copyright 2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("unused")
package com.easy.kotlins.sqlite.db

import android.database.Cursor
import android.database.sqlite.SQLiteException
import java.util.*

interface RowParser<out T> {
    fun parseRow(columns: Array<Any?>): T
}

interface MapRowParser<out T> {
    fun parseRow(columns: Map<String, ColumnValue>): T
}

private class SingleColumnParser<out T> : RowParser<T> {
    override fun parseRow(columns: Array<Any?>): T {
        if (columns.size != 1)
            throw SQLiteException("Invalid row: row for SingleColumnParser must contain exactly one column")
        @Suppress("UNCHECKED_CAST")
        return columns[0] as T
    }
}

private class ScalarColumnParser<in R, out T>(val modifier: ((R) -> T)? = null) : RowParser<T> {
    override fun parseRow(columns: Array<Any?>): T {
        if (columns.size != 1)
            throw SQLiteException("Invalid row: row for SingleColumnParser must contain exactly one column")
        @Suppress("UNCHECKED_CAST", "UNNECESSARY_NOT_NULL_ASSERTION")
        return if (modifier != null)
            modifier!!(columns[0] as R)
        else
            columns[0] as T
    }
}

val ShortParser: RowParser<Short> = ScalarColumnParser(modifier = Long::toShort)
val IntParser: RowParser<Int> = ScalarColumnParser(modifier = Long::toInt)
val LongParser: RowParser<Long> = SingleColumnParser()
val FloatParser: RowParser<Float> = ScalarColumnParser(modifier = Double::toFloat)
val DoubleParser: RowParser<Double> = SingleColumnParser()
val StringParser: RowParser<String> = SingleColumnParser()
val BlobParser: RowParser<ByteArray> = SingleColumnParser()

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

@Deprecated("Use asSequence() instead", ReplaceWith("asSequence()"))
fun Cursor.sequence(): Sequence<Array<Any?>> {
    return CursorSequence(this)
}

@Deprecated("Use asMapSequence() instead", ReplaceWith("asMapSequence()"))
fun Cursor.mapSequence(): Sequence<Map<String, Any?>> {
    return CursorMapSequence(this)
}

fun Cursor.asSequence(): Sequence<Array<Any?>> {
    return CursorSequence(this)
}

fun Cursor.asMapSequence(): Sequence<Map<String, Any?>> {
    return CursorMapSequence(this)
}

private fun Cursor.getValue(index: Int): Any? {
    if (isNull(index)) return null

    return when (getType(index)) {
        Cursor.FIELD_TYPE_INTEGER -> getLong(index)
        Cursor.FIELD_TYPE_FLOAT -> getDouble(index)
        Cursor.FIELD_TYPE_STRING -> getString(index)
        Cursor.FIELD_TYPE_BLOB -> getBlob(index)
        else -> null
    }
}

private fun Cursor.getColumnValue(index: Int): ColumnValue {
    return ColumnValue(this, index)
}

private fun readColumnsArray(cursor: Cursor): Array<Any?> {
    val count = cursor.columnCount
    val arr = arrayOfNulls<Any?>(count)
    for (i in 0 until count) {
        arr[i] = cursor.getValue(i)
    }
    return arr
}
private fun readColumnsMap(cursor: Cursor): Map<String, ColumnValue> {
    val count = cursor.columnCount
    val map = hashMapOf<String, ColumnValue>()
    for (i in 0 until count) {
        map[cursor.getColumnName(i)] = cursor.getColumnValue(i)
    }
    return map
}

private class CursorMapSequence(val cursor: Cursor) : Sequence<Map<String, Any?>> {
    override fun iterator() = CursorMapIterator(cursor)
}

private class CursorSequence(val cursor: Cursor) : Sequence<Array<Any?>> {
    override fun iterator() = CursorIterator(cursor)
}

private class CursorIterator(val cursor: Cursor) : Iterator<Array<Any?>> {
    override fun hasNext() = cursor.position < cursor.count - 1

    override fun next(): Array<Any?> {
        cursor.moveToNext()
        return readColumnsArray(cursor)
    }
}

private class CursorMapIterator(val cursor: Cursor) : Iterator<Map<String, Any?>> {
    override fun hasNext() = cursor.position < cursor.count - 1

    override fun next(): Map<String, Any?> {
        cursor.moveToNext()
        return readColumnsMap(cursor)
    }
}

class ColumnValue(private val cursor: Cursor, private val index: Int) {
    val isNull: Boolean = cursor.isNull(index)

    val stringValue: String?
        get() = cursor.getString(index)

    val shortValue: Short
        get() = cursor.getShort(index)

    val intValue: Int
        get() = cursor.getInt(index)

    val longValue: Long
        get() = cursor.getLong(index)

    val floatValue: Float
        get() = cursor.getFloat(index)

    val doubleValue: Double
        get() = cursor.getDouble(index)

    val blobValue: ByteArray
        get() = cursor.getBlob(index)
}