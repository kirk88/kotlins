@file:Suppress("UNUSED")

package com.nice.common.helper

import java.text.SimpleDateFormat
import java.util.*

private const val DEFAULT_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss"

private fun getFormatter(pattern: String): SimpleDateFormat {
    return SimpleDateFormat(pattern, Locale.getDefault())
}

fun Date.toDateString(pattern: String = DEFAULT_TIME_PATTERN): String = getFormatter(pattern).format(this)

fun String.toDate(pattern: String = DEFAULT_TIME_PATTERN): Date = requireNotNull(getFormatter(pattern).parse(this))

fun String.toDateOrNull(pattern: String = DEFAULT_TIME_PATTERN): Date? = runCatching { getFormatter(pattern).parse(this) }.getOrNull()

fun Long.asDate(): Date = Date(this)

fun String.asDate(): Date = Date(this.toLong())