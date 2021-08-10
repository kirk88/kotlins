@file:Suppress("unused")

package com.nice.sqlite.core

import com.nice.sqlite.core.ddl.*

open class Table(private val name: String) {

    val renderedName: String = name.surrounding()

    inner class BooleanColumn(name: String) : Column(this, name, SqlType.Integer) {
        operator fun invoke(value: Boolean): Assignment {
            return Assignment.Value(this, value)
        }
    }

    inner class IntColumn(name: String) : Column(this, name, SqlType.Integer) {
        operator fun invoke(value: Int): Assignment {
            return Assignment.Value(this, value)
        }
    }

    inner class LongColumn(name: String) : Column(this, name, SqlType.Integer) {
        operator fun invoke(value: Long): Assignment {
            return Assignment.Value(this, value)
        }
    }

    inner class ShortColumn(name: String) : Column(this, name, SqlType.Integer) {
        operator fun invoke(value: Short): Assignment {
            return Assignment.Value(this, value)
        }
    }

    inner class FloatColumn(name: String) : Column(this, name, SqlType.Real) {
        operator fun invoke(value: Float): Assignment {
            return Assignment.Value(this, value)
        }
    }

    inner class DoubleColumn(name: String) : Column(this, name, SqlType.Real) {
        operator fun invoke(value: Double): Assignment {
            return Assignment.Value(this, value)
        }
    }

    inner class StringColumn(name: String) : Column(this, name, SqlType.Text) {
        operator fun invoke(value: String?): Assignment {
            return Assignment.Value(this, value)
        }
    }

    inner class BlobColumn(name: String) : Column(this, name, SqlType.Blob) {
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

fun Table.IntColumn.default(value: Int): Table.IntColumn = apply {
    setMeta(defaultConstraint = ColumnConstraint.Default(value))
}

fun Table.LongColumn.default(value: Long): Table.LongColumn = apply {
    setMeta(defaultConstraint = ColumnConstraint.Default(value))
}

fun Table.ShortColumn.default(value: Short): Table.ShortColumn = apply {
    setMeta(defaultConstraint = ColumnConstraint.Default(value))
}

fun Table.FloatColumn.default(value: Float): Table.FloatColumn = apply {
    setMeta(defaultConstraint = ColumnConstraint.Default(value))
}

fun Table.DoubleColumn.default(value: Double): Table.DoubleColumn = apply {
    setMeta(defaultConstraint = ColumnConstraint.Default(value))
}

fun Table.BooleanColumn.default(value: Boolean): Table.BooleanColumn = apply {
    setMeta(defaultConstraint = ColumnConstraint.Default(value))
}

fun Table.StringColumn.default(value: String): Table.StringColumn = apply {
    setMeta(defaultConstraint = ColumnConstraint.Default(value))
}

fun Table.BlobColumn.default(value: ByteArray): Table.BlobColumn = apply {
    setMeta(defaultConstraint = ColumnConstraint.Default(value))
}
