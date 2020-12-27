package com.easy.kotlins.http

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.easy.kotlins.helper.forEach
import com.easy.kotlins.helper.toJson
import com.easy.kotlins.helper.toJsonObject
import com.easy.kotlins.http.extension.OkExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.*
import java.io.File
import kotlin.collections.set
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Create by LiZhanPing on 2020/8/22
 */

class OkFaker(method: OkRequestMethod) {

    private val request: OkRequest = OkRequest(method)

    private var onStart: (() -> Unit)? = null
    private var onSuccess: ((Any) -> Unit)? = null
    private var onError: ((OkException) -> Unit)? = null
    private var onCancel: (() -> Unit)? = null
    private var onComplete: (() -> Unit)? = null

    private var onDownloadSuccess: ((File) -> Unit)? = null
    private var onDownloadProgress: ((Long, Long) -> Unit)? = null

    val isCanceled: Boolean
        get() = request.isCanceled

    val isExecuted: Boolean
        get() = request.isExecuted

    val tag: Any?
        get() = request.tag

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

    fun tag(tag: Any): OkFaker = this.apply {
        request.tag(tag)
    }

    fun cacheControl(cacheControl: () -> CacheControl): OkFaker = this.apply {
        request.cacheControl(cacheControl())
    }

    fun cacheControl(cacheControl: CacheControl): OkFaker = this.apply {
        request.cacheControl(cacheControl)
    }

    fun extension(extension: OkExtension) = this.apply {
        request.extension(extension)
    }

    fun extension(extension: () -> OkExtension) = this.apply {
        request.extension(extension())
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
            it.value.let { value ->
                when (value) {
                    is BodyFromDataPart -> request.addFormDataPart(
                        it.key,
                        value.filename,
                        value.body
                    )
                    is FileFormDataPart -> request.addFormDataPart(it.key, value.type, value.file)
                    else -> request.addFormDataPart(it.key, value.toString())
                }
            }
        }
    }

    fun formDataParts(vararg formDataParts: Pair<String, Any?>): OkFaker = this.apply {
        formDataParts.forEach {
            it.second.let { value ->
                when (value) {
                    is BodyFromDataPart -> request.addFormDataPart(
                        it.first,
                        value.filename,
                        value.body
                    )
                    is FileFormDataPart -> request.addFormDataPart(
                        it.first,
                        value.type,
                        value.file
                    )
                    else -> request.addFormDataPart(it.first, value.toString())
                }
            }
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

    fun body(body: RequestBody): OkFaker = this.apply {
        request.body(body)
    }

    fun body(body: () -> RequestBody): OkFaker = this.apply {
        request.body(body())
    }

    fun <T> mapResponse(transform: (response: Response) -> T): OkFaker = this.apply {
        request.mapResponse(OkMapper { value ->
            OkResult.Success(transform(value))
        })
    }


    fun <T> mapError(transform: (error: Throwable) -> T): OkFaker = this.apply {
        request.mapError(OkMapper { value ->
            OkResult.Success(transform(value))
        })
    }

    fun onStart(action: () -> Unit): OkFaker = this.apply {
        onStart = action
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> onSuccess(action: (result: T) -> Unit): OkFaker = this.apply {
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

    fun onDownloadSuccess(action: (File) -> Unit): OkFaker = this.apply {
        onDownloadSuccess = action
    }

    fun onDownloadProgress(action: (downloadedBytes: Long, totalBytes: Long) -> Unit): OkFaker =
        this.apply {
            onDownloadProgress = action
        }

    @Throws(Exception::class)
    fun <T : Any> execute(): T {
        return request.execute()
    }

    fun <T : Any> safeExecute(): T? {
        return request.safeExecute()
    }

    fun enqueue(): OkFaker = this.apply {
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

            override fun onCancel() {
                onCancel?.invoke()
            }

            override fun onComplete() {
                onComplete?.invoke()
            }
        })
    }

    fun download(): OkFaker = this.apply {
        request.enqueue(object : OkDownloadCallback {
            override fun onStart() {
                onStart?.invoke()
            }

            override fun onSuccess(result: File) {
                onDownloadSuccess?.invoke(result)
            }

            override fun onProgress(downloadedBytes: Long, totalBytes: Long) {
                onDownloadProgress?.invoke(downloadedBytes, totalBytes)
            }

            override fun onError(error: OkException) {
                onError?.invoke(error)
            }

            override fun onCancel() {
                onCancel?.invoke()
            }

            override fun onComplete() {
                onComplete?.invoke()
            }
        })
    }

    fun cancel() {
        request.cancel()
    }

    companion object {

        fun get(action: OkFaker.() -> Unit): OkFaker =
            OkFaker(OkRequestMethod.GET).apply(action)

        fun post(action: OkFaker.() -> Unit): OkFaker =
            OkFaker(OkRequestMethod.POST).apply(action)

        fun delete(action: OkFaker.() -> Unit): OkFaker =
            OkFaker(OkRequestMethod.DELETE).apply(action)

        fun put(action: OkFaker.() -> Unit): OkFaker =
            OkFaker(OkRequestMethod.PUT).apply(action)

        fun head(action: OkFaker.() -> Unit): OkFaker =
            OkFaker(OkRequestMethod.HEAD).apply(action)

        fun patch(action: OkFaker.() -> Unit): OkFaker =
            OkFaker(OkRequestMethod.PATCH).apply(action)
    }

}

data class BodyFromDataPart(val body: RequestBody, val filename: String? = null)
data class FileFormDataPart(val file: File, val type: MediaType)

class RequestPairs<T> : Iterable<Map.Entry<String, T>> {

    private val pairs: MutableMap<String, T> = mutableMapOf()

    operator fun String.minus(value: T) {
        pairs[this] = value
    }

    operator fun set(key: String, value: T) {
        pairs[key] = value
    }

    fun putAll(pairsFrom: Map<String, T>) {
        pairs.putAll(pairsFrom)
    }

    fun remove(key: String): T? {
        return pairs.remove(key)
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
        val source: String =
            if (copyFrom is RequestPairs<*>) copyFrom.toString() else copyFrom.toJson()
        source.toJsonObject().forEach { key, element ->
            when {
                element == null || element.isJsonNull && serializeNulls -> this[key] =
                    element.toString()
                element.isJsonArray || element.isJsonObject -> this[key] = element.toString()
                element.isJsonPrimitive -> this[key] = element.asString
            }
        }
    }.apply(action)
}

fun <T : Any> OkFaker.asFlow(): Flow<T> = flow<T> {
    emit(execute())
}.flowOn(Dispatchers.IO)

fun <T : Any> OkFaker.asLiveData(
    context: CoroutineContext = EmptyCoroutineContext,
    timeoutInMillis: Long = 5000L
): LiveData<T> = liveData(context, timeoutInMillis) {
    emit(execute<T>())
}