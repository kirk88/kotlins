package com.easy.kotlins.http

import com.easy.kotlins.helper.asJsonObject
import com.easy.kotlins.helper.forEach
import com.easy.kotlins.helper.toJson
import com.easy.kotlins.helper.toJsonObject
import com.easy.kotlins.http.core.*
import com.easy.kotlins.http.core.extension.DownloadExtension
import okhttp3.*
import java.io.File
import kotlin.collections.set

/**
 * Create by LiZhanPing on 2020/8/22
 */

class NiceOkFaker(val okFaker: OkFaker) {

    private var onStart: (() -> Unit)? = null
    private var onSuccess: ((Any) -> Unit)? = null
    private var onError: ((Throwable) -> Unit)? = null
    private var onComplete: (() -> Unit)? = null

    private var onDownloadStart: (() -> Unit)? = null
    private var onDownloadCancel: (() -> Unit)? = null
    private var onDownloadSuccess: ((File) -> Unit)? = null
    private var onDownloadError: ((Throwable) -> Unit)? = null
    private var onDownloadProgress: ((Long, Long) -> Unit)? = null
    private var onDownloadComplete: (() -> Unit)? = null

    val isCanceled: Boolean
        get() = okFaker.isCanceled

    val isExecuted: Boolean
        get() = okFaker.isExecuted

    val tag: Any?
        get() = okFaker.tag()

    inline fun client(crossinline client: () -> OkHttpClient): NiceOkFaker = this.apply {
        okFaker.client(client())
    }

    fun client(client: OkHttpClient): NiceOkFaker = this.apply {
        okFaker.client(client)
    }

    fun downloadExtension(downloadExtension: DownloadExtension): NiceOkFaker = this.apply {
        okFaker.downloadExtension(downloadExtension)
    }

    inline fun downloadExtension(crossinline downloadExtension: () -> DownloadExtension): NiceOkFaker =
        this.apply {
            okFaker.downloadExtension(downloadExtension())
        }

    inline fun url(crossinline url: () -> String): NiceOkFaker = this.apply {
        okFaker.url(url())
    }

    fun url(url: String): NiceOkFaker = this.apply {
        okFaker.url(url)
    }

    inline fun tag(crossinline tag: () -> Any): NiceOkFaker = this.apply {
        okFaker.tag(tag())
    }

    fun tag(tag: Any) {
        okFaker.tag(tag)
    }

    fun cacheControl(cacheControl: () -> CacheControl): NiceOkFaker = this.apply {
        okFaker.cacheControl(cacheControl())
    }

    fun cacheControl(cacheControl: CacheControl): NiceOkFaker = this.apply {
        okFaker.cacheControl(cacheControl)
    }

    inline fun headers(crossinline action: RequestPairs<Any?>.() -> Unit): NiceOkFaker =
        this.apply {
            RequestPairs<Any?>().apply(action).forEach {
                okFaker.setHeader(it.key, it.value.toString())
            }
        }

    fun headers(headers: Map<String, Any?>): NiceOkFaker = this.apply {
        headers.forEach {
            okFaker.setHeader(it.key, it.value.toString())
        }
    }

    fun headers(vararg headers: Pair<String, Any?>): NiceOkFaker = this.apply {
        headers.forEach {
            okFaker.setHeader(it.first, it.second.toString())
        }
    }

    inline fun queryParameters(crossinline action: RequestPairs<Any?>.() -> Unit): NiceOkFaker =
        this.apply {
            RequestPairs<Any?>().apply(action).forEach {
                okFaker.addQueryParameter(it.key, it.value.toString())
            }
        }

    fun queryParameters(queryParameters: Map<String, Any?>): NiceOkFaker = this.apply {
        queryParameters.forEach {
            okFaker.addQueryParameter(it.key, it.value.toString())
        }
    }

    fun queryParameters(vararg queryParameters: Pair<String, Any?>): NiceOkFaker = this.apply {
        queryParameters.forEach {
            okFaker.addQueryParameter(it.first, it.second.toString())
        }
    }

    inline fun encodedQueryParameters(crossinline action: RequestPairs<Any?>.() -> Unit): NiceOkFaker =
        this.apply {
            RequestPairs<Any?>().apply(action).forEach {
                okFaker.addEncodedQueryParameter(it.key, it.value.toString())
            }
        }

    fun encodedQueryParameters(encodedQueryParameters: Map<String, Any?>): NiceOkFaker =
        this.apply {
            encodedQueryParameters.forEach {
                okFaker.addEncodedQueryParameter(it.key, it.value.toString())
            }
        }

    fun encodedQueryParameters(vararg encodedQueryParameters: Pair<String, Any?>): NiceOkFaker =
        this.apply {
            encodedQueryParameters.forEach {
                okFaker.addEncodedQueryParameter(it.first, it.second.toString())
            }
        }

