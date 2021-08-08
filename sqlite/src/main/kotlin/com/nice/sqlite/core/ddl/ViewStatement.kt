package com.nice.sqlite.core.ddl

import com.nice.sqlite.core.Dialect
import com.nice.sqlite.core.StatementViewSubject
import com.nice.sqlite.core.dml.QueryStatement

class CreateViewStatement(
    val subject: StatementViewSubject,
    val statement: QueryStatement
) : Statement {

    override fun toString(dialect: Dialect): String {
        return dialect.build(this)
    }

}

class SelectViewStatement(
    val subject: StatementViewSubject
) : Statement {

    override fun toString(dialect: Dialect): String {
        return dialect.build(this)
    }

}