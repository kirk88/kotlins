package com.easy.kotlins.http

import com.easy.kotlins.http.extension.DownloadExtension
import com.easy.kotlins.http.extension.OkCommonExtension
import com.easy.kotlins.http.extension.OkDownloadExtension
import com.easy.kotlins.http.extension.OkExtension
import okhttp3.*
import java.io.File
import java.io.IOException
import java.net.URL

/**
 * Create by LiZhanPing on 2020/4/27
 */
internal class OkRequest<T> constructor(private val method: OkRequestMethod) {
    private val requestBuilder: Request.Builder by lazy { Request.Builder() }

    private var httpClient: OkHttpClient? = null

    private var urlBuilder: HttpUrl.Builder? = null
    private var requestBody: RequestBody? = null
    private var formBuilder: FormBody.Builder? = null
    private var multiBuilder: MultipartBody.Builder? = null
    private var call: Call? = null
    private var creationFailure: Exception? = null

    @Volatile
    private var canceled = false
    private var executed = false
    private var extension: OkExtension? = null
    private var errorMapper: OkMapper<Exception, T>? = null
    private var responseMapper: OkMapper<Response, T>? = null

    val client: OkHttpClient?
        get() = httpClient

    val url: URL?
        get() = call?.request()?.url()?.url()

    val tag: Any?
        get() = call?.request()?.tag()

    val cacheControl: CacheControl?
        get() = call?.request()?.cacheControl()

    val isExecuted: Boolean
        get() {
            if (executed) {
                return true
            }
            synchronized(this) { return call?.isExecuted == true }
        }

    val isCanceled: Boolean
        get() {
            if (canceled) {
                return true
            }
            synchronized(this) { return call?.isCanceled == true }
        }


    fun client(client: OkHttpClient): OkRequest<T> = this.apply {
        this.httpClient = client
    }

    fun url(url: String): OkRequest<T> = this.apply {
        urlBuilder = HttpUrl.parse(url)?.newBuilder()
    }

    fun tag(tag: Any): OkRequest<T> = this.apply {
        requestBuilder.tag(tag)
    }

    fun cacheControl(cacheControl: CacheControl): OkRequest<T> = this.apply {
        requestBuilder.cacheControl(cacheControl)
    }

    fun extension(extension: OkExtension): OkRequest<T> = this.apply {
        this.extension = extension
    }

    fun addEncodedQueryParameter(key: String, value: String): OkRequest<T> = this.apply {
        urlBuilder?.addEncodedQueryParameter(key, value)
    }

    fun addQueryParameter(key: String, value: String): OkRequest<T> = this.apply {
        urlBuilder?.addQueryParameter(key, value)
    }

    fun addEncodedQueryParameter(key: String, value: Int): OkRequest<T> = this.apply {
        urlBuilder?.addEncodedQueryParameter(key, value.toString())
    }

    fun addQueryParameter(key: String, value: Int): OkRequest<T> = this.apply {
        urlBuilder?.addQueryParameter(key, value.toString())
    }

    fun addEncodedQueryParameter(key: String, value: Long): OkRequest<T> = this.apply {
        urlBuilder?.addEncodedQueryParameter(key, value.toString())
    }

    fun addQueryParameter(key: String, value: Long): OkRequest<T> = this.apply {
        urlBuilder?.addQueryParameter(key, value.toString())
    }

    fun addEncodedQueryParameter(key: String, value: Float): OkRequest<T> = this.apply {
        urlBuilder?.addEncodedQueryParameter(key, value.toString())
    }

    fun addQueryParameter(key: String, value: Float): OkRequest<T> = this.apply {
        urlBuilder?.addQueryParameter(key, value.toString())
    }

    fun addEncodedQueryParameter(key: String, value: Double): OkRequest<T> = this.apply {
        urlBuilder?.addEncodedQueryParameter(key, value.toString())
    }

    fun addQueryParameter(key: String, value: Double): OkRequest<T> = this.apply {
        urlBuilder?.addQueryParameter(key, value.toString())
    }

