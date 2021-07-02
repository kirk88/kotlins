@file:Suppress("unused", "NOTHING_TO_INLINE")

package com.nice.kotlins.sqlite.db

import android.database.Cursor
import android.os.CancellationSignal
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteQueryBuilder

class SupportDatabaseQueryBuilder(
    private val database: SupportSQLiteDatabase,
    table: String
) {

    private val delegate: SupportSQLiteQueryBuilder = SupportSQLiteQueryBuilder.builder(table)

    fun columns(vararg columns: SqlColumnProperty) = apply {
        delegate.columns(columns.map { it.name }.toTypedArray())
    }

    fun columns(columns: Collection<SqlColumnProperty>) = apply {
        delegate.columns(columns.map { it.name }.toTypedArray())
    }

    fun distinct() = apply {
        delegate.distinct()
    }

    fun groupBy(vararg groupBy: SqlColumnProperty) = apply {
        delegate.groupBy(groupBy.joinToString(", ") { it.name })
    }

    fun groupBy(groupBy: String) = apply {
        delegate.groupBy(groupBy)
    }

    fun orderBy(
        vararg orderBy: SqlColumnProperty,
        direction: SqlOrderDirection = SqlOrderDirection.ASC
    ) = apply {
        delegate.orderBy(orderBy.joinToString(", ") { it.name }.let {
            if (direction == SqlOrderDirection.DESC) "$it DESC" else it
        })
    }

    fun orderBy(
        orderBy: String,
        direction: SqlOrderDirection = SqlOrderDirection.ASC
    ) = apply {
        delegate.orderBy(if (direction == SqlOrderDirection.DESC) "$orderBy DESC" else orderBy)
    }

    fun limit(limit: String) = apply {
        delegate.limit(limit)
    }

    fun limit(count: Int) = apply {
        delegate.limit("$count")
    }

    fun limit(offset: Int, count: Int) = apply {
        delegate.limit("$offset, $count")
    }

    fun having(condition: SqlWhereCondition) = apply {
        delegate.having(condition.render())
    }

    fun having(having: String) = apply {
        delegate.having(having)
    }

    fun having(having: String, vararg havingArgs: Pair<String, Any>) = apply {
        delegate.having(applyArguments(having, *havingArgs))
    }

    fun selection(condition: SqlWhereCondition) = apply {
        delegate.selection(condition.whereClause, condition.whereArgs)
    }

    fun selection(selection: String, vararg selectionArgs: Any) = apply {
        delegate.selection(selection, selectionArgs)
    }

    fun selection(
        selection: String,
        vararg selectionArgs: Pair<String, Any>
    ) = apply {
        delegate.selection(applyArguments(selection, *selectionArgs), null)
    }

    fun selection(selection: String) = apply {
        delegate.selection(selection, null)
    }

    fun <T> execute(cancellationSignal: CancellationSignal? = null, action: Cursor.() -> T): T {
        val cursor = if (cancellationSignal != null) {
            database.query(delegate.create(), cancellationSignal)
        } else {
            database.query(delegate.create())
        }
        return cursor.use(action)
    }

    fun <T : Any> parseSingle(parser: RowParser<T>): T = execute {
        parseSingle(parser)
    }

    fun <T : Any> parseSingleOrNull(parser: RowParser<T>): T? = execute {
        parseSingleOrNull(parser)
    }

    fun <T : Any> parseList(parser: RowParser<T>): List<T> = execute {
        parseList(parser)
    }

    fun <T : Any> parseSingle(parser: MapRowParser<T>): T = execute {
        parseSingle(parser)
    }

    fun <T : Any> parseSingleOrNull(parser: MapRowParser<T>): T? = execute {
        parseSingleOrNull(parser)
    }

    fun <T : Any> parseList(parser: MapRowParser<T>): List<T> = execute {
        parseList(parser)
    }

    inline fun <reified T : Any> parseSingle(): T = parseSingle(classParser())

    inline fun <reified T : Any> parseSingleOrNull(): T? = parseSingleOrNull(classParser())

    inline fun <reified T : Any> parseList(): List<T> = parseList(classParser())

}