package com.easy.kotlins.sqlite

import java.util.*
import java.util.regex.Pattern


interface SqlWhereCondition {

    val whereClause: String

    val whereArgs: Array<out String>?

    infix fun and(condition: SqlWhereCondition): SqlWhereCondition

    infix fun or(condition: SqlWhereCondition): SqlWhereCondition

}

private class SqlWhereConditionImpl(override val whereClause: String, args: Array<out Any>? = null) :
    SqlWhereCondition {

    override val whereArgs: Array<out String>? = args?.map { it.toString() }?.toTypedArray()

    override fun and(condition: SqlWhereCondition): SqlWhereCondition {
        val args = if (whereArgs != null || condition.whereArgs != null) {
            (whereArgs.orEmpty().toList() + condition.whereArgs.orEmpty()).toTypedArray()
        } else {
            null
        }
        return SqlWhereConditionImpl("$whereClause AND ${condition.whereClause}", args)
    }

    override fun or(condition: SqlWhereCondition): SqlWhereCondition {
        val args = if (whereArgs != null || condition.whereArgs != null) {
            (whereArgs.orEmpty().toList() + condition.whereArgs.orEmpty()).toTypedArray()
        } else {
            null
        }
        return SqlWhereConditionImpl("$whereClause OR ${condition.whereClause}", args)
    }

    override fun toString(): String {
        return "SqlWhereCondition(where: $whereClause, whereArgs: ${Arrays.toString(whereArgs)})"
    }

}

fun SqlColumnProperty.equal(value: Any): SqlWhereCondition = SqlWhereConditionImpl("${this.name} = ${value.toEscapedString()}")
fun SqlColumnProperty.like(value: Any): SqlWhereCondition = SqlWhereConditionImpl("${this.name} LIKE ${value.toEscapedString()}")
fun SqlColumnProperty.glob(value: Any): SqlWhereCondition = SqlWhereConditionImpl("${this.name} GLOB ${value.toEscapedString()}")
fun SqlColumnProperty.greaterThan(value: Int): SqlWhereCondition = SqlWhereConditionImpl("${this.name} >= $value")
fun SqlColumnProperty.lessThan(value: Int): SqlWhereCondition = SqlWhereConditionImpl("${this.name} <= $value")
fun SqlColumnProperty.notNull(): SqlWhereCondition = SqlWhereConditionImpl("${this.name} NOT NULL")
fun SqlColumnProperty.isNull(): SqlWhereCondition = SqlWhereConditionImpl("${this.name} IS NULL")
fun SqlColumnProperty.any(vararg values: Any): SqlWhereCondition = SqlWhereConditionImpl(values.joinToString(
    ",",
    "${this.name} IN (",
    ")"
) { it.toEscapedString() })
fun SqlColumnProperty.none(vararg values: Any): SqlWhereCondition = SqlWhereConditionImpl(values.joinToString(
    ",",
    "${this.name} NOT IN (",
    ")"
) { it.toEscapedString() })
fun SqlColumnProperty.whereArgs(vararg whereArgs: Any): SqlWhereCondition {
    return SqlWhereConditionImpl(this.name, whereArgs)
}
fun SqlColumnProperty.whereArgs(vararg whereArgs: Pair<String, Any>): SqlWhereCondition {
    val whereArgsMap = whereArgs.fold(hashMapOf<String, Any>()) { map, arg ->
        map[arg.first] = arg.second
        map
    }
    return SqlWhereConditionImpl(applyArguments(this.name, whereArgsMap))
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
        val value = whereArgs[key] ?: throw IllegalStateException("Can't find a value for key $key")

        matcher.appendReplacement(buffer, "${matcher.group(1)}${value.toEscapedString()}")
    }
    matcher.appendTail(buffer)
    return buffer.toString()
}

private fun Any.toEscapedString(): String {
    return if (this is Number) {
        this.toString()
    } else if (this is Boolean) {
        if (this) "1" else "0"
    } else {
        '\'' + this.toString().replace("'", "''") + '\''
    }
}