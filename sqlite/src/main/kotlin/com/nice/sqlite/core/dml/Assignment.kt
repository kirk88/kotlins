package com.nice.sqlite.core.dml

import com.nice.sqlite.core.escapedSQLString

interface Assignment : Sequence<Assignment> {

    val column: Projection.Column
    val value: Any?

    override fun iterator(): Iterator<Assignment> = OnceIterator(this)

    fun render(fullFormat: Boolean = false): String =
        "${column.render(fullFormat)} = ${value.escapedSQLString()}"

    class Value(override val column: Projection.Column, override val value: Any?) : Assignment {

        override fun toString(): String = "$column = $value"

    }

}

operator fun MutableSequence<Assignment>.plus(assignment: Assignment): MutableSequence<Assignment> =
    apply {
        add(assignment)
    }

operator fun Assignment.plus(assignment: Assignment): MutableSequence<Assignment> =
    LinkedSequence<Assignment>().also {
        it.add(this)
        it.add(assignment)
    }