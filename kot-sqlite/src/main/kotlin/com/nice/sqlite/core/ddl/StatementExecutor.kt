package com.nice.sqlite.core.ddl

import android.database.Cursor

interface StatementExecutor {

    fun execute(statement: Statement)

    fun executeUpdate(statement: UpdateStatement<*>): Int

    fun executeUpdateBatch(statement: UpdateBatchStatement<*>): Int

    fun executeDelete(statement: DeleteStatement<*>): Int

    fun executeInsert(statement: InsertStatement<*>): Long

    fun executeInsertBatch(statement: InsertBatchStatement<*>): Long

    fun executeQuery(statement: QueryStatement): Cursor

    companion object

}