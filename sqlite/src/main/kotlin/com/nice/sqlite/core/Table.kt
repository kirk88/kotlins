@file:Suppress("unused")

package com.nice.sqlite.core

import com.nice.sqlite.core.ddl.Assignment
import com.nice.sqlite.core.ddl.Column
import com.nice.sqlite.core.ddl.SqlType
import com.nice.sqlite.core.ddl.surrounding

open class Table(private val name: String) {

    val renderedName: String = name.surrounding()

    inner class BooleanColumn(name: String) : Column<Boolean>(this, name, SqlType.Integer) {
        operator fun invoke(value: Boolean): Assignment {
            return Assignment.Value(this, value)
        }
    }

    inner class IntColumn(name: String) : Column<Int>(this, name, SqlType.Integer) {
        operator fun invoke(value: Int): Assignment {
            return Assignment.Value(this, value)
        }
    }

    inner class LongColumn(name: String) : Column<Long>(this, name, SqlType.Integer) {
        operator fun invoke(value: Long): Assignment {
            return Assignment.Value(this, value)
        }
    }

    inner class ShortColumn(name: String) : Column<Short>(this, name, SqlType.Integer) {
        operator fun invoke(value: Short): Assignment {
            return Assignment.Value(this, value)
        }
    }

    inner class FloatColumn(name: String) : Column<Float>(this, name, SqlType.Real) {
        operator fun invoke(value: Float): Assignment {
            return Assignment.Value(this, value)
        }
    }

    inner class DoubleColumn(name: String) : Column<Double>(this, name, SqlType.Real) {
        operator fun invoke(value: Double): Assignment {
            return Assignment.Value(this, value)
        }
    }

    inner class StringColumn(name: String) : Column<String>(this, name, SqlType.Text) {
        operator fun invoke(value: String?): Assignment {
            return Assignment.Value(this, value)
        }
    }

    inner class BlobColumn(name: String) : Column<ByteArray>(this, name, SqlType.Blob) {
        operator fun invoke(value: ByteArray?): Assignment {
            return Assignment.Value(this, value)
        }
    }

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