    inline fun formParameters(crossinline action: RequestPairs<Any?>.() -> Unit): NiceOkFaker =
        this.apply {
            RequestPairs<Any?>().apply(action).forEach {
                okFaker.addFormParameter(it.key, it.value.toString())
            }
        }

    fun formParameters(formParameters: Map<String, Any?>): NiceOkFaker = this.apply {
        formParameters.forEach {
            okFaker.addFormParameter(it.key, it.value.toString())
        }
    }

    fun formParameters(vararg formParameters: Pair<String, Any?>): NiceOkFaker = this.apply {
        formParameters.forEach {
            okFaker.addFormParameter(it.first, it.second.toString())
        }
    }

    inline fun encodedFormParameters(crossinline action: RequestPairs<Any?>.() -> Unit): NiceOkFaker =
        this.apply {
            RequestPairs<Any?>().apply(action).forEach {
                okFaker.addEncodedFormParameter(it.key, it.value.toString())
            }
        }

    fun encodedFormParameters(encodedFormParameters: Map<String, Any?>): NiceOkFaker = this.apply {
        encodedFormParameters.forEach {
            okFaker.addEncodedFormParameter(it.key, it.value.toString())
        }
    }

    fun encodedFormParameters(vararg encodedFormParameters: Pair<String, Any?>): NiceOkFaker =
        this.apply {
            encodedFormParameters.forEach {
                okFaker.addEncodedFormParameter(it.first, it.second.toString())
            }
        }

    inline fun formDataParts(crossinline action: RequestPairs<Any?>.() -> Unit): NiceOkFaker =
        this.apply {
            RequestPairs<Any?>().apply(action).forEach {
                okFaker.addFormDataPart(it.key, it.value.toString())
            }
        }

    fun formDataParts(formDataParts: Map<String, Any?>): NiceOkFaker = this.apply {
        formDataParts.forEach {
            okFaker.addFormDataPart(it.key, it.value.toString())
        }
    }

    fun formDataParts(vararg formDataParts: Pair<String, Any?>): NiceOkFaker = this.apply {
        formDataParts.forEach {
            okFaker.addFormDataPart(it.first, it.second.toString())
        }
    }

    inline fun formDataBodyParts(action: RequestPairs<Pair<String, RequestBody>>.() -> Unit): NiceOkFaker =
        this.apply {
            RequestPairs<Pair<String, RequestBody>>().apply(action).forEach {
                okFaker.addFormDataPart(it.key, it.value.first, it.value.second)
            }
        }

    fun formDataBodyParts(formDataBodyParts: Iterable<Map.Entry<String, Pair<String, RequestBody>>>): NiceOkFaker =
        this.apply {
            formDataBodyParts.forEach {
                okFaker.addFormDataPart(it.key, it.value.first, it.value.second)
            }
        }

    inline fun formDataFileParts(crossinline action: RequestPairs<Pair<MediaType, File>>.() -> Unit): NiceOkFaker =
        this.apply {
            RequestPairs<Pair<MediaType, File>>().apply(action).forEach {
                okFaker.addFormDataPart(it.key, it.value.first, it.value.second)
            }
        }

    fun formDataFileParts(formDataFileParts: Iterable<Map.Entry<String, Pair<MediaType, File>>>): NiceOkFaker =
        this.apply {
            formDataFileParts.forEach {
                okFaker.addFormDataPart(it.key, it.value.first, it.value.second)
            }
        }

    inline fun parts(crossinline action: ArrayList<RequestBody>.() -> Unit): NiceOkFaker =
        this.apply {
            arrayListOf<RequestBody>().apply(action).forEach {
                okFaker.addPart(it)
            }
        }

    inline fun multiParts(crossinline action: ArrayList<MultipartBody.Part>.() -> Unit): NiceOkFaker =
        this.apply {
            arrayListOf<MultipartBody.Part>().apply(action).forEach {
                okFaker.addPart(it)
            }
        }

    inline fun body(crossinline body: () -> RequestBody): NiceOkFaker = this.apply {
        okFaker.body(body())
    }

    inline fun <T> mapResponse(crossinline action: (response: String) -> T): NiceOkFaker =
        this.apply {
            okFaker.mapResponse(OkFunction<Response, OkSource<T>> {
                return@OkFunction OkSource.just(action(it.body()!!.string()))
            })
        }

    inline fun <T> mapError(crossinline action: (error: Throwable) -> T): NiceOkFaker = this.apply {
        okFaker.mapError(OkFunction<Throwable, OkSource<T>> {
            return@OkFunction OkSource.just(action(it))
        })
    }

