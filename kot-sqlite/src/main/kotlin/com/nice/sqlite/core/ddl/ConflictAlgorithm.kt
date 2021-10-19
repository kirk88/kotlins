@file:Suppress("unused")

package com.nice.sqlite.core.ddl

sealed class ConflictAlgorithm(
    private val name: String
) {
    object None : ConflictAlgorithm("")
    object Rollback : ConflictAlgorithm("ROLLBACK")
    object Abort : ConflictAlgorithm("ABORT")
    object Fail : ConflictAlgorithm("FAIL")
    object Ignore : ConflictAlgorithm("IGNORE")
    object Replace : ConflictAlgorithm("REPLACE")

    override fun toString(): String = name
}