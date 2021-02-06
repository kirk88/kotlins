@file:Suppress("unused")

package com.easy.kotlins.sqlite.db

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.easy.kotlins.sqlite.SqlColumnCell
import com.easy.kotlins.sqlite.SqlWhereCondition
import com.easy.kotlins.sqlite.applyArguments

abstract class UpdateQueryBuilder(
    private val table: String,
    private val cells: Array<out SqlColumnCell>
) {

    private var whereCauseApplied = false
    private var updateWhereCause: String? = null
    private var updateWhereArgs: Array<out String>? = null

    fun whereArgs(whereCondition: SqlWhereCondition){
        if (whereCauseApplied) {
            throw IllegalStateException("Query selection was already applied.")
        }

        whereCauseApplied = true
        updateWhereCause = whereCondition.whereCause
        updateWhereArgs = whereCondition.whereArgs
    }

    fun whereArgs(whereCause: String, vararg whereArgs: Pair<String, Any>): UpdateQueryBuilder {
        if (whereCauseApplied) {
            throw IllegalStateException("Query selection was already applied.")
        }

        whereCauseApplied = true
        val whereArgsMap = whereArgs.fold(hashMapOf<String, Any>()) { map, arg ->
            map[arg.first] = arg.second
            map
        }
        updateWhereCause = applyArguments(whereCause, whereArgsMap)
        return this
    }

    fun whereArgs(whereCause: String): UpdateQueryBuilder {
        if (whereCauseApplied)
            throw IllegalStateException("Query selection was already applied.")

        whereCauseApplied = true
        updateWhereCause = whereCause
        return this
    }

    fun whereSimple(whereCause: String, vararg whereArgs: Any): UpdateQueryBuilder {
        if (whereCauseApplied)
            throw IllegalStateException("Query selection was already applied.")

        whereCauseApplied = true
        updateWhereCause = whereCause
        updateWhereArgs = whereArgs.map { it.toString() }.toTypedArray()
        return this
    }

    fun execute(conflictAlgorithm: Int = SQLiteDatabase.CONFLICT_NONE): Int {
        val finalSelection = if (whereCauseApplied) updateWhereCause else null
        val finalSelectionArgs = if (whereCauseApplied) updateWhereArgs else null
        return update(
            table,
            cells.toContentValues(),
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
    cells: Array<out SqlColumnCell>
) : UpdateQueryBuilder(table, cells) {

    override fun update(
        table: String,
        values: ContentValues,
        whereClause: String?,
        whereArgs: Array<out String>?,
        conflictAlgorithm: Int
    ) = db.updateWithOnConflict(table, values, whereClause, whereArgs, conflictAlgorithm)
}
