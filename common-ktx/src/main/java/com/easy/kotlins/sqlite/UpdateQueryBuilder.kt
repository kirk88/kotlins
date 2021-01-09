@file:Suppress("unused")

package com.easy.kotlins.sqlite.db

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

abstract class UpdateQueryBuilder(
    private val table: String,
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

    fun execute(conflictAlgorithm: Int = SQLiteDatabase.CONFLICT_NONE): Int {
        val finalSelection = if (whereCauseApplied) simpleWhereCause else null
        val finalSelectionArgs =
            if (whereCauseApplied && useNativeWhereCause) nativeWhereArgs else null
        return update(
            table,
            values.toContentValues(),
            finalSelection,
            finalSelectionArgs,
            conflictAlgorithm
        )
    }

    protected abstract fun update(
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

    override fun update(
        table: String,
        values: ContentValues,
        whereClause: String?,
        whereArgs: Array<out String>?,
        conflictAlgorithm: Int
    ) = db.updateWithOnConflict(table, values, whereClause, whereArgs, conflictAlgorithm)
}
