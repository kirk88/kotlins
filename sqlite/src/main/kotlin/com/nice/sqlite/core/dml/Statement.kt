package com.nice.sqlite.core.dml

import com.nice.sqlite.core.Dialect

interface Statement {

    fun toString(dialect: Dialect): String

}