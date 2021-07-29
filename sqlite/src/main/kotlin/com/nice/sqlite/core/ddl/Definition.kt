package com.nice.sqlite.core.ddl

import com.nice.sqlite.core.dml.OnceIterator

enum class SqlType {
    INTEGER,
    REAL,
    TEXT,
    BLOB
}

interface Definition : Sequence<Definition> {

    override fun iterator(): Iterator<Definition> = OnceIterator(this)

    fun render(): String

}