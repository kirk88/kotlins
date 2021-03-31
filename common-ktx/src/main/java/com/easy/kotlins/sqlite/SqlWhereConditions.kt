package com.easy.kotlins.sqlite.db

import java.util.regex.Pattern

interface SqlWhereCondition {

    val whereClause: String

    val whereArgs: Array<Any>

    fun render(): String

    infix fun and(condition: SqlWhereCondition): SqlWhereCondition

    infix fun or(condition: SqlWhereCondition): SqlWhereCondition

}

private class SqlWhereConditionImpl(
    override val whereClause: String,
    vararg whereArgs: Any
) : SqlWhereCondition {

    override val whereArgs: Array<Any> = arrayOf(*whereArgs)

    override fun render(): String {
        val builder = StringBuilder(whereClause)
        var offset = builder.indexOf("?")
        for (index in whereArgs.indices) {
            val value = whereArgs[index].toEscapeString()
            builder.replace(offset, offset + 1, value)
            offset = builder.indexOf("?", offset)
        }
        return builder.toString()
    }

    override fun and(condition: SqlWhereCondition): SqlWhereCondition {
        val args = mutableListOf<Any>()
        for (arg in whereArgs) args.add(arg)
        for (arg in condition.whereArgs) args.add(arg)
        return SqlWhereConditionImpl(
            "$whereClause AND ${condition.whereClause}",
            *args.toTypedArray()
        )
    }

    override fun or(condition: SqlWhereCondition): SqlWhereCondition {
        val args = mutableListOf<Any>()
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
    SqlWhereConditionImpl("${this.name}=?", value)

fun SqlColumnProperty.notEqual(value: Any): SqlWhereCondition =
    SqlWhereConditionImpl("${this.name}<>?", value)

fun SqlColumnProperty.like(value: Any): SqlWhereCondition =
    SqlWhereConditionImpl("${this.name} LIKE ?", value)

fun SqlColumnProperty.glob(value: Any): SqlWhereCondition =
    SqlWhereConditionImpl("${this.name} GLOB ?", value)

fun SqlColumnProperty.greaterThan(value: Any): SqlWhereCondition =
    SqlWhereConditionImpl("${this.name}>?", value)

fun SqlColumnProperty.lessThan(value: Any): SqlWhereCondition =
    SqlWhereConditionImpl("${this.name}<?", value)

fun SqlColumnProperty.greaterThanOrEqual(value: Any): SqlWhereCondition =
    SqlWhereConditionImpl("${this.name}>=?", value)

fun SqlColumnProperty.lessThanOrEqual(value: Any): SqlWhereCondition =
    SqlWhereConditionImpl("${this.name}<=?", value)

fun SqlColumnProperty.between(value1: Any, value2: Any): SqlWhereCondition =
    SqlWhereConditionImpl(
        "${this.name} BETWEEN ? AND ?",
        value1,
        value2
    )

fun SqlColumnProperty.notBetween(value1: Any, value2: Any): SqlWhereCondition =
    SqlWhereConditionImpl(
        "${this.name} NOT BETWEEN ? AND ?",
        value1,
        value2
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
        *values
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
        val value = whereArgs[key].toEscapeString()
        matcher.appendReplacement(buffer, "${matcher.group(1)}${value}")
    }
    matcher.appendTail(buffer)
    return buffer.toString()
}

private fun Any?.toEscapeString(): String {
    val value = this ?: return ""
    return when (value) {
        is Number -> value.toString()
        is Boolean -> if (value) "1" else "0"
        else -> "'${value.toString().replace("'".toRegex(), "''")}'"
    }
}