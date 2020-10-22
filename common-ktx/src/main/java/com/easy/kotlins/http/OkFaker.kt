package com.easy.kotlins.http

import com.easy.kotlins.helper.toJsonObject
import com.easy.kotlins.helper.forEach
import com.easy.kotlins.helper.toJson
import com.easy.kotlins.http.core.*
import com.google.gson.JsonObject
import okhttp3.*
import java.io.File
import kotlin.collections.set

/**
 * Create by LiZhanPing on 2020/8/22
 */

class OkFaker(private val okFaker: OkRequest) {

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

    fun client(client: () -> OkHttpClient): OkFaker = this.apply {
        okFaker.client(client())
    }

    fun client(client: OkHttpClient): OkFaker = this.apply {
        okFaker.client(client)
    }

    fun url(url: () -> String): OkFaker = this.apply {
        okFaker.url(url())
    }

    fun url(url: String): OkFaker = this.apply {
        okFaker.url(url)
    }

    fun tag(tag: () -> Any): OkFaker = this.apply {
        okFaker.tag(tag())
    }

    fun tag(tag: Any) {
        okFaker.tag(tag)
    }

    fun cacheControl(cacheControl: () -> CacheControl): OkFaker = this.apply {
        okFaker.cacheControl(cacheControl())
    }

    fun cacheControl(cacheControl: CacheControl): OkFaker = this.apply {
        okFaker.cacheControl(cacheControl)
    }

    fun headers(action: RequestPairs<Any?>.() -> Unit): OkFaker = this.apply {
        RequestPairs<Any?>().apply(action).forEach {
            okFaker.setHeader(it.key, it.value.toString())
        }
    }

    fun headers(headers: Map<String, Any?>): OkFaker = this.apply {
        headers.forEach {
            okFaker.setHeader(it.key, it.value.toString())
        }
    }

    fun headers(vararg headers: Pair<String, Any?>): OkFaker = this.apply {
        headers.forEach {
            okFaker.setHeader(it.first, it.second.toString())
        }
    }

    fun queryParameters(action: RequestPairs<Any?>.() -> Unit): OkFaker = this.apply {
        RequestPairs<Any?>().apply(action).forEach {
            okFaker.addQueryParameter(it.key, it.value.toString())
        }
    }

    fun queryParameters(queryParameters: Map<String, Any?>): OkFaker = this.apply {
        queryParameters.forEach {
            okFaker.addQueryParameter(it.key, it.value.toString())
        }
    }

    fun queryParameters(vararg queryParameters: Pair<String, Any?>): OkFaker = this.apply {
        queryParameters.forEach {
            okFaker.addQueryParameter(it.first, it.second.toString())
        }
    }

    fun encodedQueryParameters(action: RequestPairs<Any?>.() -> Unit): OkFaker = this.apply {
        RequestPairs<Any?>().apply(action).forEach {
            okFaker.addEncodedQueryParameter(it.key, it.value.toString())
        }
    }

    fun encodedQueryParameters(encodedQueryParameters: Map<String, Any?>): OkFaker = this.apply {
        encodedQueryParameters.forEach {
            okFaker.addEncodedQueryParameter(it.key, it.value.toString())
        }
    }

    fun encodedQueryParameters(vararg encodedQueryParameters: Pair<String, Any?>): OkFaker = this.apply {
        encodedQueryParameters.forEach {
            okFaker.addEncodedQueryParameter(it.first, it.second.toString())
        }
    }

    fun formParameters(action: RequestPairs<Any?>.() -> Unit): OkFaker = this.apply {
        RequestPairs<Any?>().apply(action).forEach {
            okFaker.addFormParameter(it.key, it.value.toString())
        }
    }

    fun formParameters(formParameters: Map<String, Any?>): OkFaker = this.apply {
        formParameters.forEach {
            okFaker.addFormParameter(it.key, it.value.toString())
        }
    }

