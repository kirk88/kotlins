@file:Suppress("unused", "NOTHING_TO_INLINE")

package com.easy.kotlins.sqlite.db

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

abstract class SelectQueryBuilder(private val table: String) {

    private var columnsApplied = false
    private val columns: MutableList<String> by lazy { mutableListOf() }
    private var groupByApplied = false
    private val groupBy: MutableList<String> by lazy { mutableListOf() }
    private var orderByApplied = false
    private val orderBy: MutableList<String> by lazy { mutableListOf() }

    private var distinct: Boolean = false

    private var havingApplied = false
    private var having: String? = null
    private var limit: String? = null

    private var selectionApplied = false
    private var selection: String? = null
    private var selectionArgs: Array<String>? = null

    fun distinct(): SelectQueryBuilder {
        this.distinct = true
        return this
    }

    fun columns(vararg names: SqlColumnProperty): SelectQueryBuilder {
        this.columnsApplied = true
        this.columns.addAll(names.map { it.name })
        return this
    }

    fun columns(names: List<SqlColumnProperty>): SelectQueryBuilder {
        this.columnsApplied = true
        this.columns.addAll(names.map { it.name })
        return this
    }

    fun groupBy(vararg groupBy: SqlColumnProperty): SelectQueryBuilder {
        this.groupByApplied = true
        this.groupBy.addAll(groupBy.map { it.name })
        return this
    }

    fun orderBy(
        orderBy: String,
        direction: SqlOrderDirection = SqlOrderDirection.ASC
    ): SelectQueryBuilder {
        this.orderByApplied = true
        this.orderBy.add(if (direction == SqlOrderDirection.DESC) "$orderBy DESC" else orderBy)
        return this
    }

    fun limit(count: Int): SelectQueryBuilder {
        this.limit = count.toString()
        return this
    }

    fun limit(offset: Int, count: Int): SelectQueryBuilder {
        this.limit = "$offset, $count"
        return this
    }

    fun having(condition: SqlWhereCondition): SelectQueryBuilder {
        check(!this.havingApplied) { "Query having was already applied" }

        this.havingApplied = true
        this.having = condition.render()
        return this
    }

    fun having(having: String): SelectQueryBuilder {
        check(!this.havingApplied) { "Query having was already applied" }

        this.havingApplied = true
        this.having = having
        return this
    }

    fun having(having: String, vararg havingArgs: Pair<String, Any>): SelectQueryBuilder {
        check(!this.havingApplied) { "Query having was already applied" }

        this.havingApplied = true
        this.having = applyArguments(having, *havingArgs)
        return this
    }

    fun where(condition: SqlWhereCondition): SelectQueryBuilder {
        check(!this.selectionApplied) { "Query selection was already applied" }

        this.selectionApplied = true
        this.selection = condition.whereClause
        this.selectionArgs = condition.whereArgs
        return this
    }

    fun where(selection: String, vararg selectionArgs: Pair<String, Any>): SelectQueryBuilder {
        check(!this.selectionApplied) { "Query selection was already applied" }

        this.selectionApplied = true
        this.selection = applyArguments(selection, *selectionArgs)
        return this
    }

    fun where(selection: String): SelectQueryBuilder {
        check(!this.selectionApplied) { "Query selection was already applied" }

        this.selectionApplied = true
        this.selection = selection
        return this
    }

    fun where(selection: String, vararg selectionArgs: Any): SelectQueryBuilder {
        check(!this.selectionApplied) { "Query selection was already applied" }

        this.selectionApplied = true
        this.selection = selection
        this.selectionArgs = selectionArgs.map { it.toString() }.toTypedArray()
        return this
    }

    fun <T : Any> parseSingle(parser: RowParser<T>): T = execute {
        parseSingle(parser)
    }

    fun <T : Any> parseOpt(parser: RowParser<T>): T? = execute {
        parseOpt(parser)
    }

    fun <T : Any> parseList(parser: RowParser<T>): List<T> = execute {
        parseList(parser)
    }

    fun <T : Any> parseSingle(parser: MapRowParser<T>): T = execute {
        parseSingle(parser)
    }

    fun <T : Any> parseOpt(parser: MapRowParser<T>): T? = execute {
        parseOpt(parser)
    }

    fun <T : Any> parseList(parser: MapRowParser<T>): List<T> = execute {
        parseList(parser)
    }

    fun <T> execute(action: Cursor.() -> T): T {
        val finalSelection = if (this.selectionApplied) this.selection else null
        val finalSelectionArgs = if (this.selectionApplied) this.selectionArgs else null
        val finalColumns = if (this.columnsApplied) this.columns.toTypedArray() else null
        val finalGroupBy = if (this.groupByApplied) this.groupBy.joinToString(", ") else null
        val finalOrderBy = if (this.orderByApplied) this.orderBy.joinToString(", ") else null
        return query(
            distinct,
            table,
            finalColumns,
            finalSelection,
            finalSelectionArgs,
            finalGroupBy,
            having,
            finalOrderBy,
            limit
        ).use(action)
    }

    protected abstract fun query(
        distinct: Boolean,
        table: String,
        columns: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        groupBy: String?,
        having: String?,
        orderBy: String?,
        limit: String?
    ): Cursor

}

class AndroidDatabaseSelectQueryBuilder(
    private val db: SQLiteDatabase,
    table: String,
) : SelectQueryBuilder(table) {

    override fun query(
        distinct: Boolean,
        table: String,
        columns: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        groupBy: String?,
        having: String?,
        orderBy: String?,
        limit: String?
    ): Cursor {
        return db.query(
            distinct,
            table,
            columns,
            selection,
            selectionArgs,
            groupBy,
            having,
            orderBy,
            limit
        )
    }

}