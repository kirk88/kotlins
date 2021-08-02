@file:Suppress("unused")

package com.nice.sqlite.core

import com.nice.sqlite.core.ddl.Assignment
import com.nice.sqlite.core.ddl.Column
import com.nice.sqlite.core.ddl.Renderer
import com.nice.sqlite.core.ddl.SqlType

open class Table(private val name: String) {

    internal val renderedName: String = "\"$name\""

    inner class BooleanColumn(name: String) : Column<Boolean>(name, SqlType.INTEGER, this)
    inner class IntColumn(name: String) : Column<Int>(name, SqlType.INTEGER, this)
    inner class LongColumn(name: String) : Column<Long>(name, SqlType.INTEGER, this)
    inner class ShortColumn(name: String) : Column<Short>(name, SqlType.INTEGER, this)
    inner class FloatColumn(name: String) : Column<Float>(name, SqlType.REAL, this)
    inner class DoubleColumn(name: String) : Column<Double>(name, SqlType.REAL, this)
    inner class StringColumn(name: String) : Column<String>(name, SqlType.TEXT, this)
    inner class BlobColumn(name: String) : Column<ByteArray>(name, SqlType.BLOB, this)

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

operator fun Column<Int>.invoke(value: Int): Assignment {
    return Assignment.Value(this, value)
}

operator fun Column<Long>.invoke(value: Long): Assignment {
    return Assignment.Value(this, value)
}

operator fun Column<Short>.invoke(value: Short): Assignment {
    return Assignment.Value(this, value)
}

operator fun Column<Float>.invoke(value: Float): Assignment {
    return Assignment.Value(this, value)
}

operator fun Column<Double>.invoke(value: Double): Assignment {
    return Assignment.Value(this, value)
}

operator fun Column<Boolean>.invoke(value: Boolean): Assignment {
    return Assignment.Value(this, value)
}

operator fun Column<String>.invoke(value: String?): Assignment {
    return Assignment.Value(this, value)
}

operator fun Column<ByteArray>.invoke(value: ByteArray?): Assignment {
    return Assignment.Value(this, value)
}