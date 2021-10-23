package com.nice.sqlite.core.ddl

data class Value(
    val column: Column<*>,
    val value: Any?
) : Bag<Value> {

    override fun iterator(): Iterator<Value> = OnceIterator(this)

    operator fun plus(value: Value): MutableBag<Value> =
        mutableBagOf(this, value)

    override fun toString(): String = "$column = $value"

}