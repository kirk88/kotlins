@file:Suppress("UNUSED")

package com.nice.sqlite.core.ddl

enum class SqlOrderDirection { ASC, DESC }

interface Ordering : Shell<Ordering>, FullRenderer {

    val definition: Definition
    val direction: SqlOrderDirection

    override val size: Int get() = 1
    override fun iterator(): Iterator<Ordering> = OnceIterator(this)

    override fun render(): String = "${definition.render()} $direction"

    override fun fullRender(): String = "${definition.fullRender()} $direction"

    data class By(
        override val definition: Definition,
        override val direction: SqlOrderDirection
    ) : Ordering {
        override fun toString(): String = "$definition $direction"
    }

}

fun asc(definition: Definition): Ordering = Ordering.By(definition, SqlOrderDirection.ASC)
fun desc(definition: Definition): Ordering = Ordering.By(definition, SqlOrderDirection.DESC)