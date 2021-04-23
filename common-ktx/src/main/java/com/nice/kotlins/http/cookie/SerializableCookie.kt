package com.nice.kotlins.http.cookie

import okhttp3.Cookie
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

class SerializableCookie(@field:Transient private val defaultCookie: Cookie) : Serializable {
    @Transient
    private var cookie: Cookie? = null

    fun getCookie(): Cookie {
        return cookie ?: defaultCookie
    }

    @Throws(IOException::class)
    private fun writeObject(outputStream: ObjectOutputStream) {
        outputStream.writeObject(defaultCookie.name)
        outputStream.writeObject(defaultCookie.value)
        outputStream.writeLong(defaultCookie.expiresAt)
        outputStream.writeObject(defaultCookie.domain)
        outputStream.writeObject(defaultCookie.path)
        outputStream.writeBoolean(defaultCookie.secure)
        outputStream.writeBoolean(defaultCookie.httpOnly)
        outputStream.writeBoolean(defaultCookie.hostOnly)
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    private fun readObject(inputStream: ObjectInputStream) {
        val name = inputStream.readObject() as String
        val value = inputStream.readObject() as String
        val expiresAt = inputStream.readLong()
        val domain = inputStream.readObject() as String
        val path = inputStream.readObject() as String
        val secure = inputStream.readBoolean()
        val httpOnly = inputStream.readBoolean()
        val hostOnly = inputStream.readBoolean()
        val builder = Cookie.Builder()
            .name(name)
            .value(value)
            .expiresAt(expiresAt)
            .path(path)
        if (hostOnly) builder.hostOnlyDomain(domain) else builder.domain(domain)
        if (secure) builder.secure()
        if (httpOnly) builder.httpOnly()
        cookie = builder.build()
    }

    companion object {
        private const val serialVersionUID = 6374381828722046732L
    }
}