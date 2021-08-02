@file:Suppress("unused")

package com.nice.sqlite.core.ddl

import com.nice.sqlite.core.dml.MutableSequence
import com.nice.sqlite.core.dml.OnceIterator
import com.nice.sqlite.core.dml.mutableSequenceOf

enum class SqlOrderDirection { ASC, DESC }

interface Ordering : Sequence<Ordering>, Renderer {

    val column: Column<*>
    val direction: SqlOrderDirection

    override fun iterator(): Iterator<Ordering> = OnceIterator(this)

    operator fun plus(ordering: Ordering): MutableSequence<Ordering> =
        mutableSequenceOf(this, ordering)

    override fun render(): String = "${column.renderedName} $direction"

    override fun fullRender(): String = "${column.fullRenderedName} $direction"

    class By(override val column: Column<*>, override val direction: SqlOrderDirection) :
        Ordering {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as By

            if (column != other.column) return false
            if (direction != other.direction) return false

            return true
        }

        override fun hashCode(): Int {
            var result = column.hashCode()
            result = 31 * result + direction.hashCode()
            return result
        }

        override fun toString(): String = "$column $direction"

    }

}

fun asc(column: Column<*>): Ordering = Ordering.By(column, SqlOrderDirection.ASC)
fun desc(column: Column<*>): Ordering = Ordering.By(column, SqlOrderDirection.DESC)