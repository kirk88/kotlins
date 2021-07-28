package com.nice.sqlite.core.ddl

sealed class Conflict(
    private val name: String
) {
    object None : Conflict("")

    object Rollback : Conflict("ROLLBACK")

    object Abort : Conflict("ABORT")

    object Fail : Conflict("FAIL")

    object Ignore : Conflict("IGNORE")

    object Replace : Conflict("REPLACE")

    override fun toString(): String {
        return name
    }
}