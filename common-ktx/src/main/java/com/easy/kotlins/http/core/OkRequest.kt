package com.easy.kotlins.http.core

import com.easy.kotlins.http.core.extension.DownloadExtension
import com.easy.kotlins.http.core.extension.DownloadExtension.OnProgressListener
import okhttp3.*
import java.io.File
import java.io.IOException
import java.net.URL

/**
 * Create by LiZhanPing on 2020/4/27
 * desc: OkHttp请求封装 post 和 get
 */
class OkRequest constructor(private val method: Method) {
    private val requestBuilder: Request.Builder by lazy { Request.Builder() }

    private var client: OkHttpClient? = null

    private var urlBuilder: HttpUrl.Builder? = null
    private var body: RequestBody? = null
    private var formBuilder: FormBody.Builder? = null
    private var multiBuilder: MultipartBody.Builder? = null
    private var call: Call? = null
    private var creationFailure: Throwable? = null

    @Volatile
    private var canceled = false
    private var executed = false
    private var downloadExtension: DownloadExtension? = null
    private var mapErrorFunc: OkFunction<Throwable, *>? = null
    private var mapResponseFunc: OkFunction<Response, *>? = null

    enum class Method {
        GET, POST
    }

    fun client(client: OkHttpClient): OkRequest {
        this.client = client
        return this
    }

