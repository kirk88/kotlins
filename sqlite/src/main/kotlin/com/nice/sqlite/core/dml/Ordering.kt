package com.nice.sqlite.core.dml

enum class SqlOrderDirection { ASC, DESC }

interface Ordering : Sequence<Ordering> {

    val column: Projection.Column
    val direction: SqlOrderDirection

    override fun iterator(): Iterator<Ordering> = OnceIterator(this)

    operator fun plus(ordering: Ordering): MutableSequence<Ordering> =
        mutableSequenceOf(this, ordering)

    fun render(fullFormat: Boolean = false): String = "${column.render(fullFormat)} $direction"

    class By(override val column: Projection.Column, override val direction: SqlOrderDirection) :
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
