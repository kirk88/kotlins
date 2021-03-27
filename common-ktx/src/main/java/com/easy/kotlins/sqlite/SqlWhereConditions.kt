package com.easy.kotlins.sqlite.db

import java.util.regex.Pattern

interface SqlWhereCondition {

    val whereClause: String

    val whereArgs: Array<String>

    fun render(): String

    infix fun and(condition: SqlWhereCondition): SqlWhereCondition

    infix fun or(condition: SqlWhereCondition): SqlWhereCondition

}

private class SqlWhereConditionImpl(
    override val whereClause: String,
    vararg whereArgs: String
) : SqlWhereCondition {

    override val whereArgs: Array<String> = arrayOf(*whereArgs)

    override fun render(): String {
        val builder = StringBuilder(whereClause)
        var offset = builder.indexOf("?")
        for (index in whereArgs.indices) {
            val value = whereArgs[index]
            builder.replace(offset, offset + 1, value)
            offset = builder.indexOf("?", offset)
        }
        return builder.toString()
    }

    override fun and(condition: SqlWhereCondition): SqlWhereCondition {
        val args = mutableListOf<String>()
        for (arg in whereArgs) args.add(arg)
        for (arg in condition.whereArgs) args.add(arg)
        return SqlWhereConditionImpl(
            "$whereClause AND ${condition.whereClause}",
            *args.toTypedArray()
        )
    }

    override fun or(condition: SqlWhereCondition): SqlWhereCondition {
        val args = mutableListOf<String>()
        for (arg in whereArgs) args.add(arg)
        for (arg in condition.whereArgs) args.add(arg)
        return SqlWhereConditionImpl(
            "$whereClause OR ${condition.whereClause}",
            *args.toTypedArray()
        )
    }

    override fun toString(): String {
        return "SqlWhereCondition(where: $whereClause, whereArgs: ${whereArgs.contentToString()})"
    }

}

fun SqlColumnProperty.equal(value: Any): SqlWhereCondition =
    SqlWhereConditionImpl("${this.name}=?", value.toEscapeString())

fun SqlColumnProperty.notEqual(value: Any): SqlWhereCondition =
    SqlWhereConditionImpl("${this.name}<>?", value.toEscapeString())

fun SqlColumnProperty.like(value: Any): SqlWhereCondition =
    SqlWhereConditionImpl("${this.name} LIKE ?", value.toEscapeString())

fun SqlColumnProperty.glob(value: Any): SqlWhereCondition =
    SqlWhereConditionImpl("${this.name} GLOB ?", value.toEscapeString())

fun SqlColumnProperty.greaterThan(value: Any): SqlWhereCondition =
    SqlWhereConditionImpl("${this.name}>?", value.toEscapeString())

fun SqlColumnProperty.lessThan(value: Any): SqlWhereCondition =
    SqlWhereConditionImpl("${this.name}<?", value.toEscapeString())

fun SqlColumnProperty.greaterThanOrEqual(value: Any): SqlWhereCondition =
    SqlWhereConditionImpl("${this.name}>=?", value.toEscapeString())

fun SqlColumnProperty.lessThanOrEqual(value: Any): SqlWhereCondition =
    SqlWhereConditionImpl("${this.name}<=?", value.toEscapeString())

fun SqlColumnProperty.between(value1: Any, value2: Any): SqlWhereCondition =
    SqlWhereConditionImpl(
        "${this.name} BETWEEN ? AND ?",
        value1.toEscapeString(),
        value2.toEscapeString()
    )

fun SqlColumnProperty.notNull(): SqlWhereCondition =
    SqlWhereConditionImpl("${this.name} IS NOT NULL")

fun SqlColumnProperty.isNull(): SqlWhereCondition = SqlWhereConditionImpl("${this.name} IS NULL")

fun SqlColumnProperty.any(vararg values: Any): SqlWhereCondition {
    val builder = StringBuilder("${this.name} IN (")
    for (index in values.indices) {
        if (index > 0) builder.append(",")
        builder.append("?")
    }
    builder.append(")")
    return SqlWhereConditionImpl(
        builder.toString(),
        *values.map { it.toEscapeString() }.toTypedArray()
    )
}

fun SqlColumnProperty.none(vararg values: Any): SqlWhereCondition {
    val builder = StringBuilder("${this.name} NOT IN (")
    for (index in values.indices) {
        if (index > 0) builder.append(",")
        builder.append("?")
    }
    builder.append(")")
    return SqlWhereConditionImpl(
        builder.toString(),
        *values.map { it.toEscapeString() }.toTypedArray()
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
        val value = whereArgs[key].toEscapeString()
        matcher.appendReplacement(buffer, "${matcher.group(1)}${value}")
    }
    matcher.appendTail(buffer)
    return buffer.toString()
}

private fun Any?.toEscapeString(): String {
    return when (this) {
        is Number -> this.toString()
        is Boolean -> if (this) "1" else "0"
        else -> "'${this?.toString()?.replace("'".toRegex(), "''").orEmpty()}'"
    }
}