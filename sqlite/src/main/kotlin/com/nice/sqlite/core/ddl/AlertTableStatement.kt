package com.nice.sqlite.core.ddl

import com.nice.sqlite.core.Dialect
import com.nice.sqlite.core.Subject
import com.nice.sqlite.core.Table

class AlertTableStatement<T : Table>(
    val definitions: Sequence<Definition>,
    val subject: Subject<T>
) {
    fun toString(dialect: Dialect): String {
        return dialect.build(this)
    }
}