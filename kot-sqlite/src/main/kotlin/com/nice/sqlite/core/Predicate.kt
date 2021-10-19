@file:Suppress("unused")

package com.nice.sqlite.core

import com.nice.sqlite.core.ddl.Definition
import com.nice.sqlite.core.ddl.QueryStatement
import com.nice.sqlite.core.ddl.Statement
import com.nice.sqlite.core.ddl.toSqlString

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

private class ConditionPart(
    private val id: String,
    private val rendered: (dialect: Dialect, full: Boolean) -> String
) : Predicate {

    override fun render(dialect: Dialect): String = rendered(dialect, false)

    override fun fullRender(dialect: Dialect): String = rendered(dialect, true)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConditionPart

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}

infix fun Definition.eq(value: Any): Predicate =
    ConditionPart("$name = $value") { dialect, full ->
        "${render(full = full)} = ${
            value.render(
                dialect,
                full
            )
        }"
    }

infix fun Definition.ne(value: Any): Predicate =
    ConditionPart("$name <> $value") { dialect, full ->
        "${render(full = full)} <> ${
            value.render(
                dialect,
                full
            )
        }"
    }

infix fun Definition.gt(value: Any): Predicate =
    ConditionPart("$name > $value") { dialect, full ->
        "${render(full = full)} > ${
            value.render(
                dialect,
                full
            )
        }"
    }

infix fun Definition.lt(value: Any): Predicate =
    ConditionPart("$name < $value") { dialect, full ->
        "${render(full = full)} < ${
            value.render(
                dialect,
                full
            )
        }"
    }

infix fun Definition.gte(value: Any): Predicate =
    ConditionPart("$name >= $value") { dialect, full ->
        "${render(full = full)} >= ${
            value.render(
                dialect,
                full
            )
        }"
    }

infix fun Definition.lte(value: Any): Predicate =
    ConditionPart("$name <= $value") { dialect, full ->
        "${render(full = full)} <= ${
            value.render(
                dialect,
                full
            )
        }"
    }

infix fun Definition.like(value: Any): Predicate =
    ConditionPart("$name LIKE $value") { dialect, full ->
        "${render(full = full)} LIKE ${
            value.render(
                dialect,
                full
            )
        }"
    }

infix fun Definition.glob(value: Any): Predicate =
    ConditionPart("$name GLOB $value") { dialect, full ->
        "${render(full = full)} GLOB ${
            value.render(
                dialect,
                full
            )
        }"
    }

fun Definition.isNotNull(): Predicate =
    ConditionPart("$name IS NOT NULL") { _, full -> "${render(full = full)} IS NOT NULL" }

fun Definition.isNull(): Predicate =
    ConditionPart("$name IS NULL") { _, full -> "${render(full = full)} IS NULL" }

fun Definition.between(startValue: Any, endValue: Any): Predicate =
    ConditionPart("$name BETWEEN $startValue AND $endValue") { dialect, full ->
        "${render(full = full)} BETWEEN ${
            startValue.render(
                dialect,
                full
            )
        } AND ${
            endValue.render(
                dialect,
                full
            )
        }"
    }

fun Definition.notBetween(startValue: Any, endValue: Any): Predicate =
    ConditionPart("$name NOT BETWEEN $startValue AND $endValue") { dialect, full ->
        "${render(full = full)} NOT BETWEEN ${
            startValue.render(
                dialect,
                full
            )
        } AND ${
            endValue.render(
                dialect,
                full
            )
        }"
    }

fun Definition.any(vararg values: Any): Predicate =
    ConditionPart("$name IN ${values.contentToString()}") { dialect, full ->
        values.joinToString(
            prefix = "${render(full = full)} IN (",
            postfix = ")"
        ) { it.render(dialect, full) }
    }

fun Definition.none(vararg values: Any): Predicate =
    ConditionPart("$name NOT IN ${values.contentToString()}") { dialect, full ->
        values.joinToString(
            prefix = "${render(full = full)} NOT IN (",
            postfix = ")"
        ) { it.render(dialect, full) }
    }

fun exists(statement: QueryStatement): Predicate =
    ConditionPart("EXISTS ${System.identityHashCode(statement)}") { dialect, _ ->
        "EXISTS ${statement.render(dialect = dialect)}"
    }


private fun Any.render(dialect: Dialect? = null, full: Boolean = false) = when {
    this is Definition -> if (full) fullRender() else render()
    this is Statement && dialect != null -> "(${toString(dialect)})"
    else -> toSqlString()
}