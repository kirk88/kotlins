package com.nice.sqlite.core.ddl

import android.database.Cursor

interface StatementExecutor {

    fun execute(statement: Statement)

    fun executeUpdateDelete(statement: Statement): Int

    fun executeInsert(statement: Statement): Long

    fun executeBatchInsert(statement: Statement): Long

    fun executeQuery(statement: Statement): Cursor

}