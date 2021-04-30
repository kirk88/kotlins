@file:Suppress("unused")

package com.nice.kotlins.sqlite.db

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

abstract class UpdateQueryBuilder(private val table: String) {

    private val values: MutableList<SqlColumnElement> = mutableListOf()

    private var whereApplied = false
    private var whereClause: String? = null
    private var whereArgs: Array<String>? = null

    fun values(vararg values: SqlColumnElement): UpdateQueryBuilder {
        this.values.addAll(values)
        return this
    }

    fun values(values: Collection<SqlColumnElement>): UpdateQueryBuilder {
        this.values.addAll(values)
        return this
    }

    fun where(condition: SqlWhereCondition): UpdateQueryBuilder {
        check(!this.whereApplied) { "Update where was already applied" }

        this.whereApplied = true
        this.whereClause = condition.whereClause
        this.whereArgs = condition.whereArgs.map { it.toString() }.toTypedArray()
        return this
    }

    fun where(whereClause: String, vararg whereArgs: Pair<String, Any>): UpdateQueryBuilder {
        check(!this.whereApplied) { "Update where was already applied" }

        this.whereApplied = true
        val whereArgsMap = whereArgs.fold(mutableMapOf<String, Any>()) { map, arg ->
            map[arg.first] = arg.second
            map
        }
        this.whereClause = applyArguments(whereClause, whereArgsMap)
        return this
    }

    fun where(whereClause: String): UpdateQueryBuilder {
        check(!this.whereApplied) { "Update where was already applied" }

        this.whereApplied = true
        this.whereClause = whereClause
        return this
    }

    fun where(whereClause: String, vararg whereArgs: Any): UpdateQueryBuilder {
        check(!this.whereApplied) { "Update where was already applied" }

        this.whereApplied = true
        this.whereClause = whereClause
        this.whereArgs = whereArgs.map { it.toString() }.toTypedArray()
        return this
    }

    fun execute(conflictAlgorithm: Int = SQLiteDatabase.CONFLICT_NONE): Int {
        check(this.values.isNotEmpty()) { "Update values is empty" }

        val values = this.values.toContentValues()
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
