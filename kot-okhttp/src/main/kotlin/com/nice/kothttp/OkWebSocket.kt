package com.nice.kothttp

import kotlinx.coroutines.flow.Flow
import okio.ByteString

interface OkWebSocket {

    val response: Flow<OkWebSocketResponse>

    val queueSize: Long

    fun send(text: String): Boolean

    fun send(bytes: ByteString): Boolean

    fun close(code: Int, reason: String? = null): Boolean

    fun cancel()

}