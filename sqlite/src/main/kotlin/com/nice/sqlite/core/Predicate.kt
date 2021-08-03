@file:Suppress("unused")

package com.nice.sqlite.core

import com.nice.sqlite.core.ddl.Column
import com.nice.sqlite.core.ddl.Renderer
import com.nice.sqlite.core.ddl.toSqlString

interface Predicate : Renderer

interface Condition : Predicate {

    val predicateLeft: Predicate
    val connector: String
    val predicateRight: Predicate

}

private fun Predicate(render: (Boolean) -> String): Predicate {
    return object : Predicate {

        override fun render(): String = render(false)

        override fun fullRender(): String = render(true)

    }
}

private class ConditionImpl(
    override val predicateLeft: Predicate,
    override val connector: String,
    override val predicateRight: Predicate
) : Condition {

    override fun render(): String =
        "(${predicateLeft.render()} $connector ${predicateRight.render()})"

    override fun fullRender(): String =
        "(${predicateLeft.fullRender()} $connector ${predicateRight.fullRender()})"

}

infix fun Predicate.and(predicate: Predicate): Predicate {
    return ConditionImpl(this, "AND", predicate)
}

infix fun Predicate.or(predicate: Predicate): Predicate {
    return ConditionImpl(this, "OR", predicate)
}

infix fun Column<*>.eq(value: Any): Predicate =
    Predicate { "${render(it)} = ${value.toSqlString()}" }

infix fun Column<*>.ne(value: Any): Predicate =
    Predicate { "${render(it)} <> ${value.toSqlString()}" }

infix fun Column<*>.gt(value: Any): Predicate =
    Predicate { "${render(it)} > ${value.toSqlString()}" }

infix fun Column<*>.lt(value: Any): Predicate =
    Predicate { "${render(it)} < ${value.toSqlString()}" }

infix fun Column<*>.gte(value: Any): Predicate =
    Predicate { "${render(it)} >= ${value.toSqlString()}" }

infix fun Column<*>.lte(value: Any): Predicate =
    Predicate { "${render(it)} <= ${value.toSqlString()}" }

infix fun Column<*>.like(value: Any): Predicate =
    Predicate { "${render(it)} LIKE ${value.toSqlString()}" }

infix fun Column<*>.glob(value: Any): Predicate =
    Predicate { "${render(it)} GLOB ${value.toSqlString()}" }

fun Column<*>.isNotNull(): Predicate =
    Predicate { "${render(it)} IS NOT NULL" }

fun Column<*>.isNull(): Predicate =
    Predicate { "${render(it)} IS NULL" }

fun Column<*>.between(value1: Any, value2: Any): Predicate =
    Predicate { "${render(it)} BETWEEN ${value1.toSqlString()} AND ${value2.toSqlString()}" }

fun Column<*>.notBetween(value1: Any, value2: Any): Predicate =
    Predicate { "${render(it)} NOT BETWEEN ${value1.toSqlString()} AND ${value2.toSqlString()}" }

fun Column<*>.any(vararg values: Any): Predicate =
    Predicate {
        values.joinToString(
            prefix = "${render(it)} IN (",
            postfix = ")"
        ) { value -> value.toSqlString() }
    }

fun Column<*>.none(vararg values: Any): Predicate =
    Predicate {
        values.joinToString(
            prefix = "${render(it)} NOT IN (",
            postfix = ")"
        ) { value -> value.toSqlString() }
    }

private fun Column<*>.render(full: Boolean) = if (full) fullRender() else render()