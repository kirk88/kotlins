@file:Suppress("unused")

package com.nice.sqlite.core

import com.nice.sqlite.core.ddl.Column
import com.nice.sqlite.core.ddl.Renderer
import com.nice.sqlite.core.ddl.SqlType
import com.nice.sqlite.core.ddl.surrounding

abstract class Table(private val name: String) : Renderer {

    inner class BooleanColumn(name: String) : Column<Boolean>(this, name, SqlType.Integer)
    inner class IntColumn(name: String) : Column<Int>(this, name, SqlType.Integer)
    inner class LongColumn(name: String) : Column<Long>(this, name, SqlType.Integer)
    inner class ShortColumn(name: String) : Column<Short>(this, name, SqlType.Integer)
    inner class FloatColumn(name: String) : Column<Float>(this, name, SqlType.Real)
    inner class DoubleColumn(name: String) : Column<Double>(this, name, SqlType.Real)
    inner class StringColumn(name: String) : Column<String>(this, name, SqlType.Text)
    inner class BlobColumn(name: String) : Column<ByteArray>(this, name, SqlType.Blob)

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