    fun formParameters(vararg formParameters: Pair<String, Any?>): OkFaker = this.apply {
        formParameters.forEach {
            okFaker.addFormParameter(it.first, it.second.toString())
        }
    }

    fun encodedFormParameters(action: RequestPairs<Any?>.() -> Unit): OkFaker = this.apply {
        RequestPairs<Any?>().apply(action).forEach {
            okFaker.addEncodedFormParameter(it.key, it.value.toString())
        }
    }

    fun encodedFormParameters(encodedFormParameters: Map<String, Any?>): OkFaker = this.apply {
        encodedFormParameters.forEach {
            okFaker.addEncodedFormParameter(it.key, it.value.toString())
        }
    }

    fun encodedFormParameters(vararg encodedFormParameters: Pair<String, Any?>): OkFaker = this.apply {
        encodedFormParameters.forEach {
            okFaker.addEncodedFormParameter(it.first, it.second.toString())
        }
    }

    fun formDataParts(action: RequestPairs<Any?>.() -> Unit): OkFaker = this.apply {
        RequestPairs<Any?>().apply(action).forEach {
            okFaker.addFormDataPart(it.key, it.value.toString())
        }
    }

    fun formDataParts(formDataParts: Map<String, Any?>): OkFaker = this.apply {
        formDataParts.forEach {
            okFaker.addFormDataPart(it.key, it.value.toString())
        }
    }

    fun formDataParts(vararg formDataParts: Pair<String, Any?>): OkFaker = this.apply {
        formDataParts.forEach {
            okFaker.addFormDataPart(it.first, it.second.toString())
        }
    }

    fun formDataBodyParts(action: RequestPairs<Pair<String, RequestBody>>.() -> Unit): OkFaker = this.apply {
        RequestPairs<Pair<String, RequestBody>>().apply(action).forEach {
            okFaker.addFormDataPart(it.key, it.value.first, it.value.second)
        }
    }

    fun formDataBodyParts(formDataBodyParts: Iterable<Map.Entry<String, Pair<String, RequestBody>>>): OkFaker = this.apply {
        formDataBodyParts.forEach {
            okFaker.addFormDataPart(it.key, it.value.first, it.value.second)
        }
    }

    fun formDataFileParts(action: RequestPairs<Pair<MediaType, File>>.() -> Unit): OkFaker = this.apply {
        RequestPairs<Pair<MediaType, File>>().apply(action).forEach {
            okFaker.addFormDataPart(it.key, it.value.first, it.value.second)
        }
    }

    fun formDataFileParts(formDataFileParts: Iterable<Map.Entry<String, Pair<MediaType, File>>>): OkFaker = this.apply {
        formDataFileParts.forEach {
            okFaker.addFormDataPart(it.key, it.value.first, it.value.second)
        }
    }

    fun parts(action: ArrayList<RequestBody>.() -> Unit): OkFaker = this.apply {
        arrayListOf<RequestBody>().apply(action).forEach {
            okFaker.addPart(it)
        }
    }

    fun multiParts(action: ArrayList<MultipartBody.Part>.() -> Unit): OkFaker = this.apply {
        arrayListOf<MultipartBody.Part>().apply(action).forEach {
            okFaker.addPart(it)
        }
    }

    fun body(body: () -> RequestBody): OkFaker = this.apply {
        okFaker.body(body())
    }

    fun <T> mapResponse(action: (response: String) -> T): OkFaker = this.apply {
        okFaker.mapResponse(OkFunction<Response, OkSource<T>> {
            return@OkFunction OkSource.just(action(it.body()!!.string()))
        })
    }


    fun <T> mapJsonResponse(action: (response: JsonObject) -> T): OkFaker = this.apply {
        okFaker.mapResponse(OkFunction<Response, OkSource<T>> {
            return@OkFunction OkSource.just(action(it.body()!!.string().toJsonObject()))
        })
    }

