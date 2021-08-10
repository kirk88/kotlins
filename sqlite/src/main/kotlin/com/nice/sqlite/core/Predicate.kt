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

private class WhereCause(
    private vararg val values: Any,
    private val whereCase: (Boolean) -> String
) : Predicate {

    private val id: Int by lazy { whereCase(true).hashCode() }

    override fun render(dialect: Dialect): String =
        whereCase(false).format(dialect, values)

    override fun fullRender(dialect: Dialect): String =
        whereCase(true).format(dialect, values)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WhereCause

        if (!values.contentEquals(other.values)) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = values.contentHashCode()
        result = 31 * result + id
        return result
    }

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Condition

        if (predicateLeft != other.predicateLeft) return false
        if (connector != other.connector) return false
        if (predicateRight != other.predicateRight) return false

        return true
    }

    override fun hashCode(): Int {
        var result = predicateLeft.hashCode()
        result = 31 * result + connector.hashCode()
        result = 31 * result + predicateRight.hashCode()
        return result
    }

}

infix fun Predicate.and(predicate: Predicate): Predicate {
    return Condition(this, "AND", predicate)
}

infix fun Predicate.or(predicate: Predicate): Predicate {
    return Condition(this, "OR", predicate)
}

infix fun Column.eq(value: Any): Predicate =
    WhereCause(value) { "${render(it)} = ?" }

infix fun Column.ne(value: Any): Predicate =
    WhereCause(value) { "${render(it)} <> ?" }

infix fun Column.gt(value: Any): Predicate =
    WhereCause(value) { "${render(it)} > ?" }

infix fun Column.lt(value: Any): Predicate =
    WhereCause(value) { "${render(it)} < ?" }

infix fun Column.gte(value: Any): Predicate =
    WhereCause(value) { "${render(it)} >= ?" }

infix fun Column.lte(value: Any): Predicate =
    WhereCause(value) { "${render(it)} <= ?" }

infix fun Column.like(value: Any): Predicate =
    WhereCause(value) { "${render(it)} LIKE ?" }

infix fun Column.glob(value: Any): Predicate =
    WhereCause(value) { "${render(it)} GLOB ?" }

fun Column.isNotNull(): Predicate =
    WhereCause { "${render(it)} IS NOT NULL" }

fun Column.isNull(): Predicate =
    WhereCause { "${render(it)} IS NULL" }

fun Column.between(value1: Any, value2: Any): Predicate =
    WhereCause(value1, value2) { "${render(it)} BETWEEN ? AND ?" }

fun Column.notBetween(value1: Any, value2: Any): Predicate =
    WhereCause(value1, value2) { "${render(it)} NOT BETWEEN ? AND ?" }

fun Column.any(vararg values: Any): Predicate =
    WhereCause(*values) {
        values.joinToString(
            prefix = "${render(it)} IN (",
            postfix = ")"
        ) { "?" }
    }

fun Column.none(vararg values: Any): Predicate =
    WhereCause(*values) {
        values.joinToString(
            prefix = "${render(it)} NOT IN (",
            postfix = ")"
        ) { "?" }
    }

fun exists(statement: QueryStatement): Predicate =
    WhereCause(statement) { "EXISTS ?" }

private fun Column.render(full: Boolean) =
    if (full) fullRender() else render()

private fun Any.render(dialect: Dialect) =
    if (this is Statement) "(${toString(dialect)})" else toSqlString()

private fun String.format(dialect: Dialect, args: Array<out Any>): String {
    val builder = StringBuilder(this)
    var offset = builder.indexOf("?")
    for (value in args) {
        builder.replace(offset, offset + 1, value.render(dialect))
        offset = builder.indexOf("?", offset)
    }
    return builder.toString()
}