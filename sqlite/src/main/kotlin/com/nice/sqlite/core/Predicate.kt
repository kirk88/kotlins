@file:Suppress("unused")

package com.nice.sqlite.core

import com.nice.sqlite.core.ddl.Column
import com.nice.sqlite.core.ddl.Statement
import com.nice.sqlite.core.ddl.toSqlString
import com.nice.sqlite.core.dml.QueryStatement

interface Predicate {

    fun render(dialect: Dialect): String

    fun fullRender(dialect: Dialect): String

}

private class Condition(
    private val predicateLeft: Predicate,
    private val connector: String,
    private val predicateRight: Predicate
) : Predicate {

    override fun render(dialect: Dialect): String =
        "(${predicateLeft.render(dialect)} $connector ${predicateRight.render(dialect)})"

    override fun fullRender(dialect: Dialect): String =
        "(${predicateLeft.fullRender(dialect)} $connector ${predicateRight.fullRender(dialect)})"

}

infix fun Predicate.and(predicate: Predicate): Predicate {
    return Condition(this, "AND", predicate)
}

infix fun Predicate.or(predicate: Predicate): Predicate {
    return Condition(this, "OR", predicate)
}

private fun predicate(
    rendered: (dialect: Dialect, full: Boolean) -> String
): Predicate {
    return object : Predicate {
        override fun render(dialect: Dialect): String = rendered(dialect, false)

        override fun fullRender(dialect: Dialect): String = rendered(dialect, true)
    }
}

infix fun Column<*>.eq(value: Any): Predicate =
    predicate { dialect, full -> "${render(it)} = ?" }

infix fun Column<*>.ne(value: Any): Predicate =
    predicate { dialect, full -> "${render(it)} <> ?" }

infix fun Column<*>.gt(value: Any): Predicate =
    predicate { dialect, full -> "${render(it)} > ?" }

infix fun Column<*>.lt(value: Any): Predicate =
    predicate { dialect, full -> "${render(it)} < ?" }

infix fun Column<*>.gte(value: Any): Predicate =
    predicate { dialect, full -> "${render(it)} >= ?" }

infix fun Column<*>.lte(value: Any): Predicate =
    predicate { dialect, full -> "${render(it)} <= ?" }

infix fun Column<*>.like(value: Any): Predicate =
    predicate { dialect, full -> "${render(it)} LIKE ?" }

infix fun Column<*>.glob(value: Any): Predicate =
    predicate { dialect, full -> "${render(it)} GLOB ?" }

fun Column<*>.isNotNull(): Predicate =
    predicate { dialect, full -> "${render(it)} IS NOT NULL" }

fun Column<*>.isNull(): Predicate =
    predicate { dialect, full -> "${render(it)} IS NULL" }

fun Column<*>.between(value1: Any, value2: Any): Predicate =
    predicate { dialect, full -> "${render(it)} BETWEEN ? AND ?" }

fun Column<*>.notBetween(value1: Any, value2: Any): Predicate =
    predicate { dialect, full -> "${render(it)} NOT BETWEEN ? AND ?" }

fun Column<*>.any(vararg values: Any): Predicate =
    predicate { dialect, full ->
        values.joinToString(
            prefix = "${render(dialect, full)} IN (",
            postfix = ")"
        ) { it.render(dialect, full) }
    }

fun Column<*>.none(vararg values: Any): Predicate =
    predicate { dialect, full ->
        values.joinToString(
            prefix = "${render(dialect, full)} NOT IN (",
            postfix = ")"
        ) { it.render(dialect, full) }
    }

fun exists(statement: QueryStatement): Predicate =
    predicate { dialect, full -> "EXISTS ${statement.render(dialect, full)}" }

private fun Any.render(dialect: Dialect, full: Boolean) = when (this) {
    is Column<*> -> if (full) fullRender() else render()
    is Statement -> "(${toString(dialect)})"
    else -> toSqlString()
}

private fun String.format(dialect: Dialect, args: Array<out Any>): String {
    val builder = StringBuilder(this)
    var offset = builder.indexOf("?")
    for (value in args) {
        builder.replace(offset, offset + 1, value.render(dialect))
        offset = builder.indexOf("?", offset)
    }
    return builder.toString()
}