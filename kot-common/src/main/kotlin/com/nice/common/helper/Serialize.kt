@file:Suppress("UNUSED")

package com.nice.common.helper

import java.io.*

fun <T : Serializable> T.serialize(): String {
    return ByteArrayOutputStream().let { bos ->
        ObjectOutputStream(bos).use {
            it.writeObject(this)
        }
        bos.toString(Charsets.ISO_8859_1.name())
    }
}

fun <T : Serializable> String.deserialize(): T {
    @Suppress("UNCHECKED_CAST")
    return ObjectInputStream(ByteArrayInputStream(toByteArray(Charsets.ISO_8859_1))).use {
        it.readObject()
    } as T
}