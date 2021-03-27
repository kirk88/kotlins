package com.easy.kotlins.sqlite.db

import java.util.regex.Pattern

interface SqlWhereCondition {

    val whereClause: String

    val whereArgs: Array<String>

    infix fun and(condition: SqlWhereCondition): SqlWhereCondition

    infix fun or(condition: SqlWhereCondition): SqlWhereCondition

}

private class SqlWhereConditionImpl(
    override val whereClause: String,
    vararg whereArgs: Any
) : SqlWhereCondition {

    override val whereArgs: Array<String> = whereArgs.map { it.toString() }.toTypedArray()


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
        val value = whereArgs[key].let {
            when (it) {
                is Number -> it.toString()
                is Boolean -> if (it) "1" else "0"
                else -> "'${it?.toString()?.replace("'".toRegex(), "''").orEmpty()}'"
            }
        }
        matcher.appendReplacement(buffer, "${matcher.group(1)}${value}")
    }
    matcher.appendTail(buffer)
    return buffer.toString()
}
