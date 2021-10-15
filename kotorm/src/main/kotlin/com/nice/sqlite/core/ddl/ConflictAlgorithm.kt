@file:Suppress("unused")

package com.nice.sqlite.core.ddl

enum class ConflictAlgorithm(
    private val value: String
) {
    None(""),
    Rollback("ROLLBACK"),
    Abort("ABORT"),
    Fail("FAIL"),
    Ignore("IGNORE"),
    Replace("REPLACE");

    override fun toString(): String = value
}