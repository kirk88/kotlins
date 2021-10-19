package com.nice.sqlite.core.ddl


interface Renderer {

    fun render(): String

}

interface FullRenderer : Renderer {

    fun fullRender(): String

}

internal fun String.surrounding() = "\"$this\""

internal fun Any?.toSqlString(): String {
    return when (this) {
        null -> "NULL"
        is Number -> toString()
        is Boolean -> if (this) "1" else "0"
        else -> "'${toString().replace("'", "''")}'"
    }
}