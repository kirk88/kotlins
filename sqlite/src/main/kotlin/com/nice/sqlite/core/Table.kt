package com.nice.sqlite.core

import com.nice.sqlite.core.dml.Assignment
import com.nice.sqlite.core.dml.Projection.Column

open class Table(private val name: String) {
    inner class BooleanColumn(name: String) : Column(name, this)
    inner class IntColumn(name: String) : Column(name, this)
    inner class LongColumn(name: String) : Column(name, this)
    inner class ShortColumn(name: String) : Column(name, this)
    inner class ByteColumn(name: String) : Column(name, this)
    inner class FloatColumn(name: String) : Column(name, this)
    inner class DoubleColumn(name: String) : Column(name, this)
    inner class StringColumn(name: String) : Column(name, this)
    inner class BlobColumn(name: String) : Column(name, this)

    override fun toString(): String {
        return name
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
}

operator fun Table.IntColumn.invoke(value: Int): Assignment {
    return Assignment.Value(this, value)
}

operator fun Table.LongColumn.invoke(value: Long): Assignment {
    return Assignment.Value(this, value)
}

operator fun Table.ShortColumn.invoke(value: Short): Assignment {
    return Assignment.Value(this, value)
}

operator fun Table.ByteColumn.invoke(value: Byte): Assignment {
    return Assignment.Value(this, value)
}

operator fun Table.FloatColumn.invoke(value: Float): Assignment {
    return Assignment.Value(this, value)
}

operator fun Table.DoubleColumn.invoke(value: Double): Assignment {
    return Assignment.Value(this, value)
}

operator fun Table.BooleanColumn.invoke(value: Boolean): Assignment {
    return Assignment.Value(this, value)
}

operator fun Table.StringColumn.invoke(value: String?): Assignment {
    return Assignment.Value(this, value)
}

operator fun Table.BlobColumn.invoke(value: ByteArray?): Assignment {
    return Assignment.Value(this, value)
}