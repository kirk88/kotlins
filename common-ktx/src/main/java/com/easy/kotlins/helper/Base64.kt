@file:Suppress("unused")

package com.easy.kotlins.helper

import android.util.Base64
import java.nio.charset.Charset

fun String.encodeBase64ToString(charset: Charset = Charsets.UTF_8, flag: Int = Base64.DEFAULT): String
= Base64.encodeToString(this.toByteArray(charset), flag)

fun String.decodeBase64ToString(charset: Charset = Charsets.UTF_8, flag: Int = Base64.DEFAULT): String
    = Base64.decode(this.toByteArray(charset), flag).decodeToString()