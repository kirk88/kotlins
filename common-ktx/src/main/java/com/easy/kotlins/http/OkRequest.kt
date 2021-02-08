package com.easy.kotlins.http

import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.URL

internal class OkRequest(
    private val client: OkHttpClient,
    private val method: OkRequestMethod,
    private val url: HttpUrl,
    private val requestBody: RequestBody?,
    private val requestBuilder: Request.Builder,
    private val requestInterceptors: List<OkRequestInterceptor>,
    private val responseInterceptors: List<OkResponseInterceptor>
) {

    private var call: Call? = null
    private var creationFailure: Exception? = null

    @Volatile
    private var canceled = false
    private var executed = false

    val tag: Any?
        get() = call?.request()?.tag()

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
                if (!dispatchOnCancel(callback)) {
                    dispatchOnFailure(callback, e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!dispatchOnCancel(callback)) {
                    dispatchOnResponse(callback, response)
                }
            }
        })
    }

    private fun dispatchOnStart(callback: OkCallback<Response>) {
        callback.onStart()
    }

    private fun dispatchOnFailure(callback: OkCallback<Response>, e: Exception) {
        try {
            callback.onError(e)
        } finally {
            callback.onComplete()
        }
    }

    private fun dispatchOnResponse(callback: OkCallback<Response>, response: Response) {
        try {
            callback.onSuccess(processResponse(response))
        } catch (e: Exception) {
            callback.onError(e)
        } finally {
            callback.onComplete()
        }
    }

    private fun dispatchOnCancel(callback: OkCallback<Response>): Boolean {
        if (canceled) {
            callback.onCancel()
            return true
        }
        return false
    }

    @Throws(Exception::class)
    private fun createCall(): Call {
        var realCall: Call?
        synchronized(this) {
            check(!executed) { "Already executed" }
            executed = true
            realCall = this.call
            if (creationFailure != null) {
                throw creationFailure!!
            }
            if (realCall == null) {
                try {
                    this.call = client.newCall(createRequest())
                    realCall = this.call
                } catch (e: Exception) {
                    creationFailure = e
                    throw e
                }
            }
        }
        return realCall!!
    }

    private fun createRequest(): Request {
        val request = requestBuilder.url(url).also {
            it.method(method.name, requestBody)
        }.build()
        return processRequest(request)
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

    class Builder(private val method: OkRequestMethod = OkRequestMethod.GET) {
        private val requestBuilder: Request.Builder = Request.Builder()

        private val urlBuilder: HttpUrl.Builder = HttpUrl.Builder()

        private var formBuilderApplied = false
        private var formBuilder: FormBody.Builder? = null
            get() = field ?: FormBody.Builder().also { field = it }
            set(value) {
                field = value
                formBuilderApplied = true
                multipartBuilderApplied = false
            }

        private var multipartBuilderApplied = false
        private var multipartBuilder: MultipartBody.Builder? = null
            get() = field ?: MultipartBody.Builder().also { field = it }
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

        private var client: OkHttpClient? = null

        private val requestInterceptors = mutableListOf<OkRequestInterceptor>()
        private val responseInterceptors = mutableListOf<OkResponseInterceptor>()

        fun client(client: OkHttpClient): Builder {
            this.client = client
            return this
        }

        fun url(url: HttpUrl): Builder {
            urlBuilder.scheme(url.scheme)
                .encodedUsername(url.encodedUsername)
                .encodedPassword(url.encodedPassword)
                .host(url.host)
                .port(url.port)
                .encodedPath(url.encodedPath)
                .encodedQuery(url.encodedQuery)
                .encodedFragment(url.encodedFragment)
            return this
        }

        fun url(url: String): Builder {
            return url(url.toHttpUrl())
        }

        fun url(url: URL): Builder {
            return url(url.toString().toHttpUrl())
        }

        fun url(uri: URI): Builder {
            return url(uri.toString().toHttpUrl())
        }

        fun tag(tag: Any): Builder {
            requestBuilder.tag(tag)
            return this
        }

        fun cacheControl(cacheControl: CacheControl): Builder {
            requestBuilder.cacheControl(cacheControl)
            return this
        }

        fun header(key: String, value: String): Builder {
            requestBuilder.header(key, value)
            return this
        }

        fun addHeader(key: String, value: String): Builder {
            requestBuilder.addHeader(key, value)
            return this
        }

        fun removeHeader(key: String): Builder {
            requestBuilder.removeHeader(key)
            return this
        }

        fun username(username: String): Builder {
            urlBuilder.username(username)
            return this
        }

        fun password(password: String): Builder {
            urlBuilder.password(password)
            return this
        }

        fun addQueryParameter(key: String, value: String): Builder {
            urlBuilder.addQueryParameter(key, value)
            return this
        }

        fun setQueryParameter(key: String, value: String): Builder {
            urlBuilder.setQueryParameter(key, value)
            return this
        }

        fun addEncodedQueryParameter(key: String, value: String): Builder {
            urlBuilder.addEncodedQueryParameter(key, value)
            return this
        }

        fun setEncodedQueryParameter(key: String, value: String): Builder {
            urlBuilder.setEncodedQueryParameter(key, value)
            return this
        }

        fun removeQueryParameters(key: String): Builder {
            urlBuilder.removeAllQueryParameters(key)
            return this
        }

        fun removeEncodedQueryParameters(key: String): Builder {
            urlBuilder.removeAllEncodedQueryParameters(key)
            return this
        }

        fun addFormParameter(key: String, value: String): Builder {
            formBuilder?.add(key, value)
            return this
        }

        fun addEncodedFormParameter(key: String, value: String): Builder {
            formBuilder?.addEncoded(key, value)
            return this
        }

        fun addFormDataPart(name: String, value: String): Builder {
            multipartBuilder?.addFormDataPart(name, value)
            return this
        }

        fun addFormDataPart(name: String, filename: String?, body: RequestBody): Builder {
            multipartBuilder?.addFormDataPart(name, filename, body)
            return this
        }

        fun addFormDataPart(name: String, contentType: MediaType?, file: File): Builder {
            multipartBuilder?.addFormDataPart(
                name,
                file.name,
                file.asRequestBody(contentType)
            )
            return this
        }

        fun addPart(part: MultipartBody.Part): Builder {
            multipartBuilder?.addPart(part)
            return this
        }

        fun addPart(body: RequestBody): Builder {
            multipartBuilder?.addPart(body)
            return this
        }

        fun addPart(headers: Headers?, body: RequestBody): Builder {
            multipartBuilder?.addPart(headers, body)
            return this
        }

        fun body(contentType: MediaType?, body: String): Builder {
            requestBody = body.toRequestBody(contentType)
            return this
        }

        fun body(contentType: MediaType?, file: File): Builder {
            requestBody = file.asRequestBody(contentType)
            return this
        }

        fun body(body: RequestBody): Builder {
            requestBody = body
            return this
        }

        fun addRequestInterceptor(interceptor: OkRequestInterceptor) {
            requestInterceptors.add(interceptor)
        }

        fun addResponseInterceptor(interceptor: OkResponseInterceptor) {
            responseInterceptors.add(interceptor)
        }

        private fun createRequestBody(): RequestBody? {
            val formBody = if (formBuilderApplied) formBuilder?.build() else null
            val multipartBody = if (multipartBuilderApplied) multipartBuilder?.build() else null
            return requestBody ?: formBody ?: multipartBody
        }

        fun build(): OkRequest {
            return OkRequest(
                requireNotNull(client) { "OkHttpClient must not be null" },
                method,
                urlBuilder.build(),
                createRequestBody(),
                requestBuilder,
                requestInterceptors,
                responseInterceptors
            )
        }
    }

}