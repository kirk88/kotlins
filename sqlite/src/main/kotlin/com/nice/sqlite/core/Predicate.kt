@file:Suppress("unused")

package com.nice.sqlite.core

import com.nice.sqlite.core.dml.Projection

interface Predicate {

    fun render(fullFormat: Boolean = false): String

}

interface Condition : Predicate {

    val predicateLeft: Predicate
    val connector: String
    val predicateRight: Predicate

}

private fun Predicate(render: (Boolean) -> String): Predicate {
    return object : Predicate {
        override fun render(fullFormat: Boolean): String = render(fullFormat)
    }
}

private class ConditionImpl(
    override val predicateLeft: Predicate,
    override val connector: String,
    override val predicateRight: Predicate
) : Condition {

    override fun render(fullFormat: Boolean): String {
        return "${predicateLeft.render(fullFormat)} $connector ${predicateRight.render(fullFormat)}"
    }

}

infix fun Predicate.and(predicate: Predicate): Predicate {
    return ConditionImpl(this, "AND", predicate)
}

infix fun Predicate.or(predicate: Predicate): Predicate {
    return ConditionImpl(this, "OR", predicate)
}

infix fun Projection.Column.eq(value: Any): Predicate =
    Predicate { "${render(it)} = ${value.escapedSQLString()}" }

infix fun Projection.Column.ne(value: Any): Predicate =
    Predicate { "${render(it)} <> ${value.escapedSQLString()}" }

infix fun Projection.Column.gt(value: Any): Predicate =
    Predicate { "${render(it)} > ${value.escapedSQLString()}" }

infix fun Projection.Column.lt(value: Any): Predicate =
    Predicate { "${render(it)} < ${value.escapedSQLString()}" }

infix fun Projection.Column.gte(value: Any): Predicate =
    Predicate { "${render(it)} >= ${value.escapedSQLString()}" }

infix fun Projection.Column.lte(value: Any): Predicate =
    Predicate { "${render(it)} <= ${value.escapedSQLString()}" }

infix fun Projection.Column.like(value: Any): Predicate =
    Predicate { "${render(it)} LIKE ${value.escapedSQLString()}" }

infix fun Projection.Column.glob(value: Any): Predicate =
    Predicate { "${render(it)} GLOB ${value.escapedSQLString()}" }

fun Projection.Column.notNull(): Predicate =
    Predicate { "${render(it)} IS NOT NULL" }

fun Projection.Column.isNull(): Predicate =
    Predicate { "${render(it)} IS NULL" }

fun Projection.Column.between(value1: Any, value2: Any): Predicate =
    Predicate { "${render(it)} BETWEEN ${value1.escapedSQLString()} AND ${value2.escapedSQLString()}" }

fun Projection.Column.notBetween(value1: Any, value2: Any): Predicate =
    Predicate { "${render(it)} NOT BETWEEN ${value1.escapedSQLString()} AND ${value2.escapedSQLString()}" }

fun Projection.Column.any(vararg values: Any): Predicate =
    Predicate {
        values.joinToString(
            prefix = "${render(it)} IN (",
            postfix = ")"
        ) { value -> value.escapedSQLString() }
    }

fun Projection.Column.none(vararg values: Any): Predicate =
    Predicate {
        values.joinToString(
            prefix = "${render(it)} NOT IN (",
            postfix = ")"
        ) { value -> value.escapedSQLString() }
    }

internal fun Any?.escapedSQLString(): String {
    return when (this) {
        null -> "NULL"
        is Number -> toString()
        is Boolean -> if (this) "1" else "0"
        else -> "'${toString().replace("'", "''")}'"
    }
}