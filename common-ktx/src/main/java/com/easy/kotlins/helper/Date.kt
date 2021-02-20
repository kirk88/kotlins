package com.easy.kotlins.helper

import java.text.SimpleDateFormat
import java.util.*

fun Date.formatDate(pattern: String = "yyyy-MM-dd HH:mm:ss"): String =
    SimpleDateFormat(pattern, Locale.getDefault()).format(this)

fun Long.formatDate(pattern: String = "yyyy-MM-dd HH:mm:ss"): String =
    Date(this).formatDate(pattern)

fun String.toDate(pattern: String = "yyyy-MM-dd HH:mm:ss"): Date =
    runCatching { SimpleDateFormat(pattern, Locale.getDefault()).parse(this) }.getOrElse { Date() }

fun String.asDate(): Date {
    var time = this.toLong()
    if (this.length < 13) {
        time *= 1000
    }
    return Date(time)
}