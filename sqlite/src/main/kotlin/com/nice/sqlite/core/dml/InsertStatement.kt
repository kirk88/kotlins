package com.nice.sqlite.core.dml

import com.nice.sqlite.core.Dialect
import com.nice.sqlite.core.Subject
import com.nice.sqlite.core.Table
import com.nice.sqlite.core.ddl.Assignment
import com.nice.sqlite.core.ddl.Conflict
import com.nice.sqlite.core.ddl.Statement

class InsertStatement<T : Table>(
    val assignments: Sequence<Assignment>,
    val subject: Subject<T>,
    val conflict: Conflict
) : Statement {

    override fun toString(dialect: Dialect): String {
        return dialect.build(this)
    }

}