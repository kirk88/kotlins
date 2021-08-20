@file:Suppress("unused")

package com.nice.kothttp

import okhttp3.Response
import okio.ByteString

sealed class OkWebSocketResponse {

    class Open(val request: OkWebSocketRequest, val response: Response): OkWebSocketResponse()

    class StringMessage(val request: OkWebSocketRequest, val text: String): OkWebSocketResponse()

    class ByteStringMessage(val request: OkWebSocketRequest, val bytes: ByteString): OkWebSocketResponse()

    class Closing(val request: OkWebSocketRequest, val code: Int, val reason: String): OkWebSocketResponse()

    class Closed(val request: OkWebSocketRequest, val code: Int, val reason: String): OkWebSocketResponse()

    class Failure(val request: OkWebSocketRequest, val error: Throwable, val response: Response?): OkWebSocketResponse()

}