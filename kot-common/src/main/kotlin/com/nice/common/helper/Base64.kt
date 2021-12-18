@file:Suppress("UNUSED")

package com.nice.common.helper

import android.util.Base64
import java.nio.charset.Charset

fun String.encodeBase64(
    charset: Charset = Charsets.UTF_8,
    flag: Int = Base64.DEFAULT
): ByteArray = Base64.encode(this.toByteArray(charset), flag)

fun String.decodeBase64(
    charset: Charset = Charsets.UTF_8,
    flag: Int = Base64.DEFAULT
): ByteArray = Base64.decode(this.toByteArray(charset), flag)

fun ByteArray.encodeBase64(
    flag: Int = Base64.DEFAULT
): ByteArray = Base64.encode(this, flag)

fun ByteArray.decodeBase64(
    flag: Int = Base64.DEFAULT
): ByteArray = Base64.decode(this, flag)

fun String.encodeBase64ToString(
    charset: Charset = Charsets.UTF_8,
    flag: Int = Base64.DEFAULT
): String = encodeBase64(charset, flag).toString(charset)

fun String.decodeBase64ToString(
    charset: Charset = Charsets.UTF_8,
    flag: Int = Base64.DEFAULT
): String = decodeBase64(charset, flag).toString(charset)

fun ByteArray.encodeBase64ToString(
    charset: Charset = Charsets.UTF_8,
    flag: Int = Base64.DEFAULT
): String = encodeBase64(flag).toString(charset)

fun ByteArray.decodeBase64ToString(
    charset: Charset = Charsets.UTF_8,
    flag: Int = Base64.DEFAULT
): String = decodeBase64(flag).toString(charset)