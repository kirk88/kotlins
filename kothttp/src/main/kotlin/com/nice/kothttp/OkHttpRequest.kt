@file:Suppress("unused")

package com.nice.kothttp

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import okhttp3.*
import java.util.concurrent.Executors
import kotlin.coroutines.resume

class OkHttpRequest<T> internal constructor(
    private val client: OkHttpClient,
    private val request: Request,
    private val requestInterceptors: List<OkRequestInterceptor>,
    private val responseInterceptors: List<OkResponseInterceptor>,
    private val transformer: OkTransformer<T>
) : OkRequest<T> {

    private var call: Call? = null
    private var creationFailure: Throwable? = null

    override val isExecuted: Boolean
        get() = call?.isExecuted() == true

    override val isCanceled: Boolean
        get() = call?.isCanceled() == true

    override fun tag(): Any? = request.tag()

    override fun <T> tag(type: Class<out T>): T? = request.tag(type)

    override fun cancel() {
        call?.cancel()
    }

    override fun execute(): Flow<T> = flow {
        val result = withContext(Dispatchers.IO) {
            val call = createCall()
            val response = suspendCancellableCoroutine<Response> { con ->
                con.invokeOnCancellation {
                    call.cancel()
                }
                val response = call.execute()
                con.resume(response)
            }
            transformer.transformResponse(processResponse(response))
        }
        emit(result)
    }

    private suspend fun createCall(): Call {
        var realCall: Call? = this.call
        if (creationFailure != null) {
            throw creationFailure!!
        }
        if (realCall == null) {
            try {
                this.call = client.newCall(processRequest(request))
                realCall = this.call
            } catch (error: Throwable) {
                creationFailure = error
                throw error
            }
        }
        return realCall!!
    }

    private suspend fun processRequest(request: Request): Request {
        var handledRequest = request
        for (interceptor in requestInterceptors) {
            handledRequest = interceptor.invoke(handledRequest)
        }
        return handledRequest
    }

    private suspend fun processResponse(response: Response): Response {
        var handledResponse = response
        for (interceptor in responseInterceptors) {
            handledResponse = interceptor.invoke(handledResponse)
        }
        return handledResponse
    }

    companion object {

        private var config: OkHttpConfig = OkHttpConfig()
        fun setGlobalConfig(config: OkHttpConfig) {
            this.config = config
        }

        val globalConfig: OkHttpConfig get() = config


        inline fun <reified T> builder() = OkHttpRequestBuilder<T>().mapResponse(typeMapper())

        inline fun <reified T> build(buildAction: OkHttpRequestBuilder<T>.() -> Unit): OkHttpRequest<T> =
            builder<T>()
                .apply(buildAction)
                .build()

        inline fun <reified T> execute(buildAction: OkHttpRequestBuilder<T>.() -> Unit): Flow<T> =
            build(buildAction)
                .execute()

    }

}

class OkHttpRequestBuilder<T> @PublishedApi internal constructor() {

    private val config: OkHttpConfig = OkHttpRequest.globalConfig

    private var client: OkHttpClient = DEFAULT_CLIENT
    private var method: HttpMethod = HttpMethod.GET

    private val urlBuilder: HttpUrl.Builder = HttpUrl.Builder()
    private val requestBuilder: Request.Builder = Request.Builder()

    private var _formBodyBuilder: FormBody.Builder? = null
    private val formBodyBuilder: FormBody.Builder
        get() = _formBodyBuilder ?: FormBody.Builder().also {
            _formBodyBuilder = it
            _multipartBodyBuilder = null
            requestBody = null
        }

    private var _multipartBodyBuilder: MultipartBody.Builder? = null
    private val multipartBodyBuilder: MultipartBody.Builder
        get() = _multipartBodyBuilder ?: MultipartBody.Builder().also {
            _multipartBodyBuilder = it
            _formBodyBuilder = null
            requestBody = null
        }

    private var requestBody: RequestBody? = null
        set(value) {
            field = value
            _formBodyBuilder = null
            _multipartBodyBuilder = null
        }

    private val requestInterceptors = mutableListOf<OkRequestInterceptor>()
    private val responseInterceptors = mutableListOf<OkResponseInterceptor>()

    private val transformer = OkTransformer<T>()

    fun client(client: OkHttpClient) = apply {
        this.client = client
    }

    fun method(method: HttpMethod) = apply {
        this.method = method
    }

