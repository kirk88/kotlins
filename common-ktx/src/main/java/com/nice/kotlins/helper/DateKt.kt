@file:Suppress("unused")

package com.nice.kotlins.helper

import java.text.SimpleDateFormat
import java.util.*

private const val DEFAULT_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss"

private fun getFormatter(pattern: String): SimpleDateFormat {
    return SimpleDateFormat(pattern, Locale.getDefault())
}

fun Date.formatDate(pattern: String = DEFAULT_TIME_PATTERN): String =
    getFormatter(pattern).format(this)

fun String.parseDate(pattern: String = DEFAULT_TIME_PATTERN): Date =
    runCatching { getFormatter(pattern).parse(this) }.getOrElse { Date() }

fun Long.toDate(): Date = Date(this)

fun String.toDate(): Date = Date(this.toLong())

fun String.toDateOrNull(): Date? = runCatching { toDate() }.getOrNull()
