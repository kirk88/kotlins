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

@file:Suppress("unused")

package com.easy.kotlins.sqlite.db

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

abstract class UpdateQueryBuilder(
    private val tableName: String,
    private val values: Array<out Pair<String, Any?>>
) {

    private var whereCauseApplied = false
    private var useNativeWhereCause = false
    private var simpleWhereCause: String? = null
    private var nativeWhereArgs: Array<out String>? = null

    fun whereArgs(whereCause: String, vararg whereArgs: Pair<String, Any>): UpdateQueryBuilder {
        if (whereCauseApplied) {
            throw IllegalStateException("Query selection was already applied.")
        }

        whereCauseApplied = true
        useNativeWhereCause = false
        val whereArgsMap = whereArgs.fold(hashMapOf<String, Any>()) { map, arg ->
            map[arg.first] = arg.second
            map
        }
        simpleWhereCause = applyArguments(whereCause, whereArgsMap)
        return this
    }

    fun whereArgs(whereCause: String): UpdateQueryBuilder {
        if (whereCauseApplied)
            throw IllegalStateException("Query selection was already applied.")

        whereCauseApplied = true
        useNativeWhereCause = false
        simpleWhereCause = whereCause
        return this
    }

    fun whereSimple(whereCause: String, vararg whereArgs: Any): UpdateQueryBuilder {
        if (whereCauseApplied)
            throw IllegalStateException("Query selection was already applied.")

        whereCauseApplied = true
        useNativeWhereCause = true
        simpleWhereCause = whereCause
        nativeWhereArgs = whereArgs.map { it.toString() }.toTypedArray()
        return this
    }

    fun exec(conflictAlgorithm: Int = SQLiteDatabase.CONFLICT_NONE): Int {
        val finalSelection = if (whereCauseApplied) simpleWhereCause else null
        val finalSelectionArgs =
            if (whereCauseApplied && useNativeWhereCause) nativeWhereArgs else null
        return execUpdate(
            tableName,
            values.toContentValues(),
            finalSelection,
            finalSelectionArgs,
            conflictAlgorithm
        )
    }

    protected abstract fun execUpdate(
        table: String,
        values: ContentValues,
        whereClause: String?,
        whereArgs: Array<out String>?,
        conflictAlgorithm: Int = SQLiteDatabase.CONFLICT_NONE
    ): Int
}

class AndroidDatabaseUpdateQueryBuilder(
    private val db: SQLiteDatabase,
    table: String,
    values: Array<out Pair<String, Any?>>
) : UpdateQueryBuilder(table, values) {

    override fun execUpdate(
        table: String,
        values: ContentValues,
        whereClause: String?,
        whereArgs: Array<out String>?,
        conflictAlgorithm: Int
    ) = db.updateWithOnConflict(table, values, whereClause, whereArgs, conflictAlgorithm)
}
