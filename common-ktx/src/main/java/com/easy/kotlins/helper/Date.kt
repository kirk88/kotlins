package com.easy.kotlins.helper

import java.text.SimpleDateFormat
import java.util.*


/**
 * Create by LiZhanPing on 2020/8/28
 */


fun Date.formatDate(pattern: String = "yyyy-MM-dd HH:mm:ss") : String = SimpleDateFormat(pattern, Locale.getDefault()).format(this)

fun Long.formatDate(pattern: String = "yyyy-MM-dd HH:mm:ss"): String = Date(this).formatDate(pattern)

fun String.formatDate(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    var time = this.toLong()
    if (this.length < 13) {
        time *= 1000
    }
    return time.formatDate(pattern)
}

fun String.parseDate(pattern: String = "yyyy-MM-dd HH:mm:ss"): Date = runCatching { SimpleDateFormat(pattern, Locale.getDefault()).parse(this) }.getOrDefault(Date())