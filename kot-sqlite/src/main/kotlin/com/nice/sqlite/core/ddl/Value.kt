package com.nice.sqlite.core.ddl

data class Value(
    val column: Column<*>,
    val value: Any?
) : Sequence<Value> {

    override fun iterator(): Iterator<Value> = OnceIterator(this)

    operator fun plus(value: Value): MutableSequence<Value> =
        mutableSequenceOf(this, value)

    override fun toString(): String = "$column = $value"

}