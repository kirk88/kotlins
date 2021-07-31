package com.nice.sqlite.core

import android.database.Cursor
import com.nice.sqlite.core.ddl.Statement

interface Database {

    fun execute(statement: Statement)

    fun executeUpdateDelete(statement: Statement): Int

    fun executeInsert(statement: Statement): Long

    fun query(statement: Statement): Cursor

}