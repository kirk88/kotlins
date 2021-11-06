package com.nice.sqlite.core.ddl

data class Assignment(
    val column: Column<*>,
    val value: Any?
) : Bag<Assignment> {

    override val size: Int get() = 1
    override fun iterator(): Iterator<Assignment> = OnceIterator(this)

    override fun toString(): String = "$column = $value"

}