    fun addEncodedQueryParameters(parameters: Map<String, String?>): OkRequest<T> = this.apply {
        for ((key, value) in parameters) {
            urlBuilder?.addEncodedQueryParameter(key, value.toString())
        }
    }

    fun addQueryParameters(parameters: Map<String, String?>): OkRequest<T> = this.apply {
        for ((key, value) in parameters) {
            urlBuilder?.addQueryParameter(key, value.toString())
        }
    }

    fun removeAllQueryParameters(key: String): OkRequest<T> = this.apply {
        urlBuilder?.removeAllQueryParameters(key)
    }

    fun removeAllEncodedQueryParameters(key: String): OkRequest<T> = this.apply {
        urlBuilder?.removeAllEncodedQueryParameters(key)
    }

    fun body(mediaType: MediaType?, body: String): OkRequest<T> = this.apply {
        requestBody = RequestBody.create(mediaType, body)
    }

    fun body(mediaType: MediaType?, file: File): OkRequest<T> = this.apply {
        requestBody = RequestBody.create(mediaType, file)
    }

    fun body(body: RequestBody): OkRequest<T> = this.apply {
        requestBody = body
    }

    fun addFormParameter(key: String, value: String): OkRequest<T> = this.apply {
        ensureFormBody().add(key, value)
    }

    fun addFormParameter(key: String, value: Int): OkRequest<T> = this.apply {
        ensureFormBody().add(key, value.toString())
    }

    fun addFormParameter(key: String, value: Long): OkRequest<T> = this.apply {
        ensureFormBody().add(key, value.toString())
    }

    fun addFormParameter(key: String, value: Float): OkRequest<T> = this.apply {
        ensureFormBody().add(key, value.toString())
    }

    fun addFormParameter(key: String, value: Double): OkRequest<T> = this.apply {
        ensureFormBody().add(key, value.toString())
    }

    fun addFormParameters(parameters: Map<String, String?>): OkRequest<T> = this.apply {
        for ((key, value) in parameters) {
            ensureFormBody().add(key, value.toString())
        }
    }

    fun addEncodedFormParameter(key: String, value: String): OkRequest<T> = this.apply {
        ensureFormBody().addEncoded(key, value)
    }

    fun addEncodedFormParameter(key: String, value: Int): OkRequest<T> = this.apply {
        ensureFormBody().addEncoded(key, value.toString())
    }

    fun addEncodedFormParameter(key: String, value: Long): OkRequest<T> = this.apply {
        ensureFormBody().addEncoded(key, value.toString())
    }

    fun addEncodedFormParameter(key: String, value: Float): OkRequest<T> = this.apply {
        ensureFormBody().addEncoded(key, value.toString())
    }

    fun addEncodedFormParameter(key: String, value: Double): OkRequest<T> = this.apply {
        ensureFormBody().addEncoded(key, value.toString())
    }

    fun addEncodedFormParameters(parameters: Map<String, Any?>): OkRequest<T> = this.apply {
        for ((key, value) in parameters) {
            ensureFormBody().addEncoded(key, value.toString())
        }
    }

    fun addPart(part: MultipartBody.Part): OkRequest<T> = this.apply {
        ensureMultiBody().addPart(part)
    }

    fun addPart(body: RequestBody): OkRequest<T> = this.apply {
        ensureMultiBody().addPart(body)
    }

    fun addPart(headers: Headers?, body: RequestBody): OkRequest<T> = this.apply {
        ensureMultiBody().addPart(headers, body)
    }

    fun addFormDataPart(name: String, value: String): OkRequest<T> = this.apply {
        ensureMultiBody().addFormDataPart(name, value)
    }

    fun addFormDataPart(name: String, value: Int): OkRequest<T> = this.apply {
        ensureMultiBody().addFormDataPart(name, value.toString())
    }

    fun addFormDataPart(name: String, value: Long): OkRequest<T> = this.apply {
        ensureMultiBody().addFormDataPart(name, value.toString())
    }

