package com.nice.sqlite.core.dml

import com.nice.sqlite.core.escapedSQLString

interface Assignment : Sequence<Assignment> {

    val column: Projection.Column
    val value: Any?

    override fun iterator(): Iterator<Assignment> = OnceIterator(this)

    operator fun plus(assignment: Assignment): MutableSequence<Assignment> =
        mutableSequenceOf(this, assignment)

    fun render(fullFormat: Boolean = false): String =
        "${column.render(fullFormat)} = ${value.escapedSQLString()}"

    class Value(override val column: Projection.Column, override val value: Any?) : Assignment {
        override fun toString(): String = "$column = $value"
    }

}