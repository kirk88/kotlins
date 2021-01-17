package com.easy.kotlins.helper

import java.text.SimpleDateFormat
import java.util.*


/**
 * Create by LiZhanPing on 2020/8/28
 */


fun Date.format(pattern: String = "yyyy-MM-dd HH:mm:ss") : String? = SimpleDateFormat(pattern, Locale.getDefault()).format(this)

fun String.parseAsDate(pattern: String = "yyyy-MM-dd HH:mm:ss"): Date = runCatching { SimpleDateFormat(pattern, Locale.getDefault()).parse(this) }.getOrDefault(Date())