@file:Suppress("unused")

package com.easy.kotlins.sqlite.db

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.easy.kotlins.sqlite.SqlColumnElement
import com.easy.kotlins.sqlite.SqlWhereCondition
import com.easy.kotlins.sqlite.applyArguments

abstract class UpdateQueryBuilder(private val table: String) {

    private val values: MutableList<SqlColumnElement> = mutableListOf()

    private var whereApplied = false
    private var whereClause: String? = null
    private var whereArgs: Array<out String>? = null

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

    fun where(condition: SqlWhereCondition): UpdateQueryBuilder {
        if (this.whereApplied) {
            throw IllegalStateException("Query selection was already applied.")
        }

        this.whereApplied = true
        this.whereClause = condition.whereClause
        this.whereArgs = condition.whereArgs
        return this
    }

    fun where(whereClause: String, vararg whereArgs: Pair<String, Any>): UpdateQueryBuilder {
        if (this.whereApplied) {
            throw IllegalStateException("Query selection was already applied.")
        }

        this.whereApplied = true
        val whereArgsMap = whereArgs.fold(hashMapOf<String, Any>()) { map, arg ->
            map[arg.first] = arg.second
            map
        }
        this.whereClause = applyArguments(whereClause, whereArgsMap)
        return this
    }

    fun where(whereClause: String): UpdateQueryBuilder {
        if (this.whereApplied)
            throw IllegalStateException("Query selection was already applied.")

        this.whereApplied = true
        this.whereClause = whereClause
        return this
    }

    fun where(whereClause: String, vararg whereArgs: Any): UpdateQueryBuilder {
        if (this.whereApplied)
            throw IllegalStateException("Query selection was already applied.")

        this.whereApplied = true
        this.whereClause = whereClause
        this.whereArgs = whereArgs.map { it.toString() }.toTypedArray()
        return this
    }

    fun execute(conflictAlgorithm: Int = SQLiteDatabase.CONFLICT_NONE): Int {
        val values = if (this.values.isNotEmpty())
            this.values.toContentValues()
        else
            throw IllegalArgumentException("Empty values")
        val finalWhereClause = if (this.whereApplied) this.whereClause else null
        val finalWhereArgs = if (this.whereApplied) this.whereArgs else null
        return update(
            table,
            values,
            finalWhereClause,
            finalWhereArgs,
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
