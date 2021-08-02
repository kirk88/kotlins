package com.nice.sqlite.core.ddl

import com.nice.sqlite.core.dml.MutableSequence
import com.nice.sqlite.core.dml.OnceIterator
import com.nice.sqlite.core.dml.mutableSequenceOf
import com.nice.sqlite.core.render
import com.nice.sqlite.core.toSqlString

interface Assignment : Sequence<Assignment>, Renderer {

    val column: Column<*>
    val value: Any?

    override fun iterator(): Iterator<Assignment> = OnceIterator(this)

    operator fun plus(assignment: Assignment): MutableSequence<Assignment> =
        mutableSequenceOf(this, assignment)

    override fun render(): String = buildString {
        append(column.name.render())
        append(" = ")
        append(value.toSqlString())
    }

    override fun fullRender(): String = buildString {
        append(column.table.render())
        append('.')
        append(column.name.render())
        append(" = ")
        append(value.toSqlString())
    }

    class Value(override val column: Column<*>, override val value: Any?) : Assignment {
        override fun toString(): String = "$column = $value"
    }

}