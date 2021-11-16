package com.nice.kothttp

import okio.ByteString

interface OkWebSocket {

    val queueSize: Long

    fun send(text: String): Boolean

    fun send(bytes: ByteString): Boolean

    fun close(code: Int, reason: String? = null): Boolean

    fun cancel()

}