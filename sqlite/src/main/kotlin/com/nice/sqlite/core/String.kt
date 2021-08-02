package com.nice.sqlite.core

internal fun Any?.toSqlString(): String {
    return when (this) {
        null -> "NULL"
        is Number -> toString()
        is Boolean -> if (this) "1" else "0"
        else -> "'${toString().replace("'", "''")}'"
    }
}

internal fun String.render(): String = "\"$this\""