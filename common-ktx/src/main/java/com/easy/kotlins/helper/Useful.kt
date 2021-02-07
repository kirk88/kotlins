package com.easy.kotlins.helper

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * Create by LiZhanPing on 2020/8/25
 */
fun <T: Any> Boolean.opt(right: T, wrong: T): T {
    return if (this) right else wrong
}

fun <T: Any> Boolean.opt(right: () -> T, wrong: () -> T): T {
    return if (this) right() else wrong()
}

fun <T: Any?> Boolean.optNulls(right: T?, wrong: T?): T? {
    return if (this) right else wrong
}

fun <T: Any?> Boolean.optNulls(right: () -> T?, wrong: () -> T?): T? {
    return if (this) right() else wrong()
}

fun Any.serialize(): String? = runCatching {
    ByteArrayOutputStream().use { byteArrayOutputStream ->
        ObjectOutputStream(byteArrayOutputStream).use { objectOutputStream ->
            objectOutputStream.writeObject(this)
        }
        byteArrayOutputStream.toString("ISO-8859-1")
    }
}.getOrNull()

fun <T : Any> String.deserialize(): T? = runCatching {
    ObjectInputStream(ByteArrayInputStream(this.toByteArray(charset("ISO-8859-1")))).use {
        @Suppress("UNCHECKED_CAST")
        it.readObject() as? T
    }
}.getOrNull()