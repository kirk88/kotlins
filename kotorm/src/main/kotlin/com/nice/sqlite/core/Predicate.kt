@file:Suppress("unused")

package com.nice.sqlite.core

import com.nice.sqlite.SQLiteDialect
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

infix fun Column<*>.eq(value: Any): Predicate =
    ConditionPart("$name = $value") { dialect, full -> "${render(full = full)} = ${value.render(dialect, full)}" }

infix fun Column<*>.ne(value: Any): Predicate =
    ConditionPart("$name <> $value") { dialect, full -> "${render(full = full)} <> ${value.render(dialect, full)}" }

infix fun Column<*>.gt(value: Any): Predicate =
    ConditionPart("$name > $value") { dialect, full -> "${render(full = full)} > ${value.render(dialect, full)}" }

infix fun Column<*>.lt(value: Any): Predicate =
    ConditionPart("$name < $value") { dialect, full -> "${render(full = full)} < ${value.render(dialect, full)}" }

infix fun Column<*>.gte(value: Any): Predicate =
    ConditionPart("$name >= $value") { dialect, full -> "${render(full = full)} >= ${value.render(dialect, full)}" }

infix fun Column<*>.lte(value: Any): Predicate =
    ConditionPart("$name <= $value") { dialect, full -> "${render(full = full)} <= ${value.render(dialect, full)}" }

infix fun Column<*>.like(value: Any): Predicate =
    ConditionPart("$name LIKE $value") { dialect, full -> "${render(full = full)} LIKE ${value.render(dialect, full)}" }

infix fun Column<*>.glob(value: Any): Predicate =
    ConditionPart("$name GLOB $value") { dialect, full -> "${render(full = full)} GLOB ${value.render(dialect, full)}" }

fun Column<*>.isNotNull(): Predicate =
    ConditionPart("$name IS NOT NULL") { _, full -> "${render(full = full)} IS NOT NULL" }

fun Column<*>.isNull(): Predicate =
    ConditionPart("$name IS NULL") { _, full -> "${render(full = full)} IS NULL" }

fun Column<*>.between(value1: Any, value2: Any): Predicate =
    ConditionPart("$name BETWEEN $value1 AND $value2") { dialect, full ->
        "${render(full = full)} BETWEEN ${
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
    ConditionPart("$name NOT BETWEEN $value1 AND $value2") { dialect, full ->
        "${render(full = full)} NOT BETWEEN ${
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
    ConditionPart("$name IN ${values.contentToString()}") { dialect, full ->
        values.joinToString(
            prefix = "${render(full = full)} IN (",
            postfix = ")"
        ) { it.render(dialect, full) }
    }

fun Column<*>.none(vararg values: Any): Predicate =
    ConditionPart("$name NOT IN ${values.contentToString()}") { dialect, full ->
        values.joinToString(
            prefix = "${render(full = full)} NOT IN (",
            postfix = ")"
        ) { it.render(dialect, full) }
    }

fun exists(statement: QueryStatement): Predicate =
    ConditionPart(statement.toString(SQLiteDialect)) { dialect, _ -> "EXISTS ${statement.render(dialect = dialect)}" }


private fun Any.render(dialect: Dialect? = null, full: Boolean = false) = when {
    this is Column<*> -> if (full) fullRender() else render()
    this is Statement && dialect != null -> "(${toString(dialect)})"
    else -> toSqlString()
}