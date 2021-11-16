@file:Suppress("UNUSED")

package com.nice.kothttp

import okhttp3.Response
import okio.ByteString

sealed class OkWebSocketResponse {

    class Open(val socket: OkWebSocket, val response: Response): OkWebSocketResponse()

    class StringMessage(val socket: OkWebSocket, val text: String): OkWebSocketResponse()

    class ByteStringMessage(val socket: OkWebSocket, val bytes: ByteString): OkWebSocketResponse()

    class Closing(val socket: OkWebSocket, val code: Int, val reason: String): OkWebSocketResponse()

    class Closed(val socket: OkWebSocket, val code: Int, val reason: String): OkWebSocketResponse()

    class Failure(val socket: OkWebSocket, val error: Throwable, val response: Response?): OkWebSocketResponse()

}