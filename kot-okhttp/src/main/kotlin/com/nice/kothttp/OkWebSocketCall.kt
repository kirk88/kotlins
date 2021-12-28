@file:Suppress("UNUSED")

package com.nice.kothttp

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.ByteString
import java.util.concurrent.atomic.AtomicBoolean

class OkWebSocketCall(
    private val client: OkHttpClient,
    private val request: Request
) : OkCall<OkWebSocket> {

    private var webSocket: OkWebSocket? = null
    private var creationFailure: Throwable? = null

    private val executed = AtomicBoolean()

    @Volatile
    private var canceled: Boolean = false

    override val isExecuted: Boolean
        get() = executed.get()

    override val isCanceled: Boolean
        get() = canceled


    override fun tag(): Any? = request.tag()

    override fun <T> tag(type: Class<out T>): T? = request.tag(type)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun make(): OkWebSocket {
        if (creationFailure != null) {
            throw creationFailure!!
        }

        if (webSocket == null) {
            try {
                webSocket = RealOkWebSocket(client, request) { cancel() }
            } catch (error: Throwable) {
                creationFailure = error
                throw error
            }
        }

        check(executed.compareAndSet(false, true)) { "Already Executed" }

        return webSocket!!
    }

    override fun cancel() {
        if (canceled) return

        canceled = true
        webSocket?.cancel()
    }

    private class RealOkWebSocket(
        client: OkHttpClient,
        request: Request,
        private val onCancel: () -> Unit
    ) : OkWebSocket {

        @OptIn(ExperimentalCoroutinesApi::class)
        private val _response = Channel<OkWebSocketResponse>(capacity = Int.MAX_VALUE).apply {
            invokeOnClose {
                onCancel.invoke()
            }
        }
        override val response: Flow<OkWebSocketResponse> = _response.consumeAsFlow()

        private val socket = client.newWebSocket(request, OkWebSocketListener(this, _response))

        override val queueSize: Long
            get() = socket.queueSize()

        override fun send(text: String): Boolean {
            return socket.send(text)
        }

        override fun send(bytes: ByteString): Boolean {
            return socket.send(bytes)
        }

        override fun close(code: Int, reason: String?): Boolean {
            return socket.close(code, reason)
        }

        override fun cancel() {
            onCancel.invoke()
        }

    }

    private class OkWebSocketListener(
        private val socket: OkWebSocket,
        private val channel: SendChannel<OkWebSocketResponse>
    ) : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            channel.trySendBlocking(OkWebSocketResponse.Open(socket, response))
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            channel.trySendBlocking(OkWebSocketResponse.StringMessage(socket, text))
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            channel.trySendBlocking(OkWebSocketResponse.ByteStringMessage(socket, bytes))
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            channel.trySendBlocking(OkWebSocketResponse.Closing(socket, code, reason))
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            channel.trySendBlocking(OkWebSocketResponse.Closed(socket, code, reason))
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            channel.trySendBlocking(OkWebSocketResponse.Failure(socket, t, response))
        }

    }

}

class OkWebSocketCallBuilder internal constructor() {

    private var client: OkHttpClient = DEFAULT_CLIENT

    private val urlBuilder: HttpUrl.Builder = HttpUrl.Builder()

    private val requestBuilder: Request.Builder = Request.Builder()

    fun client(client: OkHttpClient) = apply {
        this.client = client
    }

    fun url(url: String) = apply {
        val httpUrl = url.toHttpUrl()
        urlBuilder.scheme(httpUrl.scheme)
            .host(httpUrl.host)
            .port(httpUrl.port)

        val username = httpUrl.username
        val password = httpUrl.password
        if (username.isNotEmpty() || password.isNotEmpty()) {
            urlBuilder.username(username)
            urlBuilder.password(password)
        }

        val pathSegments = httpUrl.pathSegments
        for (pathSegment in pathSegments) {
            urlBuilder.addPathSegment(pathSegment)
        }

        val fragment = httpUrl.fragment
        if (!fragment.isNullOrEmpty()) {
            urlBuilder.fragment(fragment)
        }

        val query = httpUrl.query
        if (!query.isNullOrEmpty()) {
            urlBuilder.query(query)
        }
    }

    fun username(username: String) = apply {
        urlBuilder.username(username)
    }

    fun password(password: String) = apply {
        urlBuilder.password(password)
    }

    fun headers(buildAction: HeadersBuilder.() -> Unit) = apply {
        HeadersBuilder(requestBuilder).apply(buildAction)
    }

    fun queryParameters(buildAction: QueryParametersBuilder.() -> Unit) = apply {
        QueryParametersBuilder(urlBuilder).apply(buildAction)
    }

    fun tag(tag: Any?) = apply {
        requestBuilder.tag(tag)
    }

    fun <T : Any> tag(type: Class<in T>, tag: T?) = apply {
        requestBuilder.tag(type, tag)
    }

    fun build(): OkWebSocketCall {
        val request = requestBuilder.url(urlBuilder.build()).get().build()
        return OkWebSocketCall(client, request)
    }

    fun make(): OkWebSocket = build().make()

}

fun webSocketCallBuilder() = OkWebSocketCallBuilder()

fun buildWebSocketCall(buildAction: OkWebSocketCallBuilder.() -> Unit): OkWebSocketCall =
    webSocketCallBuilder()
        .apply(buildAction)
        .build()