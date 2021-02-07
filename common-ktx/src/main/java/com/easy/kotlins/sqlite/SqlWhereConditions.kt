package com.easy.kotlins.sqlite

import java.util.regex.Pattern


interface SqlWhereCondition {

    val whereCause: String

    val whereArgs: Array<out String>?

    infix fun and(condition: SqlWhereCondition): SqlWhereCondition

    infix fun or(condition: SqlWhereCondition): SqlWhereCondition
}

private class SqlWhereConditionImpl(override val whereCause: String, args: Array<out Any>? = null) :
    SqlWhereCondition {

    override val whereArgs: Array<out String>? = args?.map { it.toString() }?.toTypedArray()

    override fun and(condition: SqlWhereCondition): SqlWhereCondition {
        val args = if (whereArgs != null || condition.whereArgs != null) {
            (whereArgs.orEmpty().toList() + condition.whereArgs.orEmpty()).toTypedArray()
        } else {
            null
        }
        return SqlWhereConditionImpl("$whereCause AND ${condition.whereCause}", args)
    }

    override fun or(condition: SqlWhereCondition): SqlWhereCondition {
        val args = if (whereArgs != null || condition.whereArgs != null) {
            (whereArgs.orEmpty().toList() + condition.whereArgs.orEmpty()).toTypedArray()
        } else {
            null
        }
        return SqlWhereConditionImpl("$whereCause OR ${condition.whereCause}", args)
    }

    override fun toString(): String {
        return "SqlWhereBuilder(whereCause: $whereCause, whereArgs: $whereArgs)"
    }
}

fun String.whereArgs(vararg whereArgs: Any): SqlWhereCondition {
    return SqlWhereConditionImpl(this, whereArgs)
}

fun String.whereArgs(vararg whereArgs: Pair<String, Any>): SqlWhereCondition {
    val whereArgsMap = whereArgs.fold(hashMapOf<String, Any>()) { map, arg ->
        map[arg.first] = arg.second
        map
    }
    return SqlWhereConditionImpl(applyArguments(this, whereArgsMap))
}

fun String.equal(value: Any): SqlWhereCondition {
    return SqlWhereConditionImpl("$this = ${value.toEscapedString()}")
}

fun String.like(value: Any): SqlWhereCondition {
    return SqlWhereConditionImpl("$this LIKE ${value.toEscapedString()}")
}

fun String.glob(value: Any): SqlWhereCondition {
    return SqlWhereConditionImpl("$this GLOB ${value.toEscapedString()}")
}

fun String.greaterThan(value: Int): SqlWhereCondition {
    return SqlWhereConditionImpl("$this >= $value")
}

fun String.lessThan(value: Int): SqlWhereCondition {
    return SqlWhereConditionImpl("$this <= $value")
}

fun String.any(vararg values: Any): SqlWhereCondition {
    return SqlWhereConditionImpl(values.joinToString(
        ",",
        "$this IN (",
        ")"
    ) { it.toEscapedString() })
}

fun String.none(vararg values: Any): SqlWhereCondition {
    return SqlWhereConditionImpl(values.joinToString(
        ",",
        "$this NOT IN (",
        ")"
    ) { it.toEscapedString() })
}

fun String.notNull(): SqlWhereCondition {
    return SqlWhereConditionImpl("$this NOT NULL")
}

fun String.isNull(): SqlWhereCondition {
    return SqlWhereConditionImpl("$this IS NULL")
}

fun SqlColumnProperty.whereArgs(vararg whereArgs: Any): SqlWhereCondition =
    this.name.whereArgs(*whereArgs)

fun SqlColumnProperty.whereArgs(vararg whereArgs: Pair<String, Any>): SqlWhereCondition =
    this.name.whereArgs(*whereArgs)

fun SqlColumnProperty.equal(value: Any): SqlWhereCondition = this.name.equal(value)
fun SqlColumnProperty.like(value: Any): SqlWhereCondition = this.name.like(value)
fun SqlColumnProperty.glob(value: Any): SqlWhereCondition = this.name.glob(value)
fun SqlColumnProperty.greaterThan(value: Int): SqlWhereCondition = this.name.greaterThan(value)
fun SqlColumnProperty.lessThan(value: Int): SqlWhereCondition = this.name.lessThan(value)
fun SqlColumnProperty.any(vararg values: Any): SqlWhereCondition = this.name.any(*values)
fun SqlColumnProperty.none(vararg values: Any): SqlWhereCondition = this.name.none(*values)
fun SqlColumnProperty.notNull(): SqlWhereCondition = this.name.notNull()
fun SqlColumnProperty.isNull(): SqlWhereCondition = this.name.isNull()

private val ARG_PATTERN: Pattern = Pattern.compile("([^\\\\])\\{([^{}]+)\\}")

internal fun applyArguments(whereClause: String, vararg args: Pair<String, Any>): String {
    val argsMap = args.fold(hashMapOf<String, Any>()) { map, arg ->
        map[arg.first] = arg.second
        map
    }
    return applyArguments(whereClause, argsMap)
}

internal fun applyArguments(whereClause: String, args: Map<String, Any>): String {
    val matcher = ARG_PATTERN.matcher(whereClause)
    val buffer = StringBuffer(whereClause.length)
    while (matcher.find()) {
        val key = matcher.group(2) ?: continue
        val value = args[key] ?: throw IllegalStateException("Can't find a value for key $key")

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