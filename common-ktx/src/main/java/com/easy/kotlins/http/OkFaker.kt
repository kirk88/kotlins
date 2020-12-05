package com.easy.kotlins.http

import com.easy.kotlins.helper.forEach
import com.easy.kotlins.helper.toJson
import com.easy.kotlins.helper.toJsonObject
import com.easy.kotlins.http.core.*
import com.google.gson.JsonObject
import okhttp3.*
import java.io.File
import kotlin.collections.set

/**
 * Create by LiZhanPing on 2020/8/22
 */

class OkFaker(private val request: OkRequest) {

    private var onStart: (() -> Unit)? = null
    private var onSuccess: ((Any) -> Unit)? = null
    private var onError: ((OkException) -> Unit)? = null
    private var onComplete: (() -> Unit)? = null

    private var onDownloadStart: (() -> Unit)? = null
    private var onDownloadCancel: (() -> Unit)? = null
    private var onDownloadSuccess: ((File) -> Unit)? = null
    private var onDownloadError: ((OkException) -> Unit)? = null
    private var onDownloadProgress: ((Long, Long) -> Unit)? = null
    private var onDownloadComplete: (() -> Unit)? = null

    val isCanceled: Boolean
        get() = request.isCanceled

    val isExecuted: Boolean
        get() = request.isExecuted

    val tag: Any?
        get() = request.tag()

    fun client(client: () -> OkHttpClient): OkFaker = this.apply {
        request.client(client())
    }

    fun client(client: OkHttpClient): OkFaker = this.apply {
        request.client(client)
    }

    fun url(url: () -> String): OkFaker = this.apply {
        request.url(url())
    }

    fun url(url: String): OkFaker = this.apply {
        request.url(url)
    }

    fun tag(tag: () -> Any): OkFaker = this.apply {
        request.tag(tag())
    }

    fun tag(tag: Any) {
        request.tag(tag)
    }

    fun cacheControl(cacheControl: () -> CacheControl): OkFaker = this.apply {
        request.cacheControl(cacheControl())
    }

    fun cacheControl(cacheControl: CacheControl): OkFaker = this.apply {
        request.cacheControl(cacheControl)
    }

    fun headers(action: RequestPairs<Any?>.() -> Unit): OkFaker = this.apply {
        RequestPairs<Any?>().apply(action).forEach {
            request.setHeader(it.key, it.value.toString())
        }
    }

    fun headers(headers: Map<String, Any?>): OkFaker = this.apply {
        headers.forEach {
            request.setHeader(it.key, it.value.toString())
        }
    }

    fun headers(vararg headers: Pair<String, Any?>): OkFaker = this.apply {
        headers.forEach {
            request.setHeader(it.first, it.second.toString())
        }
    }

    fun queryParameters(action: RequestPairs<Any?>.() -> Unit): OkFaker = this.apply {
        RequestPairs<Any?>().apply(action).forEach {
            request.addQueryParameter(it.key, it.value.toString())
        }
    }

    fun queryParameters(queryParameters: Map<String, Any?>): OkFaker = this.apply {
        queryParameters.forEach {
            request.addQueryParameter(it.key, it.value.toString())
        }
    }

    fun queryParameters(vararg queryParameters: Pair<String, Any?>): OkFaker = this.apply {
        queryParameters.forEach {
            request.addQueryParameter(it.first, it.second.toString())
        }
    }

    fun encodedQueryParameters(action: RequestPairs<Any?>.() -> Unit): OkFaker = this.apply {
        RequestPairs<Any?>().apply(action).forEach {
            request.addEncodedQueryParameter(it.key, it.value.toString())
        }
    }

    fun encodedQueryParameters(encodedQueryParameters: Map<String, Any?>): OkFaker = this.apply {
        encodedQueryParameters.forEach {
            request.addEncodedQueryParameter(it.key, it.value.toString())
        }
    }

    fun encodedQueryParameters(vararg encodedQueryParameters: Pair<String, Any?>): OkFaker =
        this.apply {
            encodedQueryParameters.forEach {
                request.addEncodedQueryParameter(it.first, it.second.toString())
            }
        }

    fun formParameters(action: RequestPairs<Any?>.() -> Unit): OkFaker = this.apply {
        RequestPairs<Any?>().apply(action).forEach {
            request.addFormParameter(it.key, it.value.toString())
        }
    }

    fun formParameters(formParameters: Map<String, Any?>): OkFaker = this.apply {
        formParameters.forEach {
            request.addFormParameter(it.key, it.value.toString())
        }
    }

    fun formParameters(vararg formParameters: Pair<String, Any?>): OkFaker = this.apply {
        formParameters.forEach {
            request.addFormParameter(it.first, it.second.toString())
        }
    }

    fun encodedFormParameters(action: RequestPairs<Any?>.() -> Unit): OkFaker = this.apply {
        RequestPairs<Any?>().apply(action).forEach {
            request.addEncodedFormParameter(it.key, it.value.toString())
        }
    }

    fun encodedFormParameters(encodedFormParameters: Map<String, Any?>): OkFaker = this.apply {
        encodedFormParameters.forEach {
            request.addEncodedFormParameter(it.key, it.value.toString())
        }
    }

    fun encodedFormParameters(vararg encodedFormParameters: Pair<String, Any?>): OkFaker =
        this.apply {
            encodedFormParameters.forEach {
                request.addEncodedFormParameter(it.first, it.second.toString())
            }
        }

    fun formDataParts(action: RequestPairs<Any?>.() -> Unit): OkFaker = this.apply {
        RequestPairs<Any?>().apply(action).forEach {
            request.addFormDataPart(it.key, it.value.toString())
        }
    }

