package com.nice.sqlite.core.ddl

import com.nice.sqlite.core.Dialect

interface Statement {

    fun toString(dialect: Dialect): String

}