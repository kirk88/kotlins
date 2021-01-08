package com.easy.kotlins.http

import okhttp3.*
import java.io.IOException

abstract class OkRequest<T> {

    protected val requestBuilder: Request.Builder by lazy { Request.Builder() }
    protected val urlBuilder: HttpUrl.Builder by lazy {
        val requestUrl = url ?: throw NullPointerException("Url is null")
        HttpUrl.parse(requestUrl)?.newBuilder() ?: throw IllegalStateException("Url parse failed")
    }

    private var call: Call? = null
    private var creationFailure: Exception? = null

    @Volatile
    private var canceled = false
    private var executed = false

    private var errorMapper: OkMapper<Exception, T>? = null
    private var responseMapper: OkMapper<Response, T>? = null

    private var callback: OkCallback<T>? = null

    var client: OkHttpClient? = null

    var url: String? = null

    var tag: Any? = null
        set(value) {
            field = value
            if (value != null) requestBuilder.tag(value)
        }

    var cacheControl: CacheControl? = null
        set(value) {
            field = value
            if (value != null) requestBuilder.cacheControl(value)
        }

    val isExecuted: Boolean
        get() {
            if (executed) return true
            synchronized(this) { return call?.isExecuted == true }
        }

    val isCanceled: Boolean
        get() {
            if (canceled) return true
            synchronized(this) { return call?.isCanceled == true }
        }

    fun cancel() {
        if (canceled) return
        canceled = true
        synchronized(this) { call?.cancel() }
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

    fun addEncodedQueryParameter(key: String, value: String) {
        urlBuilder.addEncodedQueryParameter(key, value)
    }

    fun removeAllQueryParameters(key: String) {
        urlBuilder.removeAllQueryParameters(key)
    }

    fun removeAllEncodedQueryParameters(key: String) {
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
            callOnError(failure)
            return
        }

        callOnStart()

        call!!.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                try {
                    callOnFailure(e)
                } finally {
                    callOnComplete()
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                try {
                    callOnResponse(response)
                } finally {
                    callOnComplete()
                }
            }
        })
    }

    @Throws(Exception::class)
    protected fun createRealCall(): Call {
        var call: Call?
        synchronized(this) {
            check(!executed) { "Already Executed" }
            check(client != null) { "OkHttpClient is null" }
            executed = true
            call = this.call
            if (creationFailure != null) {
                throw creationFailure as Exception
            }
            if (call == null) {
                try {
                    this.call = client!!.newCall(createRealRequest())
                    call = this.call
                } catch (exception: Exception) {
                    creationFailure = exception
                    throw exception
                }
            }
        }
        return call!!
    }

    @Throws(Exception::class)
    protected open fun onFailure(exception: Exception): Boolean {
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
    ): T? {
        val mapper = responseMapper ?: OkMapper { it as T }
        return mapper.map(response)
    }

    protected open fun mapError(
        exception: Exception,
        errorMapper: OkMapper<Exception, T>?
    ): T? {
        val mapper = errorMapper ?: OkMapper { throw it }
        return mapper.map(exception)
    }

    protected abstract fun createRealRequest(): Request

    protected fun callOnStart() {
        OkCallbacks.onCancel(callback)
    }

    protected fun callOnProgress(bytes: Long, totalBytes: Long) {
        OkCallbacks.onProgress(callback, bytes, totalBytes)
    }

    protected fun callOnSuccess(result: T) {
        OkCallbacks.onSuccess(callback, result)
    }

    protected fun callOnError(exception: Exception) {
        OkCallbacks.onError(callback, exception)
    }

    protected fun callOnCancel() {
        OkCallbacks.onCancel(callback)
    }

    protected fun callOnComplete() {
        OkCallbacks.onComplete(callback)
    }

    @Suppress("UNCHECKED_CAST")
    private fun callOnFailure(exception: Exception) {
        try {
            if (isCanceled) {
                callOnCancel()
                return
            }

            if (onFailure(exception)) {
                return
            }

            val result = mapError(exception, errorMapper)
            if (result != null) {
                callOnSuccess(result)
            } else {
                callOnError(exception)
            }
        } catch (e: Exception) {
            callOnError(e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun callOnResponse(response: Response) {
        try {
            if (onResponse(response)) {
                return
            }

            val result = mapResponse(response, responseMapper)
                ?: throw NullPointerException("Result is null")
            callOnSuccess(result)
        } catch (e: Exception) {
            callOnFailure(e)
        }
    }
}