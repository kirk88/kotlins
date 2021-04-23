package com.nice.kotlins.http.cookie

import okhttp3.Cookie
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

class SerializableCookie(@field:Transient private val cookie: Cookie) : Serializable {
    @Transient
    private var clientCookie: Cookie? = null

    fun getCookie(): Cookie {
        return clientCookie ?: cookie
    }

    @Throws(IOException::class)
    private fun writeObject(outputStream: ObjectOutputStream) {
        outputStream.writeObject(cookie.name)
        outputStream.writeObject(cookie.value)
        outputStream.writeLong(cookie.expiresAt)
        outputStream.writeObject(cookie.domain)
        outputStream.writeObject(cookie.path)
        outputStream.writeBoolean(cookie.secure)
        outputStream.writeBoolean(cookie.httpOnly)
        outputStream.writeBoolean(cookie.hostOnly)
        outputStream.writeBoolean(cookie.persistent)
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
        val persistent = inputStream.readBoolean()
        val builder = Cookie.Builder()
            .name(name)
            .value(value)
            .expiresAt(expiresAt)
            .path(path)
        if (hostOnly) builder.hostOnlyDomain(domain) else builder.domain(domain)
        if (secure) builder.secure()
        if (httpOnly) builder.httpOnly()
        clientCookie = builder.build()
    }

    companion object {
        private const val serialVersionUID = 6374381828722046732L
    }
}