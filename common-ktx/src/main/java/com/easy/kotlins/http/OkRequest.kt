package com.easy.kotlins.http

import com.easy.kotlins.http.extension.DownloadExtension
import com.easy.kotlins.http.extension.DownloadExtension.OnProgressListener
import com.easy.kotlins.http.extension.OkExtension
import okhttp3.*
import java.io.File
import java.io.IOException
import java.net.URL

/**
 * Create by LiZhanPing on 2020/4/27
 */
class OkRequest constructor(private val method: OkRequestMethod) {
    private val requestBuilder: Request.Builder by lazy { Request.Builder() }

    private var httpClient: OkHttpClient? = null

    private var urlBuilder: HttpUrl.Builder? = null
    private var body: RequestBody? = null
    private var formBuilder: FormBody.Builder? = null
    private var multiBuilder: MultipartBody.Builder? = null
    private var call: Call? = null
    private var creationFailure: Throwable? = null

    @Volatile
    private var canceled = false
    private var executed = false
    private var extension: OkExtension? = null
    private var errorMapper: OkMapper<Throwable, *>? = null
    private var responseMapper: OkMapper<Response, *>? = null

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


    fun client(client: OkHttpClient): OkRequest {
        this.httpClient = client
        return this
    }

    fun url(url: String): OkRequest {
        urlBuilder = HttpUrl.parse(url)?.newBuilder()
        return this
    }

    fun tag(tag: Any): OkRequest {
        requestBuilder.tag(tag)
        return this
    }

    fun cacheControl(cacheControl: CacheControl): OkRequest {
        requestBuilder.cacheControl(cacheControl)
        return this
    }

    fun extension(extension: OkExtension): OkRequest {
        this.extension = extension
        return this
    }

    fun addEncodedQueryParameter(key: String, value: String): OkRequest {
        urlBuilder?.addEncodedQueryParameter(key, value)
        return this
    }

    fun addQueryParameter(key: String, value: String): OkRequest {
        urlBuilder?.addQueryParameter(key, value)
        return this
    }

    fun addEncodedQueryParameter(key: String, value: Int): OkRequest {
        urlBuilder?.addEncodedQueryParameter(key, value.toString())
        return this
    }

    fun addQueryParameter(key: String, value: Int): OkRequest {
        urlBuilder?.addQueryParameter(key, value.toString())
        return this
    }

    fun addEncodedQueryParameter(key: String, value: Long): OkRequest {
        urlBuilder?.addEncodedQueryParameter(key, value.toString())
        return this
    }

    fun addQueryParameter(key: String, value: Long): OkRequest {
        urlBuilder?.addQueryParameter(key, value.toString())
        return this
    }

    fun addEncodedQueryParameter(key: String, value: Float): OkRequest {
        urlBuilder?.addEncodedQueryParameter(key, value.toString())
        return this
    }

    fun addQueryParameter(key: String, value: Float): OkRequest {
        urlBuilder?.addQueryParameter(key, value.toString())
        return this
    }

    fun addEncodedQueryParameter(key: String, value: Double): OkRequest {
        urlBuilder?.addEncodedQueryParameter(key, value.toString())
        return this
    }

    fun addQueryParameter(key: String, value: Double): OkRequest {
        urlBuilder?.addQueryParameter(key, value.toString())
        return this
    }

    fun addEncodedQueryParameters(parameters: Map<String, String?>) {
        for ((key, value) in parameters) {
            urlBuilder?.addEncodedQueryParameter(key, value.toString())
        }
    }

    fun addQueryParameters(parameters: Map<String, String?>) {
        for ((key, value) in parameters) {
            urlBuilder?.addQueryParameter(key, value.toString())
        }
    }

    fun removeAllQueryParameters(key: String): OkRequest {
        urlBuilder?.removeAllQueryParameters(key)
        return this
    }

    fun removeAllEncodedQueryParameters(key: String): OkRequest {
        urlBuilder?.removeAllEncodedQueryParameters(key)
        return this
    }

    fun body(mediaType: MediaType?, body: String): OkRequest {
        this.body = RequestBody.create(mediaType, body)
        return this
    }

    fun body(mediaType: MediaType?, file: File): OkRequest {
        body = RequestBody.create(mediaType, file)
        return this
    }

    fun body(body: RequestBody): OkRequest {
        this.body = body
        return this
    }

