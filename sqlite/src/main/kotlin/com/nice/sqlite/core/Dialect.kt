package com.nice.sqlite.core

import com.nice.sqlite.core.ddl.AlterStatement
import com.nice.sqlite.core.ddl.CreateStatement
import com.nice.sqlite.core.ddl.DropStatement
import com.nice.sqlite.core.dml.*

interface Dialect {

    fun <T : Table> build(statement: CreateStatement<T>): String
    fun <T : Table> build(statement: AlterStatement<T>): String
    fun <T : Table> build(statement: DropStatement<T>): String

    fun <T : Table> build(statement: SelectStatement<T>): String
    fun <T : Table, T2 : Table> build(statement: Select2Statement<T, T2>): String
    fun <T : Table, T2 : Table, T3 : Table> build(statement: Select3Statement<T, T2, T3>): String
    fun <T : Table, T2 : Table, T3 : Table, T4 : Table> build(statement: Select4Statement<T, T2, T3, T4>): String

    fun <T : Table> build(statement: InsertStatement<T>): String
    fun <T : Table> build(statement: UpdateStatement<T>): String
    fun <T : Table> build(statement: DeleteStatement<T>): String

}