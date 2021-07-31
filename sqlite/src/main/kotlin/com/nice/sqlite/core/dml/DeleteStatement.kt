package com.nice.sqlite.core.dml

import com.nice.sqlite.core.Dialect
import com.nice.sqlite.core.Subject
import com.nice.sqlite.core.Table
import com.nice.sqlite.core.ddl.Statement

class DeleteStatement<T : Table>(
    val subject: Subject<T>,
    val whereClause: WhereClause<T>? = null
): Statement {

    override fun toString(dialect: Dialect): String {
        return dialect.build(this)
    }
}