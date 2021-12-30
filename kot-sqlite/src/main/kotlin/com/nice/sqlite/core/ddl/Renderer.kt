package com.nice.sqlite.core.ddl

import java.nio.charset.StandardCharsets

interface Renderer {

    fun render(): String

}

interface FullRenderer : Renderer {

    fun fullRender(): String

}

internal fun String.addSurrounding(prefix: CharSequence = "\"", suffix: CharSequence = "\"") = "$prefix$$this$suffix"

internal fun Any?.toSqlString(): String {
    return when (this) {
        null -> "NULL"
        is String -> "'${replace("'", "''")}'"
        is Number -> toString()
        is Boolean -> if (this) "1" else "0"
        is ByteArray -> "'${toString(StandardCharsets.UTF_8)}'"
        is Defined -> render()
        else -> error("Unsupported value type: ${javaClass.name}")
    }
}