    fun addFormDataPart(name: String, value: Float): OkRequest<T> = this.apply {
        ensureMultiBody().addFormDataPart(name, value.toString())
    }

    fun addFormDataPart(name: String, value: Double): OkRequest<T> = this.apply {
        ensureMultiBody().addFormDataPart(name, value.toString())
    }

    fun addFormDataPart(name: String, filename: String?, body: RequestBody): OkRequest<T> =
        this.apply {
            ensureMultiBody().addFormDataPart(name, filename, body)
        }

    fun addFormDataPart(name: String, type: MediaType?, file: File): OkRequest<T> = this.apply {
        ensureMultiBody().addFormDataPart(
            name,
            file.name,
            RequestBody.create(type, file)
        )
    }

    fun setHeader(key: String, value: String): OkRequest<T> = this.apply {
        requestBuilder.header(key, value)
    }

    fun setHeader(key: String, value: Int): OkRequest<T> = this.apply {
        requestBuilder.header(key, value.toString())
    }

    fun setHeader(key: String, value: Long): OkRequest<T> = this.apply {
        requestBuilder.header(key, value.toString())
    }

    fun setHeader(key: String, value: Float): OkRequest<T> = this.apply {
        requestBuilder.header(key, value.toString())
    }

    fun setHeader(key: String, value: Double): OkRequest<T> = this.apply {
        requestBuilder.header(key, value.toString())
    }

    fun addHeader(key: String, value: String): OkRequest<T> = this.apply {
        requestBuilder.addHeader(key, value)
    }

    fun addHeader(key: String, value: Int): OkRequest<T> = this.apply {
        requestBuilder.addHeader(key, value.toString())
    }

    fun addHeader(key: String, value: Long): OkRequest<T> = this.apply {
        requestBuilder.addHeader(key, value.toString())
    }

    fun addHeader(key: String, value: Float): OkRequest<T> = this.apply {
        requestBuilder.addHeader(key, value.toString())
    }

    fun addHeader(key: String, value: Double): OkRequest<T> = this.apply {
        requestBuilder.addHeader(key, value.toString())
    }

    fun removeHeader(key: String): OkRequest<T> = this.apply {
        requestBuilder.removeHeader(key)
    }

    private fun ensureFormBody(): FormBody.Builder {
        return formBuilder ?: FormBody.Builder().also {
            formBuilder = it
            multiBuilder = null
        }
    }

    private fun ensureMultiBody(): MultipartBody.Builder {
        return multiBuilder ?: MultipartBody.Builder().setType(MultipartBody.FORM).also {
            multiBuilder = it
            formBuilder = null
        }
    }

    fun mapResponse(mapper: OkMapper<Response, T>): OkRequest<T> = this.apply {
        responseMapper = mapper
    }

    fun mapError(mapper: OkMapper<Exception, T>): OkRequest<T> = this.apply {
        errorMapper = mapper
    }

    @Throws(Exception::class)
    private fun createCall(): Call {
        val client = requireNotNull(httpClient) { "OkHttpClient must not be null" }
        val url = requireNotNull(urlBuilder?.build()) { "request Url is null or invalid" }
        val body =
            requestBody ?: formBuilder?.build() ?: multiBuilder?.build() ?: FormBody.Builder()
                .build()
        return requestBuilder.url(url).let {
            when (method) {
                OkRequestMethod.GET -> it.get()
                OkRequestMethod.POST -> it.post(body)
                OkRequestMethod.DELETE -> it.delete(body)
                OkRequestMethod.PUT -> it.put(body)
                OkRequestMethod.HEAD -> it.head()
                OkRequestMethod.PATCH -> it.patch(body)
            }
        }.let {
            val request: Request = it.build()
            client.newCall(extension?.shouldInterceptRequest(request) ?: request)
        }
    }

