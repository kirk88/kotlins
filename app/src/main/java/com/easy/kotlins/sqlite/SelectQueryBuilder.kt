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

    private var selectionApplied = false
    private var useNativeSelection = false
    private var selection: String? = null
    private var nativeSelectionArgs: Array<out String>? = null

    fun <T> exec(f: Cursor.() -> T): T {
        return doExec().use(f)
    }

    inline fun <T : Any> parseSingle(parser: RowParser<T>): Unit = doExec().use {
        it.parseSingle(parser)
    }

    inline fun <T : Any> parseOpt(parser: RowParser<T>): Unit = doExec().use {
        it.parseOpt(parser)
    }

    inline fun <T : Any> parseList(parser: RowParser<T>): Unit = doExec().use {
        it.parseList(parser)
    }

    inline fun <T : Any> parseSingle(parser: MapRowParser<T>): T = doExec().use {
        it.parseSingle(parser)
    }

    inline fun <T : Any> parseOpt(parser: MapRowParser<T>): T? = doExec().use {
        it.parseOpt(parser)
    }

    inline fun <T : Any> parseList(parser: MapRowParser<T>): List<T> = doExec().use {
        it.parseList(parser)
    }

    @PublishedApi
    internal fun doExec(): Cursor {
        val finalSelection = if (selectionApplied) selection else null
        val finalSelectionArgs = if (selectionApplied && useNativeSelection) nativeSelectionArgs else null
        val finalColumns = if(columnsApplied) columns.toTypedArray() else null
        val finalGroupBy = if(groupByApplied) groupBy.joinToString(", ") else null
        val finalOrderBy = if(orderByApplied) orderBy.joinToString(", ") else null
        return execQuery(distinct, tableName,
                finalColumns,
                finalSelection,
                finalSelectionArgs,
                finalGroupBy,
                having,
                finalOrderBy,
                limit)
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

    fun orderBy(value: String, direction: SqlOrderDirection = SqlOrderDirection.ASC): SelectQueryBuilder {
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
        if (selectionApplied) {
            throw IllegalStateException("Query having was already applied.")
        }

        havingApplied = true
        this.having = applyArguments(having, *args)
        return this
    }

    @Deprecated("Use whereArgs(select, args) instead.", ReplaceWith("whereArgs(select, args)"))
    fun where(select: String, vararg args: Pair<String, Any>): SelectQueryBuilder {
        return whereArgs(select, *args)
    }

    fun whereArgs(select: String, vararg args: Pair<String, Any>): SelectQueryBuilder {
        if (selectionApplied) {
            throw IllegalStateException("Query selection was already applied.")
        }

        selectionApplied = true
        useNativeSelection = false
        selection = applyArguments(select, *args)
        return this
    }

    @Deprecated("Use whereArgs(select) instead.", ReplaceWith("whereArgs(select)"))
    fun where(select: String): SelectQueryBuilder {
        return whereArgs(select)
    }

    fun whereArgs(select: String): SelectQueryBuilder {
        if (selectionApplied) {
            throw IllegalStateException("Query selection was already applied.")
        }

        selectionApplied = true
        useNativeSelection = false
        selection = select
        return this
    }

    fun whereSimple(select: String, vararg args: String): SelectQueryBuilder {
        if (selectionApplied) {
            throw IllegalStateException("Query selection was already applied.")
        }

        selectionApplied = true
        useNativeSelection = true
        selection = select
        nativeSelectionArgs = args
        return this
    }

    @Deprecated("Use whereSimple() instead", replaceWith = ReplaceWith("whereSimple(select, *args)"))
    fun whereSupport(select: String, vararg args: String): SelectQueryBuilder {
        return whereSimple(select, *args)
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
        return db.query(distinct, tableName, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
    }

}