    fun addFormParameter(key: String, value: String): OkRequest {
        ensureFormBody().add(key, value)
        return this
    }

    fun addFormParameter(key: String, value: Int): OkRequest {
        ensureFormBody().add(key, value.toString())
        return this
    }

    fun addFormParameter(key: String, value: Long): OkRequest {
        ensureFormBody().add(key, value.toString())
        return this
    }

    fun addFormParameter(key: String, value: Float): OkRequest {
        ensureFormBody().add(key, value.toString())
        return this
    }

    fun addFormParameter(key: String, value: Double): OkRequest {
        ensureFormBody().add(key, value.toString())
        return this
    }

    fun addFormParameters(parameters: Map<String, String?>): OkRequest {
        for ((key, value) in parameters) {
            ensureFormBody().add(key, value.toString())
        }
        return this
    }

    fun addEncodedFormParameter(key: String, value: String): OkRequest {
        ensureFormBody().addEncoded(key, value)
        return this
    }

    fun addEncodedFormParameter(key: String, value: Int): OkRequest {
        ensureFormBody().addEncoded(key, value.toString())
        return this
    }

    fun addEncodedFormParameter(key: String, value: Long): OkRequest {
        ensureFormBody().addEncoded(key, value.toString())
        return this
    }

    fun addEncodedFormParameter(key: String, value: Float): OkRequest {
        ensureFormBody().addEncoded(key, value.toString())
        return this
    }

    fun addEncodedFormParameter(key: String, value: Double): OkRequest {
        ensureFormBody().addEncoded(key, value.toString())
        return this
    }

    fun addEncodedFormParameters(parameters: Map<String, Any?>): OkRequest {
        for ((key, value) in parameters) {
            ensureFormBody().addEncoded(key, value.toString())
        }
        return this
    }

    fun addPart(part: MultipartBody.Part): OkRequest {
        ensureMultiBody().addPart(part)
        return this
    }

    fun addPart(body: RequestBody): OkRequest {
        ensureMultiBody().addPart(body)
        return this
    }

    fun addPart(headers: Headers?, body: RequestBody): OkRequest {
        ensureMultiBody().addPart(headers, body)
        return this
    }

    fun addFormDataPart(name: String, value: String): OkRequest {
        ensureMultiBody().addFormDataPart(name, value)
        return this
    }

    fun addFormDataPart(name: String, value: Int): OkRequest {
        ensureMultiBody().addFormDataPart(name, value.toString())
        return this
    }

    fun addFormDataPart(name: String, value: Long): OkRequest {
        ensureMultiBody().addFormDataPart(name, value.toString())
        return this
    }

    fun addFormDataPart(name: String, value: Float): OkRequest {
        ensureMultiBody().addFormDataPart(name, value.toString())
        return this
    }

    fun addFormDataPart(name: String, value: Double): OkRequest {
        ensureMultiBody().addFormDataPart(name, value.toString())
        return this
    }

    fun addFormDataPart(name: String, filename: String?, body: RequestBody): OkRequest {
        ensureMultiBody().addFormDataPart(name, filename, body)
        return this
    }

    fun addFormDataPart(name: String, type: MediaType, file: File): OkRequest {
        ensureMultiBody().addFormDataPart(
            name,
            file.name,
            RequestBody.create(type, file)
        )
        return this
    }

    fun setHeader(key: String, value: String): OkRequest {
        requestBuilder.header(key, value)
        return this
    }

    fun setHeader(key: String, value: Int): OkRequest {
        requestBuilder.header(key, value.toString())
        return this
    }

    fun setHeader(key: String, value: Long): OkRequest {
        requestBuilder.header(key, value.toString())
        return this
    }

    fun setHeader(key: String, value: Float): OkRequest {
        requestBuilder.header(key, value.toString())
        return this
    }

    fun setHeader(key: String, value: Double): OkRequest {
        requestBuilder.header(key, value.toString())
        return this
    }

    fun addHeader(key: String, value: String): OkRequest {
        requestBuilder.addHeader(key, value)
        return this
    }

    fun addHeader(key: String, value: Int): OkRequest {
        requestBuilder.addHeader(key, value.toString())
        return this
    }

    fun addHeader(key: String, value: Long): OkRequest {
        requestBuilder.addHeader(key, value.toString())
        return this
    }

