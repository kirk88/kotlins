package com.nice.sqlite.core.dml

interface Assignment : Sequence<Assignment> {

    val column: Projection.Column
    val value: Any?

    override fun iterator(): Iterator<Assignment> = OnceIterator(this)

    class Value(override val column: Projection.Column, override val value: Any?) : Assignment

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