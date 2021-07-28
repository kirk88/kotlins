package com.nice.sqlite.core.dml

import com.nice.sqlite.core.Dialect
import com.nice.sqlite.core.Subject
import com.nice.sqlite.core.Table
import com.nice.sqlite.core.ddl.Conflict

class UpdateStatement<T : Table>(
    val assignments: Sequence<Assignment>,
    val subject: Subject<T>,
    val whereClause: WhereClause<T>?,
    val conflict: Conflict
) {

    fun toString(dialect: Dialect): String {
        return dialect.build(this)
    }
}