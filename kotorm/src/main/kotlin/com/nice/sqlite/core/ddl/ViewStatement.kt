package com.nice.sqlite.core.ddl

import com.nice.sqlite.core.Dialect
import com.nice.sqlite.core.ViewSubject
import com.nice.sqlite.core.dml.QueryStatement

class CreateViewStatement(
    val subject: ViewSubject,
    val statement: QueryStatement
) : Statement {

    override fun toString(dialect: Dialect): String {
        return dialect.build(this)
    }

}

class SelectViewStatement(
    val subject: ViewSubject
) : QueryStatement {

    override fun toString(dialect: Dialect): String {
        return dialect.build(this)
    }

}