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
    predicate { dialect, full -> "${render(full)} = ${value.render(dialect, full)}" }

infix fun Column<*>.ne(value: Any): Predicate =
    predicate { dialect, full -> "${render(full)} <> ${value.render(dialect, full)}" }

infix fun Column<*>.gt(value: Any): Predicate =
    predicate { dialect, full -> "${render(full)} > ${value.render(dialect, full)}" }

infix fun Column<*>.lt(value: Any): Predicate =
    predicate { dialect, full -> "${render(full)} < ${value.render(dialect, full)}" }

infix fun Column<*>.gte(value: Any): Predicate =
    predicate { dialect, full -> "${render(full)} >= ${value.render(dialect, full)}" }

infix fun Column<*>.lte(value: Any): Predicate =
    predicate { dialect, full -> "${render(full)} <= ${value.render(dialect, full)}" }

infix fun Column<*>.like(value: Any): Predicate =
    predicate { dialect, full -> "${render(full)} LIKE ${value.render(dialect, full)}" }

infix fun Column<*>.glob(value: Any): Predicate =
    predicate { dialect, full -> "${render(full)} GLOB ${value.render(dialect, full)}" }

fun Column<*>.isNotNull(): Predicate =
    predicate { dialect, full -> "${render(full)} IS NOT NULL" }

fun Column<*>.isNull(): Predicate =
    predicate { dialect, full -> "${render(full)} IS NULL" }

fun Column<*>.between(value1: Any, value2: Any): Predicate =
    predicate { dialect, full ->
        "${render(full)} BETWEEN ${
            value1.render(
                dialect,
                full
            )
        } AND ${
            value2.render(
                dialect,
                full
            )
        }"
    }

fun Column<*>.notBetween(value1: Any, value2: Any): Predicate =
    predicate { dialect, full ->
        "${render(full)} NOT BETWEEN ${
            value1.render(
                dialect,
                full
            )
        } AND ${
            value2.render(
                dialect,
                full
            )
        }"
    }

fun Column<*>.any(vararg values: Any): Predicate =
    predicate { dialect, full ->
        values.joinToString(
            prefix = "${render(full)} IN (",
            postfix = ")"
        ) { it.render(dialect, full) }
    }

fun Column<*>.none(vararg values: Any): Predicate =
    predicate { dialect, full ->
        values.joinToString(
            prefix = "${render(full)} NOT IN (",
            postfix = ")"
        ) { it.render(dialect, full) }
    }

fun exists(statement: QueryStatement): Predicate =
    predicate { dialect, _ -> "EXISTS ${statement.toString(dialect)}" }

private fun Column<*>.render(full: Boolean) =
    if (full) fullRender() else render()

private fun Any.render(dialect: Dialect, full: Boolean) = when (this) {
    is Column<*> -> if (full) fullRender() else render()
    is Statement -> "(${toString(dialect)})"
    else -> toSqlString()
}