@file:Suppress("unused")

package com.nice.kothttp

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.*
import okio.ByteString
import java.util.concurrent.atomic.AtomicBoolean

class OkWebSocketRequest(
    private val client: OkHttpClient,
    private val request: Request
) : OkRequest<OkWebSocketResponse> {

    private var webSocket: WebSocket? = null
    private var connectionFailure: Throwable? = null

    private val executed = AtomicBoolean()

    @Volatile
    var canceled: Boolean = false

    override val isExecuted: Boolean
        get() = executed.get()

    override val isCanceled: Boolean
        get() = canceled

    override fun tag(): Any? = request.tag()

    override fun <T> tag(type: Class<out T>): T? = request.tag(type)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun execute(): Flow<OkWebSocketResponse> {
        check(executed.compareAndSet(false, true)) { "Already Executed" }

        return callbackFlow {

            val listener = OkWebSocketListener(this@OkWebSocketRequest, this)

            connectWebSocket(listener)

            invokeOnClose {
                cancel()
            }

        }
    }

    override fun cancel() {
        if (canceled) return

        canceled = true
        webSocket?.cancel()
    }

    fun close(code: Int = 0, reason: String? = null) {
        webSocket?.close(code, reason)
    }

    fun send(text: String): Boolean {
        return webSocket?.send(text) ?: false
    }

    fun send(bytes: ByteString): Boolean {
        return webSocket?.send(bytes) ?: false
    }

    private fun connectWebSocket(listener: WebSocketListener) {
        if (connectionFailure != null) {
            throw connectionFailure!!
        }
        try {
            this.webSocket = client.newWebSocket(request, listener)
        } catch (error: Throwable) {
            connectionFailure = error
            throw error
        }
    }

    private class OkWebSocketListener(
        private val request: OkWebSocketRequest,
        private val channel: SendChannel<OkWebSocketResponse>
    ) : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            channel.trySend(OkWebSocketResponse.Open(request, response))
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            channel.trySend(OkWebSocketResponse.StringMessage(request, text))
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            channel.trySend(OkWebSocketResponse.ByteStringMessage(request, bytes))
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            channel.trySend(OkWebSocketResponse.Closing(request, code, reason))
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            channel.trySend(OkWebSocketResponse.Closed(request, code, reason))
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            channel.trySend(OkWebSocketResponse.Failure(request, t, response))
        }
    }

    companion object {

        fun builder() = OkWebSocketRequestBuilder()

        fun build(buildAction: OkWebSocketRequestBuilder.() -> Unit): OkWebSocketRequest =
            builder()
                .apply(buildAction)
                .build()

        fun execute(buildAction: OkWebSocketRequestBuilder.() -> Unit): Flow<OkWebSocketResponse> =
            build(buildAction)
                .execute()

    }
}

class OkWebSocketRequestBuilder internal constructor() {

    private var client: OkHttpClient = DEFAULT_CLIENT

    private val requestBuilder: Request.Builder = Request.Builder()

    fun client(client: OkHttpClient) = apply {
        this.client = client
    }

    fun url(url: String) = apply {
        requestBuilder.url(url)
    }

    fun tag(tag: Any?) = apply {
        requestBuilder.tag(tag)
    }

    fun <T : Any> tag(type: Class<in T>, tag: T?) = apply {
        requestBuilder.tag(type, tag)
    }

    fun build(): OkWebSocketRequest {
        val request = requestBuilder.get().build()
        return OkWebSocketRequest(client, request)
    }

    fun execute(): Flow<OkWebSocketResponse> = build().execute()

}