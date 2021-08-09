package com.nice.sqlite.core.dml

import com.nice.sqlite.core.Dialect
import com.nice.sqlite.core.Subject
import com.nice.sqlite.core.Table
import com.nice.sqlite.core.ddl.Assignment
import com.nice.sqlite.core.ddl.ConflictAlgorithm
import com.nice.sqlite.core.ddl.Statement

class UpdateStatement<T : Table>(
    val subject: Subject<T>,
    val assignments: Sequence<Assignment>,
    val conflictAlgorithm: ConflictAlgorithm,
    val whereClause: WhereClause<T>? = null
) : Statement {

    override fun toString(dialect: Dialect): String {
        return dialect.build(this)
    }
}