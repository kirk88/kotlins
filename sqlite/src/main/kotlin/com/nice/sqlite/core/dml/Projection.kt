@file:Suppress("unused")

package com.nice.sqlite.core.dml

import com.nice.sqlite.core.Table

interface Projection : Sequence<Projection> {

    override fun iterator(): Iterator<Projection> = OnceIterator(this)

    abstract class Column(
        val name: String,
        val table: Table
    ) : Projection {

        val counter: Projection
            get() = Function("count", this)
        val maximum: Projection
            get() = Function("max", this)
        val minimum: Projection
            get() = Function("min", this)
        val average: Projection
            get() = Function("avg", this)
        val summation: Projection
            get() = Function("sum", this)
        val absolute: Projection
            get() = Function("abs", this)
        val upper: Projection
            get() = Function("upper", this)
        val lower: Projection
            get() = Function("lower", this)
        val length: Projection
            get() = Function("length", this)

        val asc: Ordering
            get() = Ordering.By(this, SqlOrderDirection.ASC)

        val desc: Ordering
            get() = Ordering.By(this, SqlOrderDirection.DESC)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Column

            if (name != other.name) return false
            if (table != other.table) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + table.hashCode()
            return result
        }

        override fun toString(): String {
            return name
        }

    }

    class Function(private val name: String, private val column: Column) : Projection {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Function

            if (name != other.name) return false
            if (column != other.column) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + column.hashCode()
            return result
        }

        override fun toString(): String = "$name(${column.name})"

    }

}

operator fun MutableSequence<Projection>.plus(projection: Projection): MutableSequence<Projection> =
    apply {
        add(projection)
    }

operator fun Projection.plus(projection: Projection): MutableSequence<Projection> =
    LinkedSequence<Projection>().also {
        it.add(this)
        it.add(projection)
    }