    @Throws(Exception::class)
    private fun rawExecute(): Response {
        var call: Call?
        synchronized(this) {
            check(!executed) { "Already Executed" }
            executed = true
            call = this.call
            if (creationFailure != null) {
                throw creationFailure as Exception
            }
            if (call == null) {
                try {
                    this.call = createCall()
                    call = this.call
                } catch (exception: Exception) {
                    creationFailure = exception
                    throw exception
                }
            }
        }
        return call!!.execute()
    }

    @Suppress("UNCHECKED_CAST")
    private fun transformResponse(
        response: Response,
        responseMapper: OkMapper<Response, T>?
    ): T? {
        val mapper = responseMapper ?: OkMapper {
            OkResult.Success(it as T)
        }
        return when (val result = mapper.transform(response)) {
            is OkResult.Success -> result.data
            is OkResult.Error -> throw result.exception
        }
    }

    private fun transformError(
        error: Exception,
        errorMapper: OkMapper<Exception, T>?
    ): T? {
        return when (val result = errorMapper?.transform(error)) {
            is OkResult.Success -> result.data
            is OkResult.Error -> throw result.exception
            else -> null
        }
    }

    @Throws(Exception::class)
    fun execute(): T {
        return try {
            transformResponse(rawExecute(), responseMapper)
                ?: throw NullPointerException("Result is null")
        } catch (error: Exception) {
            transformError(error, errorMapper) ?: throw error
        }
    }

    fun safeExecute(): T? {
        return try {
            execute()
        } catch (error: Exception) {
            null
        }
    }

    fun enqueue(callback: OkCallback<T>? = null) {
        var call: Call?
        var failure: Exception?
        synchronized(this) {
            call = this.call
            failure = creationFailure
            if (executed) failure = IllegalStateException("Already Executed")
            executed = true
            if (call == null && failure == null) {
                try {
                    this.call = createCall()
                    call = this.call
                } catch (exception: Exception) {
                    failure = exception
                }
            }
        }
        if (failure != null) {
            OkCallbacks.onError(callback, failure!!)
            return
        }

        OkCallbacks.onStart(callback)

        call!!.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                try {
                    callOnFailure(callback, e)
                } finally {
                    OkCallbacks.onComplete(callback)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                try {
                    if (callback is OkDownloadCallback) {
                        callOnDownloadResponse(callback as OkDownloadCallback, response)
                    } else {
                        callOnResponse(callback, response)
                    }
                } finally {
                    OkCallbacks.onComplete(callback)
                }
            }
        })
    }

    @Suppress("UNCHECKED_CAST")
    private fun callOnFailure(callback: OkCallback<T>?, exception: Exception) {
        if (isCanceled) {
            OkCallbacks.onCancel(callback)
            return
        }

        try {
            if ((extension as? OkCommonExtension)?.onError(exception) == true) return

            transformError(exception, errorMapper)?.also {
                OkCallbacks.onSuccess(callback, it)
            } ?: throw exception
        } catch (error: Exception) {
            OkCallbacks.onError(callback, error)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun callOnResponse(callback: OkCallback<T>?, response: Response) {
        try {
            if ((extension as? OkCommonExtension)?.onResponse(response) == true) return

            transformResponse(response, responseMapper)?.also {
                OkCallbacks.onSuccess(callback, it)
            } ?: throw NullPointerException("Response parse failure, the result is null")
        } catch (error: Exception) {
            callOnFailure(callback, error)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun callOnDownloadResponse(callback: OkDownloadCallback, response: Response) {
        try {
            check(extension != null && extension is OkDownloadExtension) { "The extension is null or not a OkDownloadExtension" }

            val file: File = (extension as OkDownloadExtension).onResponse(response) { downloadedBytes, totalBytes ->
                OkCallbacks.onProgress(callback, downloadedBytes, totalBytes)
            }

            OkCallbacks.onSuccess(callback, file)
        } catch (error: Exception) {
            if (isCanceled) {
                OkCallbacks.onCancel(callback)
                return
            }

            OkCallbacks.onError(callback, error)
        }
    }

    fun cancel() {
        if (canceled) {
            return
        }
        canceled = true
        synchronized(this) { call?.cancel() }
    }
}