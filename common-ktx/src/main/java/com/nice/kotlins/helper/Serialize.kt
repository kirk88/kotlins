@file:Suppress("unused")

package com.nice.kotlins.helper

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

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