    fun url(url: String) = apply {
        val httpUrl = url.toHttpUrl(config)
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

    fun cacheControl(cacheControl: CacheControl) = apply {
        requestBuilder.cacheControl(cacheControl)
    }

    fun tag(tag: Any?) = apply {
        requestBuilder.tag(tag)
    }

    fun <T : Any> tag(type: Class<in T>, tag: T?) = apply {
        requestBuilder.tag(type, tag)
    }

    fun headers(buildAction: HeadersBuilder.() -> Unit) = apply {
        HeadersBuilder(requestBuilder).apply(buildAction)
    }

    fun queryParameters(buildAction: QueryParametersBuilder.() -> Unit) = apply {
        QueryParametersBuilder(urlBuilder).apply(buildAction)
    }

    fun formParameters(buildAction: FormParametersBuilder.() -> Unit) = apply {
        FormParametersBuilder(formBodyBuilder).apply(buildAction)
    }

    fun multipartBody(buildAction: MultipartBodyBuilder.() -> Unit) = apply {
        MultipartBodyBuilder(multipartBodyBuilder).apply(buildAction)
    }

    fun requestBody(body: RequestBody) = apply {
        requestBody = body
    }

    fun addRequestInterceptor(interceptor: OkRequestInterceptor) {
        requestInterceptors.add(interceptor)
    }

    fun addResponseInterceptor(interceptor: OkResponseInterceptor) {
        responseInterceptors.add(interceptor)
    }

    fun mapResponse(mapper: OkResponseMapper<T>) = apply {
        transformer.mapResponse(mapper)
    }

    fun mapError(mapper: OkErrorMapper<T>) = apply {
        transformer.mapError(mapper)
    }

    fun build(): OkHttpRequest<T> {
        val body = requestBody
            ?: _formBodyBuilder?.build()
            ?: _multipartBodyBuilder?.build()

        val request = requestBuilder.url(urlBuilder.build())
            .method(method.name, body)
            .build()

        return OkHttpRequest(
            client,
            request,
            requestInterceptors,
            responseInterceptors,
            transformer
        )
    }

    fun execute(): Flow<T> = build().execute()

    init {
        config.client?.let { client(it) }
        config.cacheControl?.let { cacheControl(it) }
        config.username?.let { username(it) }
        config.password?.let { password(it) }
        config.headers?.forEach {
            requestBuilder.addHeader(it.key, it.value)
        }
        config.queryParameters?.forEach {
            urlBuilder.addQueryParameter(it.key, it.value)
        }
        config.formParameters?.forEach {
            formBodyBuilder.add(it.key, it.value)
        }
    }

}

class HeadersBuilder internal constructor(private val builder: Request.Builder) {

    fun add(name: String, value: Any?) {
        builder.addHeader(name, value.toStringOrEmpty())
    }

    fun set(name: String, value: Any?) {
        builder.header(name, value.toStringOrEmpty())
    }

    fun remove(name: String) {
        builder.removeHeader(name)
    }

    operator fun String.plusAssign(value: Any?) = add(this, value)

    operator fun String.unaryMinus() = remove(this)

}

class QueryParametersBuilder internal constructor(private val builder: HttpUrl.Builder) {

    fun add(name: String, value: Any?) {
        builder.addQueryParameter(name, value.toStringOrEmpty())
    }

    fun addEncoded(name: String, value: Any?) {
        builder.addEncodedQueryParameter(name, value.toStringOrEmpty())
    }

    fun set(name: String, value: Any?) {
        builder.setQueryParameter(name, value.toStringOrEmpty())
    }

    fun setEncoded(name: String, value: Any?) {
        builder.setEncodedQueryParameter(name, value.toStringOrEmpty())
    }

    fun remove(name: String) {
        builder.removeAllQueryParameters(name)
    }

    fun removeEncoded(name: String) {
        builder.removeAllEncodedQueryParameters(name)
    }

    operator fun String.plusAssign(value: Any?) = add(this, value)

    operator fun String.unaryMinus() = remove(this)

}

class FormParametersBuilder internal constructor(private val builder: FormBody.Builder) {

    fun add(name: String, value: Any?) {
        builder.add(name, value.toStringOrEmpty())
    }

    fun addEncoded(name: String, value: Any?) {
        builder.addEncoded(name, value.toStringOrEmpty())
    }

    operator fun String.plusAssign(value: Any?) = add(this, value)

}

class MultipartBodyBuilder internal constructor(private val builder: MultipartBody.Builder) {

    fun add(name: String, value: Any?) {
        builder.addFormDataPart(name, value.toStringOrEmpty())
    }

    fun add(name: String, filename: String?, body: RequestBody) {
        builder.addFormDataPart(name, filename, body)
    }

    fun add(part: MultipartBody.Part) {
        builder.addPart(part)
    }

    fun add(body: RequestBody) {
        builder.addPart(body)
    }

    operator fun String.plusAssign(value: Any?) = add(this, value)

}

private fun Any?.toStringOrEmpty() = (this ?: "").toString()

fun main() {
    OkHttpRequest.setGlobalConfig(
        OkHttpConfig.Builder()
            .domain("http://www.baidu.com/s")
            .build()
    )

    val scope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())

    OkHttpRequest.execute<String> {

    }.onStart {

    }

}