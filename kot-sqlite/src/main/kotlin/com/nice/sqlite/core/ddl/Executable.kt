package com.nice.sqlite.core.ddl

data class Executable(
    val sql: String,
    val values: Shell<ColumnValue>
)