package com.easy.kotlins.helper

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * Create by LiZhanPing on 2020/8/25
 */

inline fun <T, R> T.transform(crossinline transform: (T) -> R): R {
    return transform(this)
}

fun <T> Boolean.opt(right: T, wrong: T): T {
    return if (this) right else wrong
}

fun <T> Boolean.opt(right: () -> T, wrong: () -> T): T {
    return if (this) right() else wrong()
}

fun Boolean.opt(rightAction: () -> Unit, wrongAction: () -> Unit){
    if(this) rightAction() else wrongAction()
}

fun Any.serialize(): String? = runCatching {
    ByteArrayOutputStream().use { byteArrayOutputStream ->
        ObjectOutputStream(byteArrayOutputStream).use { objectOutputStream ->
            objectOutputStream.writeObject(this)
        }
        byteArrayOutputStream.toString("ISO-8859-1")
    }
}.getOrNull()

@Suppress("UNCHECKED_CAST")
fun <T : Any> String.deserialize(): T? = runCatching {
    ObjectInputStream(ByteArrayInputStream(this.toByteArray(charset("ISO-8859-1")))).use {
        it.readObject() as? T
    }
}.getOrNull()