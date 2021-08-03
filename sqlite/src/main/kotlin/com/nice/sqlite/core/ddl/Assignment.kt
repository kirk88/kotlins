package com.nice.sqlite.core.ddl

import com.nice.sqlite.core.Table
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

class Assignments @PublishedApi internal constructor(
    private val assignments: Sequence<Assignment>
) : Sequence<Assignment> by assignments {

    val id: Int = toString().hashCode()

    override fun toString(): String = assignments.joinToString {
        it.column.name
    }

}

class BatchAssignmentsBuilder<T : Table> @PublishedApi internal constructor(
    @PublishedApi internal val table: T
) : MutableSequence<Assignments> by mutableSequenceOf() {

    inline fun assignments(assignments: (T) -> Sequence<Assignment>): Assignments =
        Assignments(assignments(table)).also { add(it) }

}


internal fun Any?.toSqlString(): String {
    return when (this) {
        null -> "NULL"
        is Number -> toString()
        is Boolean -> if (this) "1" else "0"
        else -> "'${toString().replace("'", "''")}'"
    }
}