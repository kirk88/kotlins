package com.nice.sqlite.core.dml

import com.nice.sqlite.core.Dialect
import com.nice.sqlite.core.Subject
import com.nice.sqlite.core.Table
import com.nice.sqlite.core.ddl.Conflict
import com.nice.sqlite.core.ddl.Statement

class UpdateStatement<T : Table>(
    val assignments: Sequence<Assignment>,
    val subject: Subject<T>,
    val conflict: Conflict,
    val whereClause: WhereClause<T>? = null
) : Statement {

    override fun toString(dialect: Dialect): String {
        return dialect.build(this)
    }
}