    fun onStart(action: () -> Unit): NiceOkFaker = this.apply {
        onStart = action
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> onSuccess(action: (result: T) -> Unit): NiceOkFaker = this.apply {
        onSuccess = { action(it as T) }
    }

    fun onSuccess(action: () -> Unit): NiceOkFaker = this.apply {
        onSuccess = { action() }
    }

    fun onError(action: (error: Throwable) -> Unit): NiceOkFaker = this.apply {
        onError = action
    }

    fun onComplete(action: () -> Unit): NiceOkFaker = this.apply {
        onComplete = action
    }

    fun onDownloadStart(action: () -> Unit): NiceOkFaker = this.apply {
        onDownloadStart = action
    }

    fun onDownloadSuccess(action: (file: File) -> Unit): NiceOkFaker = this.apply {
        onDownloadSuccess = action
    }

    fun onDownloadError(action: (error: Throwable) -> Unit): NiceOkFaker = this.apply {
        onDownloadError = action
    }

    fun onDownloadProgress(action: (downloadedBytes: Long, totalBytes: Long) -> Unit): NiceOkFaker =
        this.apply {
            onDownloadProgress = action
        }

    fun onDownloadCancel(action: () -> Unit): NiceOkFaker = this.apply {
        onDownloadCancel = action
    }

    fun onDownloadComplete(action: () -> Unit): NiceOkFaker = this.apply {
        onDownloadComplete = action
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(Exception::class)
    fun <T> execute(): T? {
        return okFaker.execute<Any?>() as? T
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> safeExecute(defaultValue: T): T {
        return okFaker.safeExecute<T?>() ?: defaultValue
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> safeExecute(defaultValue: () -> T): T {
        return okFaker.safeExecute<T?>() ?: defaultValue()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> safeExecute(): T? {
        return okFaker.safeExecute<T?>()
    }

    fun request(): NiceOkFaker = this.apply {
        okFaker.enqueue(object : OkStartedCallback<Any> {
            override fun onStart() {
                onStart?.invoke()
            }

            override fun onSuccess(result: Any) {
                onSuccess?.invoke(result)
                onComplete?.invoke()
            }

            override fun onError(error: Throwable) {
                onError?.invoke(error)
                onComplete?.invoke()
            }
        })
    }

    fun download(): NiceOkFaker = this.apply {
        okFaker.enqueue(object : OkDownloadCallback {
            override fun onSuccess(result: File) {
                onDownloadSuccess?.invoke(result)
                onDownloadComplete?.invoke()
            }

            override fun onProgress(downloadedBytes: Long, totalBytes: Long) {
                onDownloadProgress?.invoke(downloadedBytes, totalBytes)
            }

            override fun onError(error: Throwable) {
                onDownloadError?.invoke(error)
                onDownloadComplete?.invoke()
            }

            override fun onStart() {
                onDownloadStart?.invoke()
            }

            override fun onCancel() {
                onDownloadCancel?.invoke()
            }

        })
    }

    fun cancel() {
        okFaker.cancel()
    }

    companion object {

        fun get(action: NiceOkFaker.() -> Unit): NiceOkFaker {
            return NiceOkFaker(OkFaker.newGet()).apply(action)
        }

        fun post(action: NiceOkFaker.() -> Unit): NiceOkFaker {
            return NiceOkFaker(OkFaker.newPost()).apply(action)
        }

    }

}

inline class RequestPairs<T>(
    private val pairs: Map<String, T> = mutableMapOf()
) : Iterable<Map.Entry<String, T>> {

    operator fun String.minus(value: T) {
        (pairs as MutableMap)[this] = value
    }

    fun put(key: String, value: T) {
        key.minus(value)
    }

    fun putAll(pairsFrom: Map<String, T>) {
        (pairs as MutableMap<String, T>).putAll(pairsFrom)
    }

    fun remove(key: String): T? {
        return (pairs as MutableMap<String, T>).remove(key)
    }

    override fun toString(): String {
        return pairs.toJson()
    }

    fun toArrayString(): String {
        return listOf(pairs).toJson()
    }

    override fun iterator(): Iterator<Map.Entry<String, T>> {
        return pairs.iterator()
    }
}

inline fun requestPairsOf(crossinline action: RequestPairs<Any?>.() -> Unit): RequestPairs<Any?> {
    return RequestPairs<Any?>().apply(action)
}

inline fun requestPairsOf(
    vararg pairs: Pair<String, Any?>,
    crossinline action: RequestPairs<Any?>.() -> Unit = {}
): RequestPairs<Any?> {
    return RequestPairs<Any?>().apply { putAll(pairs.toMap()) }.apply(action)
}

inline fun requestPairsOf(
    copyFrom: Any,
    serializeNulls: Boolean = false,
    crossinline action: RequestPairs<Any?>.() -> Unit = {}
): RequestPairs<Any?> {
    return RequestPairs<Any?>().apply {
        val source = if (copyFrom is String) copyFrom.toJsonObject() else copyFrom.asJsonObject()
        source.forEach { key, element ->
            when {
                element == null || element.isJsonNull && serializeNulls -> key - element.toString()
                element.isJsonArray || element.isJsonObject -> key - element.toString()
                element.isJsonPrimitive -> key - element.asString
            }
        }
    }.apply(action)
}