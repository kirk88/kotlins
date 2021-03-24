@file:Suppress("unused")

package com.easy.kotlins.helper

import java.text.SimpleDateFormat
import java.util.*

private fun getFormatter(pattern: String): SimpleDateFormat {
    return SimpleDateFormat(pattern, Locale.getDefault())
}

fun Date.formatDate(pattern: String = "yyyy-MM-dd HH:mm:ss"): String =
    getFormatter(pattern).format(this)

fun String.parseDate(pattern: String = "yyyy-MM-dd HH:mm:ss"): Date =
    runCatching { getFormatter(pattern).parse(this) }.getOrElse { Date() }

fun Long.toDate(): Date = Date(this)

fun String.toDate(): Date = Date(this.toLong())

fun String.toDateOrNull(): Date? = runCatching { toDate() }.getOrNull()
