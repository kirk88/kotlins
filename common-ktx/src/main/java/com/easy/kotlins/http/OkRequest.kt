package com.easy.kotlins.http

import com.easy.kotlins.helper.isNetworkUrl
import com.easy.kotlins.helper.plus
import com.easy.kotlins.helper.toUrl
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.http.HttpMethod
import java.io.File
import java.io.IOException

internal class OkRequest(
    private val client: OkHttpClient,
    private val request: Request,
    private val requestInterceptors: List<OkRequestInterceptor>,
    private val responseInterceptors: List<OkResponseInterceptor>,
) {

    private var call: Call? = null
    private var creationFailure: Exception? = null

    @Volatile
    private var canceled = false
    private var executed = false

    val isExecuted: Boolean
        get() {
            if (executed) return true
            synchronized(this) { return call?.isExecuted() == true }
        }

    val isCanceled: Boolean
        get() {
            if (canceled) return true
            synchronized(this) { return call?.isCanceled() == true }
        }

    fun tag(): Any? = request.tag()

    fun <T> tag(type: Class<out T>): T? = request.tag(type)

    fun cancel() {
        if (canceled) return
        canceled = true
        synchronized(this) { call?.cancel() }
    }

    @Throws(IOException::class)
    fun execute(): Response {
        return processResponse(createCall().execute())
    }

    fun enqueue(callback: OkCallback<Response>) {
        var call: Call? = null
        var failure: Exception? = null
        try {
            call = createCall()
        } catch (e: Exception) {
            failure = e
        }

        if (failure != null) {
            dispatchOnFailure(callback, failure)
            return
        }

        dispatchOnStart(callback)

        call!!.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                dispatchOnFailure(callback, e)
            }

            override fun onResponse(call: Call, response: Response) {
                dispatchOnResponse(callback, response)
            }
        })
    }

    private fun dispatchOnStart(callback: OkCallback<Response>) {
        callback.onStart()
    }

    private fun dispatchOnFailure(callback: OkCallback<Response>, e: Exception) {
        if (dispatchOnCancel(callback)) {
            return
        }

        try {
            callback.onFailure(e)
        } finally {
            callback.onCompletion()
        }
    }

    private fun dispatchOnResponse(callback: OkCallback<Response>, response: Response) {
        if (dispatchOnCancel(callback)) {
            return
        }

        try {
            callback.onSuccess(processResponse(response))
        } catch (e: Exception) {
            callback.onFailure(e)
        } finally {
            callback.onCompletion()
        }
    }

    private fun dispatchOnCancel(callback: OkCallback<Response>): Boolean {
        if (canceled) {
            callback.onCancel()
            return true
        }
        return false
    }

    private fun createCall(): Call {
        var realCall: Call?
        synchronized(this) {
            check(!canceled) { "Already canceled" }
            check(!executed) { "Already executed" }
            executed = true
            realCall = this.call
            if (creationFailure != null) {
                throw creationFailure!!
            }
            if (realCall == null) {
                try {
                    this.call = client.newCall(processRequest(request))
                    realCall = this.call
                } catch (e: Exception) {
                    creationFailure = e
                    throw e
                }
            }
        }
        return realCall!!
    }

    private fun processRequest(request: Request): Request {
        var handledRequest = request
        for (interceptor in requestInterceptors) {
            handledRequest = interceptor.shouldInterceptRequest(handledRequest)
        }
        return handledRequest
    }

    private fun processResponse(response: Response): Response {
        var handledResponse = response
        for (interceptor in responseInterceptors) {
            handledResponse = interceptor.shouldInterceptResponse(handledResponse)
        }
        return handledResponse
    }

    class Builder(private val method: OkRequestMethod, private val config: OkConfig) {

        private var client: OkHttpClient? = config.client

        private val urlBuilder: HttpUrl.Builder = HttpUrl.Builder().also {
            it.username(config.username.orEmpty())
            it.password(config.password.orEmpty())
            for ((name, value) in config.queryParameters) {
                it.addQueryParameter(name, value)
            }
        }

        private val requestBuilder: Request.Builder = Request.Builder().also {
            val cacheControl = config.cacheControl
            if (cacheControl != null) {
                it.cacheControl(cacheControl)
            }

            for ((name, value) in config.headers) {
                it.addHeader(name, value)
            }
        }

        private var formBuilderApplied = false
        private var formBuilder: FormBody.Builder? = null
            get() = field ?: FormBody.Builder().also {
                field = it

                for ((name, value) in config.formParameters) {
                    it.add(name, value)
                }
            }
            set(value) {
                field = value
                formBuilderApplied = true
                multipartBuilderApplied = false
            }

        private var multipartBuilderApplied = false
        private var multipartBuilder: MultipartBody.Builder? = null
            get() = field ?: MultipartBody.Builder().also {
                field = it
            }
            set(value) {
                field = value
                formBuilderApplied = false
                multipartBuilderApplied = true
            }

        private var requestBody: RequestBody? = null
            set(value) {
                field = value
                formBuilderApplied = false
                multipartBuilderApplied = false
            }

        private val requestInterceptors = mutableListOf<OkRequestInterceptor>()
        private val responseInterceptors = mutableListOf<OkResponseInterceptor>()

        fun client(client: OkHttpClient) = apply {
            this.client = client
        }

        fun url(url: String) = apply {
            val baseUrl = config.baseUrl
            val httpUrl: HttpUrl = when {
                url.isNetworkUrl() -> url.toHttpUrl()
                baseUrl != null -> (baseUrl.toUrl() + url).toString().toHttpUrl()
                else -> throw IllegalArgumentException("Invalid url: $url")
            }

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

        fun cacheControl(cacheControl: CacheControl) = apply {
            requestBuilder.cacheControl(cacheControl)
        }

        fun header(name: String, value: String) = apply {
            requestBuilder.header(name, value)
        }

        fun addHeader(name: String, value: String) = apply {
            requestBuilder.addHeader(name, value)
        }

        fun removeHeader(name: String) = apply {
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
            formBuilder?.add(name, value)
        }

        fun addEncodedFormParameter(name: String, value: String) = apply {
            formBuilder?.addEncoded(name, value)
        }

        fun addFormDataPart(name: String, value: String) = apply {
            multipartBuilder?.addFormDataPart(name, value)
        }

        fun addFormDataPart(name: String, filename: String?, body: RequestBody) = apply {
            multipartBuilder?.addFormDataPart(name, filename, body)
        }

        fun addFormDataPart(name: String, contentType: MediaType?, file: File) = apply {
            multipartBuilder?.addFormDataPart(
                name,
                file.name,
                file.asRequestBody(contentType)
            )
        }

        fun addPart(part: MultipartBody.Part) = apply {
            multipartBuilder?.addPart(part)
        }

        fun addPart(body: RequestBody) = apply {
            multipartBuilder?.addPart(body)
        }

        fun body(contentType: MediaType?, body: String) = apply {
            requestBody = body.toRequestBody(contentType)
        }

        fun body(contentType: MediaType?, file: File) = apply {
            requestBody = file.asRequestBody(contentType)
        }

        fun body(body: RequestBody) = apply {
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

        fun build(): OkRequest {
            val body = when {
                requestBody != null -> requestBody
                formBuilderApplied -> formBuilder?.build()
                multipartBuilderApplied -> multipartBuilder?.build()
                HttpMethod.requiresRequestBody(method.name) -> formBuilder?.build()
                else -> null
            }

            val request = requestBuilder.url(urlBuilder.build())
                .method(method.name, body)
                .build()

            return OkRequest(
                requireNotNull(client) { "OkHttpClient must not be null" },
                request,
                requestInterceptors,
                responseInterceptors,
            )
        }

    }

}