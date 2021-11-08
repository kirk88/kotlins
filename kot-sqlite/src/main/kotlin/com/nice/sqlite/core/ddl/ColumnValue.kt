package com.nice.sqlite.core.ddl

data class ColumnValue(
    val column: Column<*>,
    val value: Any?
) : Bag<ColumnValue> {

    override val size: Int get() = 1
    override fun iterator(): Iterator<ColumnValue> = OnceIterator(this)

    override fun toString(): String = "$column = $value"

}