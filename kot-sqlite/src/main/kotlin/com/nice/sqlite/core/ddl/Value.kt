package com.nice.sqlite.core.ddl

data class Value(
    val column: Column<*>,
    val value: Any?
) : Bag<Value> {

    override fun iterator(): Iterator<Value> = OnceIterator(this)

    override fun toString(): String = "$column = $value"

}