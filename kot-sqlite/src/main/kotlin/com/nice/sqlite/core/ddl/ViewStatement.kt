package com.nice.sqlite.core.ddl

import com.nice.sqlite.core.Dialect
import com.nice.sqlite.core.ViewSubject

class ViewCreateStatement(
    val subject: ViewSubject,
) : Statement {

    override fun toString(dialect: Dialect): String = dialect.build(this)

}

class ViewSelectStatement(
    val subject: ViewSubject
) : QueryStatement {

    override fun toString(dialect: Dialect): String = dialect.build(this)

}