package com.nice.sqlite.core.ddl

import com.nice.sqlite.core.dml.MutableSequence
import com.nice.sqlite.core.dml.OnceIterator
import com.nice.sqlite.core.dml.mutableSequenceOf

interface Assignment : Sequence<Assignment>, Renderer {

    val column: Column<*>
    val value: Any?

    override fun iterator(): Iterator<Assignment> = OnceIterator(this)

    operator fun plus(assignment: Assignment): MutableSequence<Assignment> =
        mutableSequenceOf(this, assignment)

    override fun render(): String = "${column.renderedName} = ${value.toSqlString()}"

    override fun fullRender(): String = "${column.fullRenderedName} = ${value.toSqlString()}"

    class Value(override val column: Column<*>, override val value: Any?) : Assignment {
        override fun toString(): String = "$column = $value"
    }

}

internal fun Any?.toSqlString(): String {
    return when (this) {
        null -> "NULL"
        is Number -> toString()
        is Boolean -> if (this) "1" else "0"
        else -> "'${toString().replace("'", "''")}'"
    }
}