    fun addHeader(key: String, value: Float): OkRequest {
        requestBuilder.addHeader(key, value.toString())
        return this
    }

    fun addHeader(key: String, value: Double): OkRequest {
        requestBuilder.addHeader(key, value.toString())
        return this
    }

    fun removeHeader(key: String): OkRequest {
        requestBuilder.removeHeader(key)
        return this
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

    /**
     * response convert to <T> which you need
     *
     * you must make the <T> type equals the method enqueue or safeExecute return type
     */
    fun <T> mapResponse(mapper: OkMapper<Response, T>): OkRequest {
        responseMapper = mapper
        return this
    }

    /**
     * response convert to <T> which you need
     *
     * you must make the <T> type equals the method enqueue or safeExecute return type
     */
    fun <T> mapError(mapper: OkMapper<Throwable, T>): OkRequest {
        errorMapper = mapper
        return this
    }

    @Throws(Exception::class)
    private fun createCall(): Call {
        val client = requireNotNull(httpClient) { "OkHttpClient must not be null" }
        val url = requireNotNull(urlBuilder?.build()) { "request Url is null or invalid" }
        val body =
            body ?: multiBuilder?.build() ?: formBuilder?.build() ?: FormBody.Builder().build()
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
    private fun <T : Any> transformResponse(
        responseMapper: OkMapper<Response, *>?,
        response: Response
    ): T? {
        val mapper = responseMapper ?: OkMapper {
            OkResult.Success(it as T)
        }
        return when (val result = mapper.transform(response)) {
            is OkResult.Success -> result.data as T
            is OkResult.Error -> throw result.exception
            else -> null
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> transformError(
        errorMapper: OkMapper<Throwable, *>?,
        error: Throwable
    ): T? {
        return when (val result = errorMapper?.transform(error)) {
            is OkResult.Success -> result.data as T
            is OkResult.Error -> throw result.exception
            else -> null
        }
    }

    @Throws(Exception::class)
    fun <T : Any> execute(): T {
        try {
            return transformResponse(responseMapper, rawExecute())
                ?: throw NullPointerException("Result is null")
        } catch (error: Throwable) {
            throw OkException(cause = error)
        }
    }

    fun <T : Any> safeExecute(errorHandler: ((OkException) -> Unit)? = null): T? {
        return try {
            execute()
        } catch (error: Throwable) {
            errorHandler?.invoke(OkException(cause = error))
            null
        }
    }

    fun <T : Any> enqueue(callback: OkCallback<T>? = null) {
        var call: Call?
        var failure: Throwable?
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
    private fun <T : Any> callOnFailure(callback: OkCallback<T>?, exception: Exception) {
        if (isCanceled) {
            OkCallbacks.onCancel(callback)
            return
        }

        try {
            if (extension?.onError(exception) == true) return

            transformError<T>(errorMapper, exception)?.also {
                OkCallbacks.onSuccess(callback, it)
            } ?: throw exception
        } catch (error: Throwable) {
            OkCallbacks.onError(callback, error)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> callOnResponse(callback: OkCallback<T>?, response: Response) {
        try {
            if (extension?.onResponse(response) == true) return

            transformResponse<T>(responseMapper, response)?.also {
                OkCallbacks.onSuccess(callback, it)
            } ?: throw NullPointerException("Response parse failure, the result is null")
        } catch (error: Throwable) {
            OkCallbacks.onError(callback, error)
        }
    }

    private fun callOnDownloadResponse(callback: OkDownloadCallback, response: Response) {
        try {
            check(extension != null && extension is DownloadExtension) { "The extension is null or not a DownloadExtension" }

            val downloadException = extension as DownloadExtension

            downloadException.use {
                val file = it.onResponse(response, object : OnProgressListener {
                    override fun onProgressChanged(downloadedBytes: Long, totalBytes: Long) {
                        OkCallbacks.onProgress(
                            callback,
                            downloadedBytes,
                            totalBytes
                        )
                    }
                })

                when {
                    file != null -> {
                        OkCallbacks.onSuccess(callback, file)
                    }
                    isCanceled -> {
                        OkCallbacks.onCancel(callback)
                    }
                    else -> {
                        OkCallbacks.onError(
                            callback,
                            IOException("Download not completed, response code: " + response.code() + " , message: " + response.message())
                        )
                    }
                }
            }
        } catch (error: Throwable) {
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