package com.easy.kotlins.sqlite.db

import java.util.regex.Pattern

interface SqlWhereCondition {

    val whereClause: String

    val whereArgs: Array<out String>

    infix fun and(condition: SqlWhereCondition): SqlWhereCondition

    infix fun or(condition: SqlWhereCondition): SqlWhereCondition

}

private class SqlWhereConditionImpl(
    override val whereClause: String,
    vararg args: Any
) : SqlWhereCondition {

    override val whereArgs: Array<out String> = args.map { it.toEscapedString() }.toTypedArray()

    override fun and(condition: SqlWhereCondition): SqlWhereCondition {
        val args = (whereArgs.toList() + condition.whereArgs).toTypedArray()
        return SqlWhereConditionImpl("$whereClause AND ${condition.whereClause}", args)
    }

    override fun or(condition: SqlWhereCondition): SqlWhereCondition {
        val args = (whereArgs.toList() + condition.whereArgs).toTypedArray()
        return SqlWhereConditionImpl("$whereClause OR ${condition.whereClause}", args)
    }

    override fun toString(): String {
        return "SqlWhereCondition(where: $whereClause, whereArgs: ${whereArgs.contentToString()})"
    }

}

fun SqlColumnProperty.equal(value: Any): SqlWhereCondition =
    SqlWhereConditionImpl("${this.name} = ?", value)

fun SqlColumnProperty.notEqual(value: Any): SqlWhereCondition =
    SqlWhereConditionImpl("${this.name} <> ?", value)

fun SqlColumnProperty.like(value: Any): SqlWhereCondition =
    SqlWhereConditionImpl("${this.name} LIKE ?", value)

fun SqlColumnProperty.glob(value: Any): SqlWhereCondition =
    SqlWhereConditionImpl("${this.name} GLOB ?", value)

fun SqlColumnProperty.greaterThan(value: Any): SqlWhereCondition =
    SqlWhereConditionImpl("${this.name} > ?", value)

fun SqlColumnProperty.lessThan(value: Any): SqlWhereCondition =
    SqlWhereConditionImpl("${this.name} < ?", value)

fun SqlColumnProperty.greaterThanOrEqual(value: Any): SqlWhereCondition =
    SqlWhereConditionImpl("${this.name} >= ?", value)

fun SqlColumnProperty.lessThanOrEqual(value: Any): SqlWhereCondition =
    SqlWhereConditionImpl("${this.name} <= ?", value)

fun SqlColumnProperty.between(value1: Any, value2: Any): SqlWhereCondition =
    SqlWhereConditionImpl("${this.name} BETWEEN ? AND ?", value1, value2)

fun SqlColumnProperty.notNull(): SqlWhereCondition =
    SqlWhereConditionImpl("${this.name} IS NOT NULL")

fun SqlColumnProperty.isNull(): SqlWhereCondition = SqlWhereConditionImpl("${this.name} IS NULL")
fun SqlColumnProperty.any(vararg values: Any): SqlWhereCondition {
    val placeholders = arrayOfNulls<String>(values.size)
    placeholders.fill("?")
    return SqlWhereConditionImpl(
        placeholders.joinToString(
            ",",
            "${this.name} IN (",
            ")"
        ),
        *values
    )
}

fun SqlColumnProperty.none(vararg values: Any): SqlWhereCondition {
    val placeholders = arrayOfNulls<String>(values.size)
    placeholders.fill("?")
    return SqlWhereConditionImpl(
        placeholders.joinToString(
            ",",
            "${this.name} NOT IN (",
            ")"
        ),
        *values
    )
}

private val ARG_PATTERN: Pattern = Pattern.compile("([^\\\\])\\{([^{}]+)\\}")

internal fun applyArguments(whereClause: String, vararg whereArgs: Pair<String, Any>): String {
    val argsMap = whereArgs.fold(hashMapOf<String, Any>()) { map, arg ->
        map[arg.first] = arg.second
        map
    }
    return applyArguments(whereClause, argsMap)
}

internal fun applyArguments(whereClause: String, whereArgs: Map<String, Any>): String {
    val matcher = ARG_PATTERN.matcher(whereClause)
    val buffer = StringBuffer(whereClause.length)
    while (matcher.find()) {
        val key = matcher.group(2) ?: continue
        val value = whereArgs[key].toEscapedString()
        matcher.appendReplacement(buffer, "${matcher.group(1)}${value}")
    }
    matcher.appendTail(buffer)
    return buffer.toString()
}

private fun Any?.toEscapedString(): String {
    return if (this is Number) {
        this.toString()
    } else if (this is Boolean) {
        if (this) "1" else "0"
    } else if (this == null) {
        ""
    } else {
        '\'' + this.toString().replace("'", "''") + '\''
    }
}