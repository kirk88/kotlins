@file:Suppress("unused")

package com.nice.kotlins.sqlite.db

import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

class SupportDatabaseUpdateBuilder(
    private val database: SupportSQLiteDatabase,
    private val table: String,
) {

    private val values: MutableList<SqlColumnElement> = mutableListOf()

    private var whereApplied = false
    private var whereClause: String? = null
    private var whereArgs: Array<Any>? = null

    fun values(vararg values: SqlColumnElement) = apply {
        this.values.addAll(values)
    }

    fun values(values: Collection<SqlColumnElement>) = apply {
        this.values.addAll(values)
    }

    fun where(condition: SqlWhereCondition) = apply {
        check(!this.whereApplied) { "Update where was already applied" }
        this.whereApplied = true
        this.whereClause = condition.whereClause
        this.whereArgs = condition.whereArgs
    }

    fun where(whereClause: String, vararg whereArgs: Pair<String, Any>) = apply {
        check(!this.whereApplied) { "Update where was already applied" }
        this.whereApplied = true
        this.whereClause = applyArguments(whereClause, *whereArgs)
    }

    fun where(whereClause: String) = apply {
        check(!this.whereApplied) { "Update where was already applied" }
        this.whereApplied = true
        this.whereClause = whereClause
    }

    fun where(whereClause: String, vararg whereArgs: Any) = apply {
        check(!this.whereApplied) { "Update where was already applied" }
        this.whereApplied = true
        this.whereClause = whereClause
        this.whereArgs = arrayOf(*whereArgs)
    }

    fun execute(conflictAlgorithm: Int = SQLiteDatabase.CONFLICT_NONE): Int {
        check(values.isNotEmpty()) { "Update values is empty" }
        val values = values.toContentValues()
        val finalWhereClause = if (whereApplied) whereClause else null
        val finalWhereArgs = if (whereApplied) whereArgs else null
        return database.update(
            table,
            conflictAlgorithm,
            values,
            finalWhereClause,
            finalWhereArgs
        )
    }

}
