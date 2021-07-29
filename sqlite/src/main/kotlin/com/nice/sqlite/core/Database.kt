package com.nice.sqlite.core

import com.nice.sqlite.core.dml.Statement

interface Database {

    fun execute(statement: Statement)

    fun executeUpdateDelete(statement: Statement): Int

    fun executeInsert(statement: Statement): Long

    fun query(statement: Statement): Long

}