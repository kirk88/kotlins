@file:Suppress("unused")

package com.nice.sqlite.core.ddl

import com.nice.sqlite.core.dml.MutableSequence
import com.nice.sqlite.core.dml.OnceIterator
import com.nice.sqlite.core.dml.mutableSequenceOf

enum class SqlOrderDirection { ASC, DESC }

interface Ordering : Sequence<Ordering>, Renderer {

    val column: Column
    val direction: SqlOrderDirection

    override fun iterator(): Iterator<Ordering> = OnceIterator(this)

    operator fun plus(ordering: Ordering): MutableSequence<Ordering> =
        mutableSequenceOf(this, ordering)

    override fun render(): String = "${column.render()} $direction"

    override fun fullRender(): String = "${column.fullRender()} $direction"

    data class By(override val column: Column, override val direction: SqlOrderDirection) : Ordering {
        override fun toString(): String = "$column $direction"
    }

}

fun asc(column: Column): Ordering = Ordering.By(column, SqlOrderDirection.ASC)
fun desc(column: Column): Ordering = Ordering.By(column, SqlOrderDirection.DESC)