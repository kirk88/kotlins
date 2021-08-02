package com.nice.sqlite.core.ddl

import com.nice.sqlite.core.Dialect
import com.nice.sqlite.core.Subject
import com.nice.sqlite.core.Table

class CreateStatement<T : Table>(
    val subject: Subject<T>,
    val definitions: Sequence<Definition>
) : Statement {

    override fun toString(dialect: Dialect): String {
        return dialect.build(this)
    }

}