    fun url(url: String): OkRequest {
        val httpUrl = HttpUrl.parse(url)
        urlBuilder = httpUrl?.newBuilder()
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

    fun downloadExtension(downloadExtension: DownloadExtension): OkRequest {
        this.downloadExtension = downloadExtension
        downloadExtension.install(this)
        return this
    }

    fun addEncodedQueryParameter(key: String, value: String): OkRequest {
        if (urlBuilder != null) {
            urlBuilder!!.addEncodedQueryParameter(key, value)
        }
        return this
    }

    fun addQueryParameter(key: String, value: String): OkRequest {
        if (urlBuilder != null) {
            urlBuilder!!.addQueryParameter(key, value)
        }
        return this
    }

    fun addEncodedQueryParameter(key: String, value: Int): OkRequest {
        if (urlBuilder != null) {
            urlBuilder!!.addEncodedQueryParameter(key, value.toString())
        }
        return this
    }

    fun addQueryParameter(key: String, value: Int): OkRequest {
        if (urlBuilder != null) {
            urlBuilder!!.addQueryParameter(key, value.toString())
        }
        return this
    }

    fun addEncodedQueryParameter(key: String, value: Long): OkRequest {
        if (urlBuilder != null) {
            urlBuilder!!.addEncodedQueryParameter(key, value.toString())
        }
        return this
    }

    fun addQueryParameter(key: String, value: Long): OkRequest {
        if (urlBuilder != null) {
            urlBuilder!!.addQueryParameter(key, value.toString())
        }
        return this
    }

    fun addEncodedQueryParameter(key: String, value: Float): OkRequest {
        if (urlBuilder != null) {
            urlBuilder!!.addEncodedQueryParameter(key, value.toString())
        }
        return this
    }

    fun addQueryParameter(key: String, value: Float): OkRequest {
        if (urlBuilder != null) {
            urlBuilder!!.addQueryParameter(key, value.toString())
        }
        return this
    }

    fun addEncodedQueryParameter(key: String, value: Double): OkRequest {
        if (urlBuilder != null) {
            urlBuilder!!.addEncodedQueryParameter(key, value.toString())
        }
        return this
    }

    fun addQueryParameter(key: String, value: Double): OkRequest {
        if (urlBuilder != null) {
            urlBuilder!!.addQueryParameter(key, value.toString())
        }
        return this
    }

    fun addEncodedQueryParameters(parameters: Map<String, String?>) {
        if (urlBuilder != null) {
            for ((key, value) in parameters) {
                urlBuilder!!.addEncodedQueryParameter(key, value.toString())
            }
        }
    }

    fun addQueryParameters(parameters: Map<String, String?>) {
        if (urlBuilder != null) {
            for ((key, value) in parameters) {
                urlBuilder!!.addQueryParameter(key, value.toString())
            }
        }
    }

    fun removeAllQueryParameters(key: String): OkRequest {
        if (urlBuilder != null) {
            urlBuilder!!.removeAllQueryParameters(key)
        }
        return this
    }

    fun removeAllEncodedQueryParameters(key: String): OkRequest {
        if (urlBuilder != null) {
            urlBuilder!!.removeAllEncodedQueryParameters(key)
        }
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
        ensureFormBody()
        formBuilder!!.add(key, value)
        return this
    }

    fun addFormParameter(key: String, value: Int): OkRequest {
        ensureFormBody()
        formBuilder!!.add(key, value.toString())
        return this
    }

    fun addFormParameter(key: String, value: Long): OkRequest {
        ensureFormBody()
        formBuilder!!.add(key, value.toString())
        return this
    }

    fun addFormParameter(key: String, value: Float): OkRequest {
        ensureFormBody()
        formBuilder!!.add(key, value.toString())
        return this
    }

    fun addFormParameter(key: String, value: Double): OkRequest {
        ensureFormBody()
        formBuilder!!.add(key, value.toString())
        return this
    }

    fun addFormParameters(parameters: Map<String, String?>): OkRequest {
        ensureFormBody()
        for ((key, value) in parameters) {
            formBuilder!!.add(key, value.toString())
        }
        return this
    }

    fun addEncodedFormParameter(key: String, value: String): OkRequest {
        ensureFormBody()
        formBuilder!!.addEncoded(key, value)
        return this
    }

    fun addEncodedFormParameter(key: String, value: Int): OkRequest {
        ensureFormBody()
        formBuilder!!.addEncoded(key, value.toString())
        return this
    }

    fun addEncodedFormParameter(key: String, value: Long): OkRequest {
        ensureFormBody()
        formBuilder!!.addEncoded(key, value.toString())
        return this
    }

    fun addEncodedFormParameter(key: String, value: Float): OkRequest {
        ensureFormBody()
        formBuilder!!.addEncoded(key, value.toString())
        return this
    }

    fun addEncodedFormParameter(key: String, value: Double): OkRequest {
        ensureFormBody()
        formBuilder!!.addEncoded(key, value.toString())
        return this
    }

    fun addEncodedFormParameters(parameters: Map<String, Any?>): OkRequest {
        ensureFormBody()
        for ((key, value) in parameters) {
            formBuilder!!.addEncoded(key, value.toString())
        }
        return this
    }

    fun addPart(part: MultipartBody.Part): OkRequest {
        ensureMultiBody()
        multiBuilder!!.addPart(part)
        return this
    }

    fun addPart(body: RequestBody): OkRequest {
        ensureMultiBody()
        multiBuilder!!.addPart(body)
        return this
    }

    fun addPart(headers: Headers?, body: RequestBody): OkRequest {
        ensureMultiBody()
        multiBuilder!!.addPart(headers, body)
        return this
    }

    fun addFormDataPart(name: String, value: String): OkRequest {
        ensureMultiBody()
        multiBuilder!!.addFormDataPart(name, value)
        return this
    }

    fun addFormDataPart(name: String, value: Int): OkRequest {
        ensureMultiBody()
        multiBuilder!!.addFormDataPart(name, value.toString())
        return this
    }

    fun addFormDataPart(name: String, value: Long): OkRequest {
        ensureMultiBody()
        multiBuilder!!.addFormDataPart(name, value.toString())
        return this
    }

    fun addFormDataPart(name: String, value: Float): OkRequest {
        ensureMultiBody()
        multiBuilder!!.addFormDataPart(name, value.toString())
        return this
    }

    fun addFormDataPart(name: String, value: Double): OkRequest {
        ensureMultiBody()
        multiBuilder!!.addFormDataPart(name, value.toString())
        return this
    }

    fun addFormDataPart(name: String, filename: String?, body: RequestBody): OkRequest {
        ensureMultiBody()
        multiBuilder!!.addFormDataPart(name, filename, body)
        return this
    }

    fun addFormDataPart(name: String, mediaType: MediaType?, file: File): OkRequest {
        ensureMultiBody()
        multiBuilder!!.addFormDataPart(name, file.name, RequestBody.create(mediaType, file))
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

    private fun ensureFormBody() {
        if (formBuilder == null) {
            formBuilder = FormBody.Builder()
        }
        multiBuilder = null
    }

    private fun ensureMultiBody() {
        if (multiBuilder == null) {
            multiBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
        }
        formBuilder = null
    }

    /**
     * response convert to <T> which you need even request error
     *
     *
     * you must make the <T> type equals the method enqueue or safeExecute return type
     */
    fun <T> mapResponseEvenError(
        func1: OkFunction<Response, T>,
        func2: OkFunction<Throwable, T>
    ): OkRequest {
        mapResponseFunc = func1
        mapErrorFunc = func2
        return this
    }

    /**
     * response convert to <T> which you need
     *
     * you must make the <T> type equals the method enqueue or safeExecute return type
     */
    fun <T> mapResponse(func: OkFunction<Response, T>): OkRequest {
        mapResponseFunc = func
        return this
    }

    /**
     * response convert to <T> which you need
     *
     * you must make the <T> type equals the method enqueue or safeExecute return type
     */
    fun <T> mapError(func: OkFunction<Throwable, T>): OkRequest {
        mapErrorFunc = func
        return this
    }

    @Throws(Exception::class)
    private fun createCall(): Call {
        requireNotNull(urlBuilder) { "request Url is null or invalid" }
        val builder = when (method) {
            Method.POST -> {
                body = when {
                    multiBuilder != null -> multiBuilder!!.build()
                    formBuilder != null -> formBuilder!!.build()
                    else -> FormBody.Builder().build()
                }
                requestBuilder.url(urlBuilder!!.build()).post(body!!)
            }
            Method.GET -> requestBuilder.url(
                urlBuilder!!.build()
            ).get()
        }
        downloadExtension?.addHeaderTo(builder)
        return requireNotNull(client) { "OkHttpClient must not be null" }.newCall(builder.build())
    }

    @Throws(Exception::class)
    fun rawExecute(): Response {
        var call: Call?
        synchronized(this) {
            check(!executed) { "Already Executed" }
            executed = true
            call = this.call
            if (creationFailure != null) {
                throw (creationFailure as Exception?)!!
            }
            if (call == null) {
                try {
                    this.call = createCall()
                    call = this.call
                } catch (e: Exception) {
                    creationFailure = e
                    throw e
                }
            }
        }
        return call!!.execute()
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(Exception::class)
    fun <T> execute(): T? {
        return when (val source = mapResponseFunc?.apply(rawExecute())) {
            is OkResult.Success -> source.data as T?
            is OkResult.Error -> throw source.exception
            else -> throw NullPointerException("mapResponseFunc must not be null")
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> safeExecute(defResult: T): T {
        return try {
            execute() ?: defResult
        } catch (t: Throwable) {
            try {
                when (val source = mapErrorFunc?.apply(t)) {
                    is OkResult.Success -> source.data as T
                    is OkResult.Error -> throw source.exception
                    else -> defResult
                }
            } catch (t1: Throwable) {
                defResult
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> safeExecute(): T? {
        return safeExecute(null)
    }

    @JvmOverloads
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
                } catch (t: Exception) {
                    failure = t
                }
            }
        }
        if (failure != null) {
            onError(callback, failure!!)
            return
        }
        if (!canceled) {
            onStart(callback)
        }
        call!!.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                try {
                    callOnFailure(callback, e)
                } finally {
                    onComplete(callback)
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
                    onComplete(callback)
                }
            }
        })
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> callOnFailure(callback: OkCallback<T>?, e: Exception) {
        try {
            when (val source = mapErrorFunc?.apply(e)) {
                is OkResult.Success -> onSuccess(callback, source.data as T)
                is OkResult.Error -> throw source.exception
                else -> onError(callback, e)
            }
        } catch (t: Throwable) {
            onError(callback, t)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> callOnResponse(callback: OkCallback<T>?, response: Response) {
        try {
            when (val source = mapResponseFunc?.apply(response)) {
                is OkResult.Success -> onSuccess(callback, source.data as T)
                is OkResult.Error -> throw source.exception
                else -> onSuccess(callback as OkCallback<String>?, response.body()?.string()!!)
            }
        } catch (t: Throwable) {
            onError(callback, t)
        }
    }

    private fun callOnDownloadResponse(callback: OkDownloadCallback, response: Response) {
        try {
            if (downloadExtension == null) {
                throw NullPointerException("download extension must not be null")
            }
            val progressListener = object : OnProgressListener {
                override fun onProgress(downloadedBytes: Long, totalBytes: Long) {
                    this@OkRequest.onProgress(
                        callback,
                        downloadedBytes,
                        totalBytes
                    )
                }
            }
            val destFile = downloadExtension!!.download(response, progressListener)
            when {
                destFile != null -> {
                    onSuccess(callback, destFile)
                }
                isCanceled -> {
                    onCancel(callback)
                }
                else -> {
                    onError(
                        callback,
                        IOException("download not completed, response code: " + response.code() + " , message: " + response.message())
                    )
                }
            }
        } catch (e: Exception) {
            onError(callback, e)
        }
    }

    private fun <T : Any> onSuccess(callback: OkCallback<T>?, result: T) {
        OkCallbacks.success(callback, result)
    }

    private fun onError(callback: OkCallback<*>?, error: Throwable) {
        OkCallbacks.error(callback, error)
    }

    private fun onStart(callback: OkCallback<*>?) {
        OkCallbacks.start(callback)
    }

    private fun onComplete(callback: OkCallback<*>?) {
        OkCallbacks.complete(callback)
    }

    private fun onProgress(callback: OkDownloadCallback, downloadedBytes: Long, totalBytes: Long) {
        if (isCanceled) return
        OkCallbacks.progress(callback, downloadedBytes, totalBytes)
    }

    private fun onCancel(callback: OkDownloadCallback) {
        OkCallbacks.cancel(callback)
    }

    fun client(): OkHttpClient? {
        return client
    }

    fun url(): URL? {
        return if (call != null) call!!.request().url().url() else null
    }

    fun tag(): Any? {
        return if (call != null) call!!.request().tag() else null
    }

    fun cacheControl(): CacheControl? {
        return if (call != null) call!!.request().cacheControl() else null
    }

    val isExecuted: Boolean
        get() {
            if (executed) {
                return true
            }
            synchronized(this) { return call != null && call!!.isExecuted }
        }
    val isCanceled: Boolean
        get() {
            if (canceled) {
                return true
            }
            synchronized(this) { return call != null && call!!.isCanceled }
        }

    fun cancel() {
        if (canceled) {
            return
        }
        canceled = true
        var call: Call?
        synchronized(this) { call = this.call }
        if (call != null) {
            call!!.cancel()
        }
    }
}