package com.nice.sqlite.core.ddl

import com.nice.sqlite.core.dml.MutableSequence
import com.nice.sqlite.core.dml.OnceIterator
import com.nice.sqlite.core.dml.mutableSequenceOf

interface Assignment : Sequence<Assignment> {

    val column: Column<*>
    val value: Any?

    override fun iterator(): Iterator<Assignment> = OnceIterator(this)

    operator fun plus(assignment: Assignment): MutableSequence<Assignment> =
        mutableSequenceOf(this, assignment)

    data class Value(override val column: Column<*>, override val value: Any?) : Assignment {
        override fun toString(): String = "$column = $value"
    }

}

class Assignments @PublishedApi internal constructor(
    private val assignments: Sequence<Assignment>
) : Sequence<Assignment> by assignments {

    internal val id: Int = toString().hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Assignments

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }

    override fun toString(): String = assignments.joinToString {
        it.column.name
    }

}

operator fun Column<Int>.invoke(value: Int): Assignment {
    return Assignment.Value(this, value)
}

operator fun Column<Long>.invoke(value: Long): Assignment {
    return Assignment.Value(this, value)
}

operator fun Column<Short>.invoke(value: Short): Assignment {
    return Assignment.Value(this, value)
}

operator fun Column<Float>.invoke(value: Float): Assignment {
    return Assignment.Value(this, value)
}

operator fun Column<Double>.invoke(value: Double): Assignment {
    return Assignment.Value(this, value)
}

operator fun Column<Boolean>.invoke(value: Boolean): Assignment {
    return Assignment.Value(this, value)
}

operator fun Column<String>.invoke(value: String?): Assignment {
    return Assignment.Value(this, value)
}

operator fun Column<ByteArray>.invoke(value: ByteArray?): Assignment {
    return Assignment.Value(this, value)
}