@file:Suppress("UNUSED")

package com.nice.kothttp

import okhttp3.Response
import okio.ByteString

sealed class OkWebSocketResponse {

    class Open(val call: OkWebSocketCall, val response: Response): OkWebSocketResponse()

    class StringMessage(val call: OkWebSocketCall, val text: String): OkWebSocketResponse()

    class ByteStringMessage(val call: OkWebSocketCall, val bytes: ByteString): OkWebSocketResponse()

    class Closing(val call: OkWebSocketCall, val code: Int, val reason: String): OkWebSocketResponse()

    class Closed(val call: OkWebSocketCall, val code: Int, val reason: String): OkWebSocketResponse()

    class Failure(val call: OkWebSocketCall, val error: Throwable, val response: Response?): OkWebSocketResponse()

}