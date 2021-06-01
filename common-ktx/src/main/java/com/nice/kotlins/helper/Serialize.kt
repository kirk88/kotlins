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
        byteArrayOutputStream.toString(Charsets.ISO_8859_1.name())
    }
}.getOrNull()

fun <T : Any> String.deserialize(): T? = runCatching {
    ByteArrayInputStream(this.toByteArray(Charsets.ISO_8859_1)).use { byteArrayInputStream ->
        @Suppress("UNCHECKED_CAST")
        ObjectInputStream(byteArrayInputStream).use { objectInputStream ->
            objectInputStream.readObject()
        } as? T
    }
}.getOrNull()