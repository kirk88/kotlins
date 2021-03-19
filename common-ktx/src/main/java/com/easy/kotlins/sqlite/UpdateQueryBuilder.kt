@file:Suppress("unused")

package com.easy.kotlins.sqlite.db

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.easy.kotlins.sqlite.SqlColumnElement
import com.easy.kotlins.sqlite.SqlWhereCondition
import com.easy.kotlins.sqlite.applyArguments

abstract class UpdateQueryBuilder(private val table: String) {

    private val values: MutableList<SqlColumnElement> = mutableListOf()

    private var whereCauseApplied = false
    private var updateWhereCause: String? = null
    private var updateWhereArgs: Array<out String>? = null

    fun value(value: SqlColumnElement): UpdateQueryBuilder {
        this.values.add(value)
        return this
    }

    fun values(vararg values: SqlColumnElement): UpdateQueryBuilder {
        this.values.addAll(values)
        return this
    }

    fun values(values: List<SqlColumnElement>): UpdateQueryBuilder {
        this.values.addAll(values)
        return this
    }

    fun whereArgs(whereCondition: SqlWhereCondition): UpdateQueryBuilder {
        if (this.whereCauseApplied) {
            throw IllegalStateException("Query selection was already applied.")
        }

        this.whereCauseApplied = true
        this.updateWhereCause = whereCondition.whereCause
        this.updateWhereArgs = whereCondition.whereArgs
        return this
    }

    fun whereArgs(whereCause: String, vararg whereArgs: Pair<String, Any>): UpdateQueryBuilder {
        if (this.whereCauseApplied) {
            throw IllegalStateException("Query selection was already applied.")
        }

        this.whereCauseApplied = true
        val whereArgsMap = whereArgs.fold(hashMapOf<String, Any>()) { map, arg ->
            map[arg.first] = arg.second
            map
        }
        this.updateWhereCause = applyArguments(whereCause, whereArgsMap)
        return this
    }

    fun whereArgs(whereCause: String): UpdateQueryBuilder {
        if (this.whereCauseApplied)
            throw IllegalStateException("Query selection was already applied.")

        this.whereCauseApplied = true
        this.updateWhereCause = whereCause
        return this
    }

    fun whereSimple(whereCause: String, vararg whereArgs: Any): UpdateQueryBuilder {
        if (this.whereCauseApplied)
            throw IllegalStateException("Query selection was already applied.")

        this.whereCauseApplied = true
        this.updateWhereCause = whereCause
        this.updateWhereArgs = whereArgs.map { it.toString() }.toTypedArray()
        return this
    }

    fun execute(conflictAlgorithm: Int = SQLiteDatabase.CONFLICT_NONE): Int {
        val values = if (this.values.isNotEmpty())
            this.values.toContentValues()
        else
            throw IllegalArgumentException("Empty values")
        val finalSelection = if (this.whereCauseApplied) this.updateWhereCause else null
        val finalSelectionArgs = if (this.whereCauseApplied) this.updateWhereArgs else null
        return update(
            table,
            values,
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
    table: String
) : UpdateQueryBuilder(table) {

    override fun update(
        table: String,
        values: ContentValues,
        whereClause: String?,
        whereArgs: Array<out String>?,
        conflictAlgorithm: Int
    ) = db.updateWithOnConflict(table, values, whereClause, whereArgs, conflictAlgorithm)
}
