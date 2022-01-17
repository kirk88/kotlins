@file:Suppress("UNUSED")

package com.nice.kothttp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.*
import kotlin.coroutines.resume

class OkHttpCall<T> internal constructor(
    private val client: OkHttpClient,
    private val request: Request,
    private val requestInterceptors: List<OkRequestInterceptor>,
    private val responseInterceptors: List<OkResponseInterceptor>,
    private val transformer: OkTransformer<T>
) : OkCall<Flow<T>> {

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

    override fun make(): Flow<T> = flow {
        val result = withContext(Dispatchers.IO) {
            val call = createCall()
            val response = suspendCancellableCoroutine<Response> { con ->
                con.invokeOnCancellation {
                    call.cancel()
                }
                val response = call.execute()
                con.resume(response)
            }
            transformer.transformResponse(interceptResponse(response))
        }
        emit(result)
    }

    private suspend fun createCall(): Call {
        if (creationFailure != null) {
            throw creationFailure!!
        }
        if (call == null) {
            try {
                call = client.newCall(interceptRequest(request))
            } catch (error: Throwable) {
                creationFailure = error
                throw error
            }
        }
        return call!!
    }

    private suspend fun interceptRequest(request: Request): Request {
        var handledRequest = request
        for (interceptor in requestInterceptors) {
            handledRequest = interceptor.intercept(handledRequest)
        }
        return handledRequest
    }

    private suspend fun interceptResponse(response: Response): Response {
        var handledResponse = response
        for (interceptor in responseInterceptors) {
            handledResponse = interceptor.intercept(handledResponse)
        }
        return handledResponse
    }

}

class OkHttpCallBuilder<T> @PublishedApi internal constructor(private val method: OkRequestMethod) {

    private val config: OkHttpConfiguration = OkHttpConfiguration

    private var client: OkHttpClient = DEFAULT_CLIENT

    private val urlBuilder: HttpUrl.Builder = HttpUrl.Builder()
    private val requestBuilder: Request.Builder = Request.Builder()

    private var _formBodyBuilder: FormBody.Builder? = null
    private val formBodyBuilder: FormBody.Builder
        get() = _formBodyBuilder ?: FormBody.Builder().also {
            requestBody = null
            _formBodyBuilder = it
            _multipartBodyBuilder = null
        }

    private var _multipartBodyBuilder: MultipartBody.Builder? = null
    private val multipartBodyBuilder: MultipartBody.Builder
        get() = _multipartBodyBuilder ?: MultipartBody.Builder().also {
            requestBody = null
            _formBodyBuilder = null
            _multipartBodyBuilder = it
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

    fun client(client: () -> OkHttpClient) = apply {
        this.client = client()
    }

    fun url(url: () -> String) = apply {
        val httpUrl = url().toHttpUrl(config)
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

    fun username(username: () -> String) = apply {
        urlBuilder.username(username())
    }

    fun password(password: () -> String) = apply {
        urlBuilder.password(password())
    }

    fun cacheControl(cacheControl: () -> CacheControl) = apply {
        requestBuilder.cacheControl(cacheControl())
    }

    fun tag(tag: () -> Any?) = apply {
        requestBuilder.tag(tag())
    }

    fun <T : Any> tag(type: Class<in T>, tag: () -> T?) = apply {
        requestBuilder.tag(type, tag())
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

    fun requestBody(body: () -> RequestBody) = apply {
        requestBody = body()
    }

    fun interceptRequest(interceptor: OkRequestInterceptor) = apply {
        requestInterceptors.add(interceptor)
    }

    fun interceptResponse(interceptor: OkResponseInterceptor) = apply {
        responseInterceptors.add(interceptor)
    }

    fun mapResponse(mapper: OkResponseMapper<T>) = apply {
        transformer.mapResponse(mapper)
    }

    fun mapError(mapper: OkErrorMapper<T>) = apply {
        transformer.mapError(mapper)
    }

    fun build(): OkHttpCall<T> {
        val body = requestBody
            ?: _formBodyBuilder?.build()
            ?: _multipartBodyBuilder?.build()

        val request = requestBuilder.url(urlBuilder.build())
            .method(method.toString(), body)
            .build()

        return OkHttpCall(
            client,
            request,
            requestInterceptors,
            responseInterceptors,
            transformer
        )
    }

    fun make(): Flow<T> = build().make()

    init {
        config.client?.let { client = it }
        config.cacheControl?.let { requestBuilder.cacheControl(it) }
        config.username?.let { urlBuilder.username(it) }
        config.password?.let { urlBuilder.password(it) }
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

inline fun <reified T> httpCallBuilder(method: OkRequestMethod = OkRequestMethod.Get) =
    OkHttpCallBuilder<T>(method).mapResponse(typeMapper())

inline fun <reified T> buildHttpCall(
    method: OkRequestMethod = OkRequestMethod.Get,
    buildAction: OkHttpCallBuilder<T>.() -> Unit
): OkHttpCall<T> =
    httpCallBuilder<T>(method)
        .apply(buildAction)
        .build()