package com.nice.sqlite.core.ddl

data class Executable(
    val sql: String,
    val assignments: Sequence<Assignment>
)