    fun formDataParts(formDataParts: Map<String, Any?>): OkFaker = this.apply {
        formDataParts.forEach {
            request.addFormDataPart(it.key, it.value.toString())
        }
    }

    fun formDataParts(vararg formDataParts: Pair<String, Any?>): OkFaker = this.apply {
        formDataParts.forEach {
            request.addFormDataPart(it.first, it.second.toString())
        }
    }

    fun formDataBodyParts(action: RequestPairs<Pair<String, RequestBody>>.() -> Unit): OkFaker =
        this.apply {
            RequestPairs<Pair<String, RequestBody>>().apply(action).forEach {
                request.addFormDataPart(it.key, it.value.first, it.value.second)
            }
        }

    fun formDataBodyParts(formDataBodyParts: Iterable<Map.Entry<String, Pair<String, RequestBody>>>): OkFaker =
        this.apply {
            formDataBodyParts.forEach {
                request.addFormDataPart(it.key, it.value.first, it.value.second)
            }
        }

    fun formDataFileParts(action: RequestPairs<Pair<MediaType, File>>.() -> Unit): OkFaker =
        this.apply {
            RequestPairs<Pair<MediaType, File>>().apply(action).forEach {
                request.addFormDataPart(it.key, it.value.first, it.value.second)
            }
        }

    fun formDataFileParts(formDataFileParts: Iterable<Map.Entry<String, Pair<MediaType, File>>>): OkFaker =
        this.apply {
            formDataFileParts.forEach {
                request.addFormDataPart(it.key, it.value.first, it.value.second)
            }
        }

    fun parts(action: ArrayList<RequestBody>.() -> Unit): OkFaker = this.apply {
        arrayListOf<RequestBody>().apply(action).forEach {
            request.addPart(it)
        }
    }

    fun multiParts(action: ArrayList<MultipartBody.Part>.() -> Unit): OkFaker = this.apply {
        arrayListOf<MultipartBody.Part>().apply(action).forEach {
            request.addPart(it)
        }
    }

    fun body(body: () -> RequestBody): OkFaker = this.apply {
        request.body(body())
    }

    fun <T> mapRawResponse(action: (response: Response) -> T): OkFaker = this.apply {
        request.mapResponse(OkFunction { r -> OkResult.Success(action(r)) })
    }


    fun <T> mapResponse(action: (response: JsonObject) -> T): OkFaker = this.apply {
        request.mapResponse(OkFunction { r -> OkResult.Success(action(r.body()!!.string().toJsonObject())) })
    }

    fun <T> mapError(action: (error: Throwable) -> T): OkFaker = this.apply {
        request.mapError(OkFunction { r -> OkResult.Success(action(r)) })
    }

    fun onStart(action: () -> Unit): OkFaker = this.apply {
        onStart = action
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> onSuccess(action: (result: T) -> Unit): OkFaker = this.apply {
        onSuccess = { action(it as T) }
    }

    fun onSimpleSuccess(action: () -> Unit): OkFaker = this.apply {
        onSuccess = { action() }
    }

    fun onError(action: (error: OkException) -> Unit): OkFaker = this.apply {
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

    fun onDownloadError(action: (error: OkException) -> Unit): OkFaker = this.apply {
        onDownloadError = action
    }

    fun onDownloadProgress(action: (downloadedBytes: Long, totalBytes: Long) -> Unit): OkFaker =
        this.apply {
            onDownloadProgress = action
        }

    fun onDownloadCancel(action: () -> Unit): OkFaker = this.apply {
        onDownloadCancel = action
    }

    fun onDownloadComplete(action: () -> Unit): OkFaker = this.apply {
        onDownloadComplete = action
    }

    fun <T> onResult(onSuccess: (result: T) -> Unit, onError: (error: OkException) -> Unit): OkFaker =
        this.apply {
            onSuccess(onSuccess)
            onError(onError)
        }

    @Suppress("UNCHECKED_CAST")
    @Throws(Exception::class)
    fun <T> execute(): T? {
        return request.execute() as? T
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> safeExecute(defaultValue: T): T {
        return request.safeExecute() ?: defaultValue
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> safeExecute(defaultValue: () -> T): T {
        return request.safeExecute() ?: defaultValue()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> safeExecute(): T? {
        return request.safeExecute()
    }

    fun request(): OkFaker = this.apply {
        request.enqueue(object : OkCallback<Any> {
            override fun onStart() {
                onStart?.invoke()
            }

            override fun onSuccess(result: Any) {
                onSuccess?.invoke(result)
            }

            override fun onError(error: OkException) {
                onError?.invoke(error)
            }

            override fun onComplete() {
                onComplete?.invoke()
            }
        })
    }

    fun download(): OkFaker = this.apply {
        request.enqueue(object : OkDownloadCallback {

            override fun onStart() {
                onDownloadStart?.invoke()
            }

            override fun onSuccess(result: File) {
                onDownloadSuccess?.invoke(result)
            }

            override fun onProgress(downloadedBytes: Long, totalBytes: Long) {
                onDownloadProgress?.invoke(downloadedBytes, totalBytes)
            }

            override fun onError(error: OkException) {
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
        request.cancel()
    }

    companion object {

        fun get(action: OkFaker.() -> Unit): OkFaker {
            return OkFaker(OkRequest(OkRequest.Method.GET)).apply(action)
        }

        fun post(action: OkFaker.() -> Unit): OkFaker {
            return OkFaker(OkRequest(OkRequest.Method.POST)).apply(action)
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