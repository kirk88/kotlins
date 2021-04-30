package com.nice.kotlins.http

import com.nice.kotlins.helper.isNetworkUrl
import com.nice.kotlins.helper.plus
import com.nice.kotlins.helper.toUrl
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
    private val request: Request,
    private val requestInterceptors: List<OkRequestInterceptor>,
    private val responseInterceptors: List<OkResponseInterceptor>,
) {

    private var call: Call? = null
    private var creationFailure: Throwable? = null

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

    private fun dispatchOnFailure(callback: OkCallback<Response>, error: Throwable) {
        if (dispatchOnCancel(callback)) {
            return
        }

        try {
            callback.onError(error)
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
        } catch (error: Throwable) {
            callback.onError(error)
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
                } catch (error: Throwable) {
                    creationFailure = error
                    throw error
                }
            }
        }
        return realCall!!
    }

    private fun processRequest(request: Request): Request {
        var handledRequest = request
        for (interceptor in requestInterceptors) {
            handledRequest = interceptor.intercept(handledRequest)
        }
        return handledRequest
    }

    private fun processResponse(response: Response): Response {
        var handledResponse = response
        for (interceptor in responseInterceptors) {
            handledResponse = interceptor.intercept(handledResponse)
        }
        return handledResponse
    }

    class Builder(private val method: OkRequestMethod, private val config: OkConfig) {

        private var client: OkHttpClient? = config.client

        private val urlBuilder: HttpUrl.Builder = HttpUrl.Builder().also {
            it.username(config.username.orEmpty())
            it.password(config.password.orEmpty())
        }

        private val requestBuilder: Request.Builder = Request.Builder().also {
            val cacheControl = config.cacheControl
            if (cacheControl != null) {
                it.cacheControl(cacheControl)
            }
        }

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

        fun client(client: OkHttpClient) = apply {
            this.client = client
        }

        fun url(url: String) = apply {
            val httpUrl: HttpUrl = config.baseUrl.let {
                when {
                    url.isNetworkUrl() -> url.toHttpUrl()
                    it != null -> (it.toUrl() + url).toString().toHttpUrl()
                    else -> throw IllegalArgumentException("Invalid url: $url")
                }
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

        fun addFormDataPart(name: String, contentType: MediaType?, file: File) = apply {
            multipartBodyBuilder!!.addFormDataPart(
                name,
                file.name,
                file.asRequestBody(contentType)
            )
        }

        fun addPart(part: MultipartBody.Part) = apply {
            multipartBodyBuilder!!.addPart(part)
        }

        fun addPart(body: RequestBody) = apply {
            multipartBodyBuilder!!.addPart(body)
        }

        fun stringBody(contentType: MediaType?, body: String) = apply {
            requestBody = body.toRequestBody(contentType)
        }

        fun fileBody(contentType: MediaType?, file: File) = apply {
            requestBody = file.asRequestBody(contentType)
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

        fun build(): OkRequest {
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
                requireNotNull(client) { "OkHttpClient must not be null" },
                request,
                requestInterceptors,
                responseInterceptors,
            )
        }

    }

}