    fun <T> mapError(action: (error: Throwable) -> T): OkFaker = this.apply {
        okFaker.mapError(OkFunction<Throwable, OkSource<T>> {
            return@OkFunction OkSource.just(action(it))
        })
    }

    fun onStart(action: () -> Unit): OkFaker = this.apply {
        onStart = action
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> onSuccess(action: (result: T) -> Unit): OkFaker = this.apply {
        onSuccess = { action(it as T) }
    }

    fun onSuccess(action: () -> Unit): OkFaker = this.apply {
        onSuccess = { action() }
    }

    fun onError(action: (error: Throwable) -> Unit): OkFaker = this.apply {
        onError = action
    }

    fun onComplete(action: () -> Unit): OkFaker = this.apply {
        onComplete = action
    }

    fun onDownloadStart(action: () -> Unit): OkFaker = this.apply {
        onDownloadStart = action
    }

    fun onDownloadSuccess(action: (file: File) -> Unit): OkFaker = this.apply {
        onDownloadSuccess = action
    }

    fun onDownloadError(action: (error: Throwable) -> Unit): OkFaker = this.apply {
        onDownloadError = action
    }

    fun onDownloadProgress(action: (downloadedBytes: Long, totalBytes: Long) -> Unit): OkFaker = this.apply {
        onDownloadProgress = action
    }

    fun onDownloadCancel(action: () -> Unit): OkFaker = this.apply {
        onDownloadCancel = action
    }

    fun onDownloadComplete(action: () -> Unit): OkFaker = this.apply {
        onDownloadComplete = action
    }

    fun onStartAndComplete(onStart: () -> Unit, onComplete: () -> Unit): OkFaker = this.apply {
        onStart(onStart)
        onComplete(onComplete)
    }

    fun <T> onResult(onSuccess: (result: T) -> Unit, onError: (error: Throwable) -> Unit): OkFaker = this.apply {
        onSuccess(onSuccess)
        onError(onError)
    }

    fun onSingleResult(onSuccess: () -> Unit, onError: (error: Throwable) -> Unit): OkFaker = this.apply {
        onSuccess(onSuccess)
        onError(onError)
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

    fun request(): OkFaker = this.apply {
        okFaker.enqueue(object : OkStartedCallback<Any> {
            override fun onStart() {
                onStart?.invoke()
            }

            override fun onSuccess(result: Any) {
                onSuccess?.invoke(result)
            }

            override fun onError(error: Throwable) {
                onError?.invoke(error)
            }

            override fun onComplete() {
                onComplete?.invoke()
            }
        })
    }

    fun download(): OkFaker = this.apply {
        okFaker.enqueue(object : OkDownloadCallback {

            override fun onStart() {
                onDownloadStart?.invoke()
            }

            override fun onSuccess(result: File) {
                onDownloadSuccess?.invoke(result)
            }

            override fun onProgress(downloadedBytes: Long, totalBytes: Long) {
                onDownloadProgress?.invoke(downloadedBytes, totalBytes)
            }

            override fun onError(error: Throwable) {
                onDownloadError?.invoke(error)
            }

            override fun onComplete() {
                onDownloadComplete?.invoke()
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

        fun get(url: String? = null, action: (OkFaker.() -> Unit)? = null): OkFaker {
            return OkFaker(OkFaker.newGet()).apply {
                url?.let { url(it) }
                action?.invoke(this)
            }
        }

        fun post(url: String? = null, action: (OkFaker.() -> Unit)? = null): OkFaker {
            return OkFaker(OkFaker.newPost()).apply {
                url?.let { url(it) }
                action?.invoke(this)
            }
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
        val source = if (copyFrom is String) copyFrom.toJsonObject() else copyFrom.toJsonObject()
        source.forEach { key, element ->
            when {
                element == null || element.isJsonNull && serializeNulls -> key - element.toString()
                element.isJsonArray || element.isJsonObject -> key - element.toString()
                element.isJsonPrimitive -> key - element.asString
            }
        }
    }.apply(action)
}