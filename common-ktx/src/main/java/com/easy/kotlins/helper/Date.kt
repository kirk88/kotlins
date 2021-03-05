package com.easy.kotlins.helper

import java.text.SimpleDateFormat
import java.util.*

fun Date.toFormattedString(pattern: String = "yyyy-MM-dd HH:mm:ss"): String =
    SimpleDateFormat(pattern, Locale.getDefault()).format(this)

fun Long.toDate(): Date = Date(this)

fun String.toDate(): Date = Date(this.toLong())

fun String.toDateOrNull(): Date? =
    runCatching { toDate() }.getOrNull()

fun String.parseDate(pattern: String = "yyyy-MM-dd HH:mm:ss"): Date =
    runCatching { SimpleDateFormat(pattern, Locale.getDefault()).parse(this) }.getOrElse { Date() }