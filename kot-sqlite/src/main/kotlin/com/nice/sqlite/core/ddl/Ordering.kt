@file:Suppress("unused")

package com.nice.sqlite.core.ddl

enum class SqlOrderDirection { ASC, DESC }

interface Ordering : Sequence<Ordering>, FullRenderer {

    val definition: Definition
    val direction: SqlOrderDirection

    override fun iterator(): Iterator<Ordering> = OnceIterator(this)

    operator fun plus(ordering: Ordering): MutableSequence<Ordering> =
        mutableSequenceOf(this, ordering)

    override fun render(): String = "${definition.render()} $direction"

    override fun fullRender(): String = "${definition.fullRender()} $direction"

    data class By(
        override val definition: Definition,
        override val direction: SqlOrderDirection
    ) : Ordering {
        override fun toString(): String = "$definition $direction"
    }

}

val Definition.asc: Ordering
    get() = Ordering.By(this, SqlOrderDirection.ASC)
val Definition.desc: Ordering
    get() = Ordering.By(this, SqlOrderDirection.DESC)