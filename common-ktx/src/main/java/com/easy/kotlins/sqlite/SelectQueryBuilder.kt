@file:Suppress("unused", "NOTHING_TO_INLINE")

package com.easy.kotlins.sqlite.db

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.easy.kotlins.sqlite.SqlColumnProperty
import com.easy.kotlins.sqlite.SqlWhereCondition
import com.easy.kotlins.sqlite.applyArguments

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

    private var whereCauseApplied = false
    private var selectWhereCause: String? = null
    private var selectWhereArgs: Array<out String>? = null

    fun distinct(): SelectQueryBuilder {
        this.distinct = true
        return this
    }

    fun column(name: SqlColumnProperty): SelectQueryBuilder {
        this.columnsApplied = true
        this.columns.add(name.name)
        return this
    }

    fun columns(vararg names: SqlColumnProperty): SelectQueryBuilder {
        this.columnsApplied = true
        this.columns.addAll(names.map { it.name })
        return this
    }

    fun groupBy(value: String): SelectQueryBuilder {
        this.groupByApplied = true
        this.groupBy.add(value)
        return this
    }

    fun orderBy(
        value: String,
        direction: SqlOrderDirection = SqlOrderDirection.ASC
    ): SelectQueryBuilder {
        this.orderByApplied = true
        this.orderBy.add(if (direction == SqlOrderDirection.DESC) "$value DESC" else value)
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

    fun having(having: String): SelectQueryBuilder {
        if (this.havingApplied) {
            throw IllegalStateException("Query having was already applied.")
        }

        this.havingApplied = true
        this.having = having
        return this
    }

    fun having(having: String, vararg havingArgs: Pair<String, Any>): SelectQueryBuilder {
        if (this.whereCauseApplied) {
            throw IllegalStateException("Query having was already applied.")
        }

        this.havingApplied = true
        this.having = applyArguments(having, *havingArgs)
        return this
    }

    fun whereArgs(whereCondition: SqlWhereCondition): SelectQueryBuilder {
        if (this.whereCauseApplied) {
            throw IllegalStateException("Query selection was already applied.")
        }

        this.whereCauseApplied = true
        this.selectWhereCause = whereCondition.whereCause
        this.selectWhereArgs = whereCondition.whereArgs
        return this
    }

    fun whereArgs(whereCause: String, vararg whereArgs: Pair<String, Any>): SelectQueryBuilder {
        if (this.whereCauseApplied) {
            throw IllegalStateException("Query selection was already applied.")
        }

        this.whereCauseApplied = true
        this.selectWhereCause = applyArguments(whereCause, *whereArgs)
        return this
    }

    fun whereArgs(whereCause: String): SelectQueryBuilder {
        if (this.whereCauseApplied) {
            throw IllegalStateException("Query selection was already applied.")
        }

        this.whereCauseApplied = true
        this.selectWhereCause = whereCause
        return this
    }

    fun whereSimple(whereCause: String, vararg whereArgs: Any): SelectQueryBuilder {
        if (this.whereCauseApplied) {
            throw IllegalStateException("Query selection was already applied.")
        }

        this.whereCauseApplied = true
        this.selectWhereCause = whereCause
        this.selectWhereArgs = whereArgs.map { it.toString() }.toTypedArray()
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
        val finalWhereCause = if (this.whereCauseApplied) this.selectWhereCause else null
        val finalWhereArgs = if (this.whereCauseApplied) this.selectWhereArgs else null
        val finalColumns = if (this.columnsApplied) this.columns.toTypedArray() else null
        val finalGroupBy = if (this.groupByApplied) this.groupBy.joinToString(", ") else null
        val finalOrderBy = if (this.orderByApplied) this.orderBy.joinToString(", ") else null
        return query(
            distinct,
            table,
            finalColumns,
            finalWhereCause,
            finalWhereArgs,
            finalGroupBy,
            having,
            finalOrderBy,
            limit
        ).use(action)
    }

    protected abstract fun query(
        distinct: Boolean,
        table: String,
        columns: Array<String>?,
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
        columns: Array<String>?,
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