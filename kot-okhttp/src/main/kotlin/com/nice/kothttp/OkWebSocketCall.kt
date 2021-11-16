@file:Suppress("UNUSED")

package com.nice.kothttp

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.*
import okio.ByteString
import java.util.concurrent.atomic.AtomicBoolean

class OkWebSocketCall(
    private val client: OkHttpClient,
    private val request: Request
) : OkCall<OkWebSocketResponse>, OkWebSocket {

    private var _webSocket: WebSocket? = null
    private val webSocket: WebSocket
        get() = _webSocket ?: throw IllegalStateException("Not Ready")

    private var creationFailure: Throwable? = null

    private val executed = AtomicBoolean()

    @Volatile
    private var canceled: Boolean = false

    override val isExecuted: Boolean
        get() = executed.get()

    override val isCanceled: Boolean
        get() = canceled

    override val queueSize: Long
        get() = webSocket.queueSize()

    override fun tag(): Any? = request.tag()

    override fun <T> tag(type: Class<out T>): T? = request.tag(type)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun make(): Flow<OkWebSocketResponse> {
        check(executed.compareAndSet(false, true)) { "Already Executed" }

        return callbackFlow {
            val listener = OkWebSocketListener(this@OkWebSocketCall, this)

            _webSocket = createWebSocket(listener)

            invokeOnClose {
                cancel()
            }
        }
    }

    override fun send(text: String): Boolean {
        return webSocket.send(text)
    }

    override fun send(bytes: ByteString): Boolean {
        return webSocket.send(bytes)
    }

    override fun close(code: Int, reason: String?): Boolean {
        return _webSocket?.close(code, reason) ?: false
    }

    override fun cancel() {
        if (canceled) return

        canceled = true
        _webSocket?.cancel()
    }

    private fun createWebSocket(listener: WebSocketListener): WebSocket {
        if (creationFailure != null) {
            throw creationFailure!!
        }
        try {
            return client.newWebSocket(request, listener)
        } catch (error: Throwable) {
            creationFailure = error
            throw error
        }
    }

    private class OkWebSocketListener(
        private val socket: OkWebSocket,
        private val channel: SendChannel<OkWebSocketResponse>
    ) : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            channel.trySend(OkWebSocketResponse.Open(socket, response))
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            channel.trySend(OkWebSocketResponse.StringMessage(socket, text))
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            channel.trySend(OkWebSocketResponse.ByteStringMessage(socket, bytes))
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            channel.trySend(OkWebSocketResponse.Closing(socket, code, reason))
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            channel.trySend(OkWebSocketResponse.Closed(socket, code, reason))
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            channel.trySend(OkWebSocketResponse.Failure(socket, t, response))
        }
    }

}

class OkWebSocketCallBuilder internal constructor() {

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

    fun build(): OkWebSocketCall {
        val request = requestBuilder.get().build()
        return OkWebSocketCall(client, request)
    }

    fun make(): Flow<OkWebSocketResponse> = build().make()

}

fun webSocketCallBuilder() = OkWebSocketCallBuilder()

fun buildWebSocketCall(buildAction: OkWebSocketCallBuilder.() -> Unit): OkWebSocketCall =
    webSocketCallBuilder()
        .apply(buildAction)
        .build()