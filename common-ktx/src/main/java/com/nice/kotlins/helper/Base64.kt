@file:Suppress("unused")

package com.nice.kotlins.helper

import android.util.Base64
import java.nio.charset.Charset

fun String.encodeBase64ToString(
        charset: Charset = Charsets.UTF_8,
        flag: Int = Base64.DEFAULT
): String = Base64.encode(this.toByteArray(charset), flag).decodeToString()

fun String.decodeBase64ToString(
        charset: Charset = Charsets.UTF_8,
        flag: Int = Base64.DEFAULT
): String = Base64.decode(this.toByteArray(charset), flag).decodeToString()

fun String.encodeBase64(
        charset: Charset = Charsets.UTF_8,
        flag: Int = Base64.DEFAULT
): ByteArray = Base64.encode(this.toByteArray(charset), flag)

fun String.decodeBase64(
        charset: Charset = Charsets.UTF_8,
        flag: Int = Base64.DEFAULT
): ByteArray = Base64.decode(this.toByteArray(charset), flag)

fun ByteArray.encodeBase64ToString(
        flag: Int = Base64.DEFAULT
): String = Base64.encode(this, flag).decodeToString()

fun ByteArray.decodeBase64ToString(
        flag: Int = Base64.DEFAULT
): String = Base64.decode(this, flag).decodeToString()


fun ByteArray.encodeBase64(
        flag: Int = Base64.DEFAULT
): ByteArray = Base64.encode(this, flag)

fun ByteArray.decodeBase64(
        flag: Int = Base64.DEFAULT
): ByteArray = Base64.decode(this, flag)