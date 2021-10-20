package com.nice.sqlite.core.ddl

import com.nice.sqlite.core.Dialect
import com.nice.sqlite.core.Table
import com.nice.sqlite.core.TriggerSubject

class TriggerCreateStatement<T: Table>(
    val subject: TriggerSubject<T>
): Statement {

    override fun toString(dialect: Dialect): String = dialect.build(this)

}