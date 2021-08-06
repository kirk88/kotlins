@file:Suppress("unused")

package com.nice.sqlite.core

import com.nice.sqlite.core.ddl.Assignment
import com.nice.sqlite.core.ddl.Column
import com.nice.sqlite.core.ddl.SqlType
import com.nice.sqlite.core.ddl.surrounding
import com.nice.sqlite.core.dml.QueryStatement

open class Table(private val name: String) {

    val renderedName: String = name.surrounding()

    inner class BooleanColumn(name: String) : Column<Boolean>(name, SqlType.Integer, this)
    inner class IntColumn(name: String) : Column<Int>(name, SqlType.Integer, this)
    inner class LongColumn(name: String) : Column<Long>(name, SqlType.Integer, this)
    inner class ShortColumn(name: String) : Column<Short>(name, SqlType.Integer, this)
    inner class FloatColumn(name: String) : Column<Float>(name, SqlType.Real, this)
    inner class DoubleColumn(name: String) : Column<Double>(name, SqlType.Real, this)
    inner class StringColumn(name: String) : Column<String>(name, SqlType.Text, this)
    inner class BlobColumn(name: String) : Column<ByteArray>(name, SqlType.Blob, this)

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

enum class ViewType {
    None,
    Temp,
    Temporary;

    override fun toString(): String {
        return if (this == None) "" else name.uppercase()
    }
}

class View internal constructor(
    private val name: String,
    val type: ViewType,
    val statement: QueryStatement
) {

    val renderedName: String = name.surrounding()

    override fun toString(): String = name

}

fun view(
    name: String,
    type: ViewType = ViewType.None,
    statement: () -> QueryStatement
): View = View(name, type, statement())

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