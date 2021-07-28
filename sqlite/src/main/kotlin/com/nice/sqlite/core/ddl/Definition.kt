package com.nice.sqlite.core.ddl

import com.nice.sqlite.core.dml.OnceIterator
import com.nice.sqlite.core.dml.Projection

enum class SqlType {
    INTEGER,
    REAL,
    TEXT,
    BLOB
}

interface Definition: Sequence<Definition> {

    val column: Projection.Column
    val type: SqlType
    val defaultValue: Any?

    override fun iterator(): Iterator<Definition> = OnceIterator(this)

}