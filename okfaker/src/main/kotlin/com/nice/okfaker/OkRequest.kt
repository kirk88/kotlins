package com.nice.okfaker

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import kotlin.coroutines.resume

private val DEFAULT_CLIENT = OkHttpClient.Builder()
    .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
    .build()

internal class OkRequest<T>(
    private val client: OkHttpClient,
    private val request: Request,
    private val requestInterceptors: List<OkRequestInterceptor>,
    private val responseInterceptors: List<OkResponseInterceptor>,
    private val transformer: OkTransformer<T>
) {

    private var call: Call? = null
    private var creationFailure: Throwable? = null

    val isExecuted: Boolean
        get() = call?.isExecuted() == true

    val isCanceled: Boolean
        get() = call?.isCanceled() == true

    fun tag(): Any? = request.tag()

    fun <T> tag(type: Class<out T>): T? = request.tag(type)

    fun cancel() = call?.cancel()

    suspend fun execute(): T = withContext(Dispatchers.IO) {
        val call = createCall()
        val response = suspendCancellableCoroutine<Response> { con ->
            con.invokeOnCancellation {
                cancel()
            }
            val response = call.execute()
            con.resume(response)
        }
        transformer.transformResponse(processResponse(response))
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

    class Builder<T> {

        private var client: OkHttpClient = DEFAULT_CLIENT

        private var method: OkRequestMethod = OkRequestMethod.GET

        private val urlBuilder: HttpUrl.Builder = HttpUrl.Builder()

        private val requestBuilder: Request.Builder = Request.Builder()

        private var formBodyApplied: Boolean = false
        private var formBodyBuilder: FormBody.Builder? = null
            get() = field ?: FormBody.Builder().also {
                check(!multipartBodyApplied && !requestBodyApplied) {
                    "Can not build FormBody, a request body already existed"
                }

                formBodyApplied = true
                field = it
            }

        private var multipartBodyApplied: Boolean = false
        private var multipartBodyBuilder: MultipartBody.Builder? = null
            get() = field ?: MultipartBody.Builder().also {
                check(!formBodyApplied && !requestBodyApplied) {
                    "Can not build MultipartBody, a request body already existed"
                }

                multipartBodyApplied = true
                field = it
            }

        private var requestBodyApplied: Boolean = false
        private var requestBody: RequestBody? = null
            set(value) {
                check(!formBodyApplied && !multipartBodyApplied) {
                    "Can not build RequestBody, a request body already existed"
                }

                requestBodyApplied = true
                field = value
            }

        private val requestInterceptors = mutableListOf<OkRequestInterceptor>()
        private val responseInterceptors = mutableListOf<OkResponseInterceptor>()

        private val transformer = OkTransformer<T>()

        fun client(client: OkHttpClient) = apply {
            this.client = client
        }

        fun method(method: OkRequestMethod) = apply {
            this.method = method
        }

        fun url(url: HttpUrl) = apply {
            urlBuilder.scheme(url.scheme)
                .host(url.host)
                .port(url.port)

            val username = url.username
            val password = url.password
            if (username.isNotEmpty() || password.isNotEmpty()) {
                urlBuilder.username(username)
                urlBuilder.password(password)
            }

            val pathSegments = url.pathSegments
            for (pathSegment in pathSegments) {
                urlBuilder.addPathSegment(pathSegment)
            }

            val fragment = url.fragment
            if (!fragment.isNullOrEmpty()) {
                urlBuilder.fragment(fragment)
            }

            val query = url.query
            if (!query.isNullOrEmpty()) {
                urlBuilder.query(query)
            }
        }

        fun cacheControl(cacheControl: CacheControl) = apply {
            requestBuilder.cacheControl(cacheControl)
        }

        fun header(name: String, value: String) = apply {
            requestBuilder.header(name, value)
        }

        fun addHeader(name: String, value: String) = apply {
            requestBuilder.addHeader(name, value)
        }

        fun removeHeaders(name: String) = apply {
            requestBuilder.removeHeader(name)
        }

        fun username(username: String) = apply {
            urlBuilder.username(username)
        }

        fun password(password: String) = apply {
            urlBuilder.password(password)
        }

        fun addQueryParameter(name: String, value: String) = apply {
            urlBuilder.addQueryParameter(name, value)
        }

        fun setQueryParameter(name: String, value: String) = apply {
            urlBuilder.setQueryParameter(name, value)
        }

        fun addEncodedQueryParameter(name: String, value: String) = apply {
            urlBuilder.addEncodedQueryParameter(name, value)
        }

        fun setEncodedQueryParameter(name: String, value: String) = apply {
            urlBuilder.setEncodedQueryParameter(name, value)
        }

        fun removeQueryParameters(name: String) = apply {
            urlBuilder.removeAllQueryParameters(name)
        }

        fun removeEncodedQueryParameters(name: String) = apply {
            urlBuilder.removeAllEncodedQueryParameters(name)
        }

        fun addFormParameter(name: String, value: String) = apply {
            formBodyBuilder!!.add(name, value)
        }

        fun addEncodedFormParameter(name: String, value: String) = apply {
            formBodyBuilder!!.addEncoded(name, value)
        }

        fun addFormDataPart(name: String, value: String) = apply {
            multipartBodyBuilder!!.addFormDataPart(name, value)
        }

        fun addFormDataPart(name: String, filename: String?, body: RequestBody) = apply {
            multipartBodyBuilder!!.addFormDataPart(name, filename, body)
        }

        fun addPart(part: MultipartBody.Part) = apply {
            multipartBodyBuilder!!.addPart(part)
        }

        fun addPart(body: RequestBody) = apply {
            multipartBodyBuilder!!.addPart(body)
        }

        fun requestBody(body: RequestBody) = apply {
            requestBody = body
        }

        fun tag(tag: Any?) = apply {
            requestBuilder.tag(tag)
        }

        fun <T : Any> tag(type: Class<in T>, tag: T?) = apply {
            requestBuilder.tag(type, tag)
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

        fun build(): OkRequest<T> {
            val body = when {
                requestBodyApplied -> requestBody
                formBodyApplied -> formBodyBuilder!!.build()
                multipartBodyApplied -> multipartBodyBuilder!!.build()
                else -> null
            }

            val request = requestBuilder.url(urlBuilder.build())
                .method(method.name, body)
                .build()

            return OkRequest(
                client,
                request,
                requestInterceptors,
                responseInterceptors,
                transformer
            )
        }

    }

}