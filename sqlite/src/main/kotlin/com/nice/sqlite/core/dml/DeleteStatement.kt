package com.nice.sqlite.core.dml

import com.nice.sqlite.core.Dialect
import com.nice.sqlite.core.Subject
import com.nice.sqlite.core.Table

class DeleteStatement<T: Table>(
    val subject: Subject<T>,
    val whereClause: WhereClause<T>?) {

    fun toString(dialect: Dialect): String {
        return dialect.build(this)
    }
}