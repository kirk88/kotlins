package com.easy.kotlins.http

import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.IOException
import java.net.URI
import java.net.URL

abstract class OkRequest<T> {

    protected val urlBuilder: HttpUrl.Builder = HttpUrl.Builder()
    protected val requestBuilder: Request.Builder = Request.Builder()

    private var httpUrl: HttpUrl? = null
        set(value) {
            field = requireNotNull(value) { "Url is null" }
            urlBuilder.scheme(value.scheme)
                .encodedUsername(value.encodedUsername)
                .encodedPassword(value.encodedPassword)
                .host(value.host)
                .port(value.port)
                .encodedPath(value.encodedPath)
                .encodedQuery(value.encodedQuery)
                .encodedFragment(value.encodedFragment)
        }

    private var httpClient: OkHttpClient? = null

    private var call: Call? = null
    private var creationFailure: Exception? = null

    @Volatile
    private var canceled = false
    private var executed = false

    private var errorMapper: OkMapper<Exception, T>? = null
    private var responseMapper: OkMapper<Response, T>? = null
    private var callback: OkCallback<T>? = null

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

    fun client(client: OkHttpClient) {
        httpClient = client
    }

    fun url(url: URL) {
        httpUrl = url.toHttpUrlOrNull()
    }

    fun url(uri: URI) {
        httpUrl = uri.toHttpUrlOrNull()
    }

    fun url(url: String) {
        httpUrl = url.toHttpUrlOrNull()
    }

    fun tag(tag: Any) {
        requestBuilder.tag(tag)
    }

    fun cacheControl(cacheControl: CacheControl) {
        requestBuilder.cacheControl(cacheControl)
    }

    fun setHeader(key: String, value: String) {
        requestBuilder.header(key, value)
    }

    fun addHeader(key: String, value: String) {
        requestBuilder.addHeader(key, value)
    }

    fun removeHeader(key: String) {
        requestBuilder.removeHeader(key)
    }

    fun addQueryParameter(key: String, value: String) {
        urlBuilder.addQueryParameter(key, value)
    }

    fun setQueryParameter(key: String, value: String) {
        urlBuilder.setQueryParameter(key, value)
    }

    fun addEncodedQueryParameter(key: String, value: String) {
        urlBuilder.addEncodedQueryParameter(key, value)
    }

    fun setEncodedQueryParameter(key: String, value: String) {
        urlBuilder.setEncodedQueryParameter(key, value)
    }

    fun removeQueryParameters(key: String) {
        urlBuilder.removeAllQueryParameters(key)
    }

    fun removeEncodedQueryParameters(key: String) {
        urlBuilder.removeAllEncodedQueryParameters(key)
    }

    fun mapResponse(mapper: OkMapper<Response, T>) {
        this.responseMapper = mapper
    }

    fun mapError(mapper: OkMapper<Exception, T>) {
        this.errorMapper = mapper
    }

    fun setCallback(callback: OkCallback<T>) {
        this.callback = callback
    }

    @Throws(Exception::class)
    fun execute(): T {
        return try {
            val response = createRealCall().execute()
            mapResponse(response, responseMapper) ?: throw NullPointerException("Result is null")
        } catch (e: Exception) {
            mapError(e, errorMapper) ?: throw e
        }
    }

    fun safeExecute(): T? {
        return try {
            execute()
        } catch (e: Exception) {
            null
        }
    }

    fun enqueue() {
        var call: Call? = null
        var failure: Exception? = null
        try {
            call = createRealCall()
        } catch (e: Exception) {
            failure = e
        }

        if (failure != null) {
            dispatchOnError(failure)
            return
        }

        dispatchOnStart()

        call!!.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                try {
                    dispatchOnFailure(e)
                } finally {
                    dispatchOnComplete()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    dispatchOnResponse(response)
                } finally {
                    dispatchOnComplete()
                }
            }
        })
    }

    @Throws(Exception::class)
    protected fun createRealCall(): Call {
        var realCall: Call?
        synchronized(this) {
            check(!executed) { "Already Executed" }
            check(httpClient != null) { "OkHttpClient is null" }
            executed = true
            realCall = this.call
            if (creationFailure != null) {
                throw creationFailure!!
            }
            if (realCall == null) {
                try {
                    this.call = httpClient!!.newCall(createRealRequest())
                    realCall = this.call
                } catch (e: Exception) {
                    creationFailure = e
                    throw e
                }
            }
        }
        return realCall!!
    }

    @Throws(Exception::class)
    protected open fun onFailure(error: Exception): Boolean {
        return false
    }

    @Throws(Exception::class)
    protected open fun onResponse(response: Response): Boolean {
        return false
    }

    @Suppress("UNCHECKED_CAST")
    protected open fun mapResponse(
        response: Response,
        responseMapper: OkMapper<Response, T>?
    ): T {
        val mapper = responseMapper ?: OkMapper { it.body?.string() as T }
        return mapper.map(response)
    }

    protected open fun mapError(
        error: Exception,
        errorMapper: OkMapper<Exception, T>?
    ): T {
        return errorMapper?.map(error) ?: throw error
    }

    protected abstract fun createRealRequest(): Request

    protected fun dispatchOnStart() {
        OkCallbacks.onStart(callback)
    }

    protected fun dispatchOnProgress(bytes: Long, totalBytes: Long) {
        OkCallbacks.onProgress(callback, bytes, totalBytes)
    }

    protected fun dispatchOnSuccess(result: T) {
        OkCallbacks.onSuccess(callback, result)
    }

    protected fun dispatchOnError(error: Exception) {
        OkCallbacks.onError(callback, error)
    }

    protected fun dispatchOnCancel() {
        OkCallbacks.onCancel(callback)
    }

    protected fun dispatchOnComplete() {
        OkCallbacks.onComplete(callback)
    }

    @Suppress("UNCHECKED_CAST")
    private fun dispatchOnFailure(error: Exception) {
        try {
            if (isCanceled) {
                dispatchOnCancel()
                return
            }

            if (onFailure(error)) {
                return
            }

            if (errorMapper == null) {
                dispatchOnError(error)
            } else {
                dispatchOnSuccess(mapError(error, errorMapper))
            }
        } catch (e: Exception) {
            dispatchOnError(e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun dispatchOnResponse(response: Response) {
        try {
            if (onResponse(response)) {
                return
            }

            dispatchOnSuccess(mapResponse(response, responseMapper))
        } catch (e: Exception) {
            dispatchOnFailure(e)
        }
    }
}