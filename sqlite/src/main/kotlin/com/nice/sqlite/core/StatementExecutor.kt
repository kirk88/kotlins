package com.nice.sqlite.core

import android.database.Cursor
import com.nice.sqlite.core.ddl.Statement

interface StatementExecutor {

    fun execute(statement: Statement)

    fun executeUpdateDelete(statement: Statement): Int

    fun executeInsert(statement: Statement): Long

    fun queryForCursor(statement: Statement): Cursor

}