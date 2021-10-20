@file:Suppress("unused")

package com.nice.sqlite.core

import com.nice.sqlite.core.ddl.Column
import com.nice.sqlite.core.ddl.Renderer
import com.nice.sqlite.core.ddl.SqlType
import com.nice.sqlite.core.ddl.surrounding

abstract class Table(private val name: String) : Renderer {

    inner class IntColumn(name: String) : Column<Int>(name, SqlType.Integer, this)
    inner class LongColumn(name: String) : Column<Long>(name, SqlType.Integer, this)
    inner class ShortColumn(name: String) : Column<Short>(name, SqlType.Integer, this)
    inner class FloatColumn(name: String) : Column<Float>(name, SqlType.Real, this)
    inner class DoubleColumn(name: String) : Column<Double>(name, SqlType.Real, this)
    inner class StringColumn(name: String) : Column<String>(name, SqlType.Text, this)
    inner class BlobColumn(name: String) : Column<ByteArray>(name, SqlType.Blob, this)
    inner class BooleanColumn(name: String) : Column<Boolean>(name, SqlType.Integer, this)
    inner class DateColumn(name: String) : Column<String>(name, SqlType.Named("date"), this)
    inner class TimeColumn(name: String) : Column<String>(name, SqlType.Named("time"), this)
    inner class DatetimeColumn(name: String) : Column<String>(name, SqlType.Named("datetime"), this)
    inner class TimestampColumn(name: String) : Column<String>(name, SqlType.Named("timestamp"), this)

    override fun render(): String = name.surrounding()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Table

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String = name

}