package com.nice.sqlite.core.ddl

import com.nice.sqlite.core.Dialect
import com.nice.sqlite.core.Table
import com.nice.sqlite.core.TableSubject
import com.nice.sqlite.core.dml.WhereClause

class DeleteStatement<T : Table>(
    val subject: TableSubject<T>,
    val whereClause: WhereClause<T>? = null
) : Statement {

    override fun toString(dialect: Dialect): String = dialect.build(this)
}