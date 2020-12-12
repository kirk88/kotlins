/*
 * Copyright 2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("unused", "NOTHING_TO_INLINE")

package com.easy.kotlins.sqlite.db

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

abstract class SelectQueryBuilder(private val tableName: String) {

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
    private var useNativeWhereCause = false
    private var simpleWhereCause: String? = null
    private var nativeWhereArgs: Array<out String>? = null

    fun <T> exec(f: Cursor.() -> T): T {
        return doExec().use(f)
    }

    fun <T : Any> parseSingle(parser: RowParser<T>): Unit = doExec().use {
        it.parseSingle(parser)
    }

    fun <T : Any> parseOpt(parser: RowParser<T>): Unit = doExec().use {
        it.parseOpt(parser)
    }

    fun <T : Any> parseList(parser: RowParser<T>): Unit = doExec().use {
        it.parseList(parser)
    }

    fun <T : Any> parseSingle(parser: MapRowParser<T>): T = doExec().use {
        it.parseSingle(parser)
    }

    fun <T : Any> parseOpt(parser: MapRowParser<T>): T? = doExec().use {
        it.parseOpt(parser)
    }

    fun <T : Any> parseList(parser: MapRowParser<T>): List<T> = doExec().use {
        it.parseList(parser)
    }

    private fun doExec(): Cursor {
        val finalWhereCause = if (whereCauseApplied) simpleWhereCause else null
        val finalWhereArgs = if (whereCauseApplied && useNativeWhereCause) nativeWhereArgs else null
        val finalColumns = if (columnsApplied) columns.toTypedArray() else null
        val finalGroupBy = if (groupByApplied) groupBy.joinToString(", ") else null
        val finalOrderBy = if (orderByApplied) orderBy.joinToString(", ") else null
        return execQuery(
            distinct, tableName,
            finalColumns,
            finalWhereCause,
            finalWhereArgs,
            finalGroupBy,
            having,
            finalOrderBy,
            limit
        )
    }

    protected abstract fun execQuery(
        distinct: Boolean,
        tableName: String,
        columns: Array<String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        groupBy: String?,
        having: String?,
        orderBy: String?,
        limit: String?
    ): Cursor

    fun distinct(): SelectQueryBuilder {
        this.distinct = true
        return this
    }

    fun column(name: String): SelectQueryBuilder {
        columnsApplied = true
        columns.add(name)
        return this
    }

    fun columns(vararg names: String): SelectQueryBuilder {
        columnsApplied = true
        columns.addAll(names)
        return this
    }

    fun groupBy(value: String): SelectQueryBuilder {
        groupByApplied = true
        groupBy.add(value)
        return this
    }

    fun orderBy(
        value: String,
        direction: SqlOrderDirection = SqlOrderDirection.ASC
    ): SelectQueryBuilder {
        orderByApplied = true
        orderBy.add(if (direction == SqlOrderDirection.DESC) "$value DESC" else value)
        return this
    }

    fun limit(count: Int): SelectQueryBuilder {
        limit = count.toString()
        return this
    }

    fun limit(offset: Int, count: Int): SelectQueryBuilder {
        limit = "$offset, $count"
        return this
    }

    fun having(having: String): SelectQueryBuilder {
        if (havingApplied) {
            throw IllegalStateException("Query having was already applied.")
        }

        havingApplied = true
        this.having = having
        return this
    }

    fun having(having: String, vararg args: Pair<String, Any>): SelectQueryBuilder {
        if (whereCauseApplied) {
            throw IllegalStateException("Query having was already applied.")
        }

        havingApplied = true
        this.having = applyArguments(having, *args)
        return this
    }


    fun whereArgs(whereCause: String, vararg whereArgs: Pair<String, Any>): SelectQueryBuilder {
        if (whereCauseApplied) {
            throw IllegalStateException("Query selection was already applied.")
        }

        whereCauseApplied = true
        useNativeWhereCause = false
        simpleWhereCause = applyArguments(whereCause, *whereArgs)
        return this
    }

    fun whereArgs(whereCause: String): SelectQueryBuilder {
        if (whereCauseApplied) {
            throw IllegalStateException("Query selection was already applied.")
        }

        whereCauseApplied = true
        useNativeWhereCause = false
        simpleWhereCause = whereCause
        return this
    }

    fun whereSimple(whereCause: String, vararg whereArgs: Any): SelectQueryBuilder {
        if (whereCauseApplied) {
            throw IllegalStateException("Query selection was already applied.")
        }

        whereCauseApplied = true
        useNativeWhereCause = true
        simpleWhereCause = whereCause
        nativeWhereArgs = whereArgs.map { it.toString() }.toTypedArray()
        return this
    }

}

class AndroidSdkDatabaseSelectQueryBuilder(
    private val db: SQLiteDatabase,
    tableName: String
) : SelectQueryBuilder(tableName) {

    override fun execQuery(
        distinct: Boolean,
        tableName: String,
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
            tableName,
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