package com.nice.sqlite.core.ddl

import com.nice.sqlite.core.dml.MutableSequence
import com.nice.sqlite.core.dml.OnceIterator
import com.nice.sqlite.core.dml.mutableSequenceOf

data class Value(
    val column: Column<*>,
    val value: Any?
) : Sequence<Value> {

    override fun iterator(): Iterator<Value> = OnceIterator(this)

    operator fun plus(value: Value): MutableSequence<Value> =
        mutableSequenceOf(this, value)

    override fun toString(): String = "$column = $value"

}

operator fun Column<Int>.invoke(value: Int): Value {
    return Value(this, value)
}

operator fun Column<Long>.invoke(value: Long): Value {
    return Value(this, value)
}

operator fun Column<Short>.invoke(value: Short): Value {
    return Value(this, value)
}

operator fun Column<Float>.invoke(value: Float): Value {
    return Value(this, value)
}

operator fun Column<Double>.invoke(value: Double): Value {
    return Value(this, value)
}

operator fun Column<Boolean>.invoke(value: Boolean): Value {
    return Value(this, value)
}

operator fun Column<String>.invoke(value: String?): Value {
    return Value(this, value)
}

operator fun Column<ByteArray>.invoke(value: ByteArray?): Value {
    return Value(this, value)
}