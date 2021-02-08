package com.easy.kotlins.http

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.easy.kotlins.helper.forEach
import com.easy.kotlins.helper.toJSON
import com.easy.kotlins.helper.toJSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.*
import java.io.File
import java.net.URI
import java.net.URL
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class OkFaker<T> internal constructor(method: OkRequestMethod) {

    private val builder = OkRequest.Builder(method)

    private var request: OkRequest? = null

    val isCanceled: Boolean
        get() = request?.isCanceled ?: false

    val isExecuted: Boolean
        get() = request?.isExecuted ?: false

    val tag: Any?
        get() = request?.tag

    private var onStartApplied = false
    private val onStartActions: MutableList<SimpleAction> by lazy { mutableListOf() }

    private var onSuccessApplied = false
    private val onSuccessActions: MutableList<Action<T>> by lazy { mutableListOf() }

    private var onErrorApplied = false
    private val onErrorActions: MutableList<Action<Exception>> by lazy { mutableListOf() }

    private var onCancelApplied = false
    private val onCancelActions: MutableList<SimpleAction> by lazy { mutableListOf() }

    private var onCompleteApplied = false
    private val onCompleteActions: MutableList<SimpleAction> by lazy { mutableListOf() }

    private val transformer = OkTransformer<T>()

    fun client(client: OkHttpClient) = this.apply {
        builder.client(client)
    }

    fun url(url: String) = this.apply {
        builder.url(url)
    }

    fun url(url: URL) = this.apply {
        builder.url(url)
    }

    fun uri(uri: URI) = this.apply {
        builder.url(uri)
    }

    fun tag(tag: Any) = this.apply {
        builder.tag(tag)
    }

    fun cacheControl(cacheControl: CacheControl) = this.apply {
        builder.cacheControl(cacheControl)
    }

    fun headers(operation: RequestPairs<Any?>.() -> Unit) = this.apply {
        RequestPairs<Any?>().apply(operation).forEach {
            builder.header(it.key, it.value.toString())
        }
    }

    fun headers(headers: Map<String, Any?>) = this.apply {
        headers.forEach {
            builder.header(it.key, it.value.toString())
        }
    }

    fun headers(vararg headers: Pair<String, Any?>) = this.apply {
        headers.forEach {
            builder.header(it.first, it.second.toString())
        }
    }

    fun addHeaders(operation: RequestPairs<Any?>.() -> Unit) = this.apply {
        RequestPairs<Any?>().apply(operation).forEach {
            builder.addHeader(it.key, it.value.toString())
        }
    }

    fun addHeaders(headers: Map<String, Any?>) = this.apply {
        headers.forEach {
            builder.addHeader(it.key, it.value.toString())
        }
    }

    fun addHeaders(vararg headers: Pair<String, Any?>) = this.apply {
        headers.forEach {
            builder.addHeader(it.first, it.second.toString())
        }
    }

    fun username(username: String) = this.apply {
        builder.username(username)
    }

    fun password(password: String) = this.apply {
        builder.password(password)
    }

    fun queryParameters(operation: RequestPairs<Any?>.() -> Unit) = this.apply {
        RequestPairs<Any?>().apply(operation).forEach {
            builder.setQueryParameter(it.key, it.value.toString())
        }
    }

    fun queryParameters(queryParameters: Map<String, Any?>) = this.apply {
        queryParameters.forEach {
            builder.setQueryParameter(it.key, it.value.toString())
        }
    }

    fun queryParameters(vararg queryParameters: Pair<String, Any?>) = this.apply {
        queryParameters.forEach {
            builder.setQueryParameter(it.first, it.second.toString())
        }
    }

    fun addQueryParameters(operation: RequestPairs<Any?>.() -> Unit) = this.apply {
        RequestPairs<Any?>().apply(operation).forEach {
            builder.addQueryParameter(it.key, it.value.toString())
        }
    }

    fun addQueryParameters(queryParameters: Map<String, Any?>) = this.apply {
        queryParameters.forEach {
            builder.addQueryParameter(it.key, it.value.toString())
        }
    }

    fun addQueryParameters(vararg queryParameters: Pair<String, Any?>) = this.apply {
        queryParameters.forEach {
            builder.addQueryParameter(it.first, it.second.toString())
        }
    }

    fun encodedQueryParameters(operation: RequestPairs<Any?>.() -> Unit) = this.apply {
        RequestPairs<Any?>().apply(operation).forEach {
            builder.setEncodedQueryParameter(it.key, it.value.toString())
        }
    }

    fun encodedQueryParameters(encodedQueryParameters: Map<String, Any?>) = this.apply {
        encodedQueryParameters.forEach {
            builder.setEncodedQueryParameter(it.key, it.value.toString())
        }
    }

    fun encodedQueryParameters(vararg encodedQueryParameters: Pair<String, Any?>) = this.apply {
        encodedQueryParameters.forEach {
            builder.setEncodedQueryParameter(it.first, it.second.toString())
        }
    }

    fun addEncodedQueryParameters(operation: RequestPairs<Any?>.() -> Unit) = this.apply {
        RequestPairs<Any?>().apply(operation).forEach {
            builder.addEncodedQueryParameter(it.key, it.value.toString())
        }
    }

    fun addEncodedQueryParameters(encodedQueryParameters: Map<String, Any?>) = this.apply {
        encodedQueryParameters.forEach {
            builder.addEncodedQueryParameter(it.key, it.value.toString())
        }
    }

    fun addEncodedQueryParameters(vararg encodedQueryParameters: Pair<String, Any?>) = this.apply {
        encodedQueryParameters.forEach {
            builder.addEncodedQueryParameter(it.first, it.second.toString())
        }
    }

    fun formParameters(operation: RequestPairs<Any?>.() -> Unit) = this.apply {
        RequestPairs<Any?>().apply(operation).forEach {
            builder.addFormParameter(it.key, it.value.toString())
        }
    }

    fun formParameters(formParameters: Map<String, Any?>) = this.apply {
        formParameters.forEach {
            builder.addFormParameter(it.key, it.value.toString())
        }
    }

    fun formParameters(vararg formParameters: Pair<String, Any?>) = this.apply {
        formParameters.forEach {
            builder.addFormParameter(it.first, it.second.toString())
        }
    }

    fun encodedFormParameters(operation: RequestPairs<Any?>.() -> Unit) = this.apply {
        RequestPairs<Any?>().apply(operation).forEach {
            builder.addEncodedFormParameter(it.key, it.value.toString())
        }
    }

    fun encodedFormParameters(encodedFormParameters: Map<String, Any?>) = this.apply {
        encodedFormParameters.forEach {
            builder.addEncodedFormParameter(it.key, it.value.toString())
        }
    }

    fun encodedFormParameters(vararg encodedFormParameters: Pair<String, Any?>) = this.apply {
        encodedFormParameters.forEach {
            builder.addEncodedFormParameter(it.first, it.second.toString())
        }
    }

    fun parts(vararg parts: BodyPart) = this.apply {
        parts.forEach {
            builder.addPart(it.headers, it.body)
        }
    }

    fun formDataParts(operation: RequestPairs<Any?>.() -> Unit) = this.apply {
        RequestPairs<Any?>().apply(operation).forEach {
            it.value.let { value ->
                when (value) {
                    is BodyFormDataPart -> builder.addFormDataPart(
                        it.key,
                        value.filename,
                        value.body
                    )
                    is FileFormDataPart -> builder.addFormDataPart(
                        it.key,
                        value.contentType,
                        value.file
                    )
                    else -> builder.addFormDataPart(it.key, value.toString())
                }
            }
        }
    }

    fun formDataParts(formDataParts: Map<String, Any?>) = this.apply {
        formDataParts.forEach {
            it.value.let { value ->
                when (value) {
                    is BodyFormDataPart -> builder.addFormDataPart(
                        it.key,
                        value.filename,
                        value.body
                    )
                    is FileFormDataPart -> builder.addFormDataPart(
                        it.key,
                        value.contentType,
                        value.file
                    )
                    else -> builder.addFormDataPart(it.key, value.toString())
                }
            }
        }
    }

    fun formDataParts(vararg formDataParts: Pair<String, Any?>) = this.apply {
        formDataParts.forEach {
            it.second.let { value ->
                when (value) {
                    is BodyFormDataPart -> builder.addFormDataPart(
                        it.first,
                        value.filename,
                        value.body
                    )
                    is FileFormDataPart -> builder.addFormDataPart(
                        it.first,
                        value.contentType,
                        value.file
                    )
                    else -> builder.addFormDataPart(it.first, value.toString())
                }
            }
        }
    }

    fun parts(operation: MutableList<RequestBody>.() -> Unit) = this.apply {
        mutableListOf<RequestBody>().apply(operation).forEach {
            builder.addPart(it)
        }
    }

    fun multiParts(operation: MutableList<MultipartBody.Part>.() -> Unit) = this.apply {
        mutableListOf<MultipartBody.Part>().apply(operation).forEach {
            builder.addPart(it)
        }
    }

    fun body(body: RequestBody) = this.apply {
        builder.body(body)
    }

    fun body(body: () -> RequestBody) = this.apply {
        builder.body(body())
    }

    fun addRequestInterceptor(interceptor: OkRequestInterceptor) {
        builder.addRequestInterceptor(interceptor)
    }

    fun addResponseInterceptor(interceptor: OkResponseInterceptor) {
        builder.addResponseInterceptor(interceptor)
    }

    fun mapResponse(mapper: OkMapper<Response, T>) = this.apply {
        transformer.mapResponse(mapper)
        if (mapper is OkDownloadMapper) {
            builder.addRequestInterceptor(mapper)
        }
    }

    fun mapError(mapper: OkMapper<Exception, T>) = this.apply {
        transformer.mapError(mapper)
    }

    fun onStart(action: SimpleAction) = this.apply {
        onStartApplied = true
        onStartActions.add(action)
    }

    fun onSuccess(action: Action<T>) = this.apply {
        onSuccessApplied = true
        onSuccessActions.add(action)
    }

    fun onError(action: Action<Exception>) = this.apply {
        onErrorApplied = true
        onErrorActions.add(action)
    }

    fun onCancel(action: SimpleAction) = this.apply {
        onCancelApplied = true
        onCancelActions.add(action)
    }

    fun onComplete(action: SimpleAction) = this.apply {
        onCompleteApplied = true
        onCompleteActions.add(action)
    }

    fun cancel() {
        request?.cancel()
    }

    @Throws(Exception::class)
    fun execute(): T {
        return transformer.transformResponse(getRequest().execute())
    }

    fun safeExecute(): T? {
        return runCatching { execute() }.getOrNull()
    }

    fun safeExecute(defaultValue: () -> T): T {
        return safeExecute() ?: defaultValue()
    }

    fun enqueue() = this.apply {
        getRequest().enqueue(ResponseHandler(transformer, object : OkCallback<T> {
            override fun onStart() {
                if (onStartApplied) {
                    for (action in onStartActions) action()
                }
            }

            override fun onSuccess(result: T) {
                if (onSuccessApplied) {
                    for (action in onSuccessActions) action(result)
                }
            }

            override fun onError(error: Exception) {
                if (onErrorApplied) {
                    for (action in onErrorActions) action(error)
                }
            }

            override fun onComplete() {
                if (onCompleteApplied) {
                    for (action in onCompleteActions) action()
                }
            }

            override fun onCancel() {
                if (onCancelApplied) {
                    for (action in onCancelActions) action()
                }
            }
        }))
    }

    private fun getRequest(): OkRequest {
        return request ?: builder.build().also {
            request = it
        }
    }

    private class ResponseHandler<T>(
        private val transformer: OkTransformer<T>,
        private val callback: OkCallback<T>
    ) : OkCallback<Response> {
        override fun onStart() {
            OkCallbacks.onStart(callback)
        }

        override fun onSuccess(result: Response) {
            runCatching {
                transformer.transformResponse(result)
            }.onFailure {
                OkCallbacks.onError(callback, Exception(it))
            }.onSuccess {
                OkCallbacks.onSuccess(callback, it)
            }
        }

        override fun onError(error: Exception) {
            OkCallbacks.onError(callback, error)
        }

        override fun onComplete() {
            OkCallbacks.onComplete(callback)
        }

        override fun onCancel() {
            OkCallbacks.onCancel(callback)
        }
    }

    companion object {

        fun <T> get(block: (OkFaker<T>.() -> Unit)? = null): OkFaker<T> =
            OkFaker<T>(OkRequestMethod.GET).apply {
                block?.invoke(this)
            }

        fun <T> post(block: (OkFaker<T>.() -> Unit)? = null): OkFaker<T> =
            OkFaker<T>(OkRequestMethod.POST).apply {
                block?.invoke(this)
            }

        fun <T> delete(block: (OkFaker<T>.() -> Unit)? = null): OkFaker<T> =
            OkFaker<T>(OkRequestMethod.DELETE).apply {
                block?.invoke(this)
            }

        fun <T> put(block: (OkFaker<T>.() -> Unit)? = null): OkFaker<T> =
            OkFaker<T>(OkRequestMethod.PUT).apply {
                block?.invoke(this)
            }

        fun <T> head(block: (OkFaker<T>.() -> Unit)? = null): OkFaker<T> =
            OkFaker<T>(OkRequestMethod.HEAD).apply {
                block?.invoke(this)
            }

        fun <T> patch(block: (OkFaker<T>.() -> Unit)? = null): OkFaker<T> =
            OkFaker<T>(OkRequestMethod.PATCH).apply {
                block?.invoke(this)
            }

    }
}

typealias Action<T> = (T) -> Unit
typealias SimpleAction = () -> Unit

data class BodyPart(val body: RequestBody, val headers: Headers? = null)
data class BodyFormDataPart(val body: RequestBody, val filename: String? = null)
data class FileFormDataPart(val file: File, val contentType: MediaType? = null)

class RequestPairs<T>(
    pairs: Map<String, T> = mutableMapOf()
) : Iterable<Map.Entry<String, T>> {

    private val pairs: MutableMap<String, T> = pairs.toMutableMap()

    infix fun String.and(value: T) {
        pairs[this] = value
    }

    fun put(key: String, value: T) {
        pairs[key] = value
    }

    fun putAll(pairsFrom: Map<String, T>) {
        pairs.putAll(pairsFrom)
    }

    fun remove(key: String): T? {
        return pairs.remove(key)
    }

    override fun toString(): String {
        return pairs.toJSON()
    }

    override fun iterator(): Iterator<Map.Entry<String, T>> {
        return pairs.iterator()
    }
}

inline fun requestPairsOf(crossinline operation: RequestPairs<Any?>.() -> Unit): RequestPairs<Any?> {
    return RequestPairs<Any?>().apply(operation)
}

fun requestPairsOf(
    vararg pairs: Pair<String, Any?>,
    operation: (RequestPairs<Any?>.() -> Unit)? = null
): RequestPairs<Any?> {
    return RequestPairs<Any?>().apply { putAll(pairs.toMap()) }.also {
        operation?.invoke(it)
    }
}

fun requestPairsOf(
    copyFrom: Any,
    serializeNulls: Boolean = false,
    operation: (RequestPairs<Any?>.() -> Unit)? = null
): RequestPairs<Any?> {
    if (copyFrom is Map<*, *>) {
        return RequestPairs(copyFrom.mapKeys { it.key.toString() })
    }
    return RequestPairs<Any?>().apply {
        val source: String =
            if (copyFrom is RequestPairs<*>) copyFrom.toString() else copyFrom.toJSON()
        source.toJSONObject().forEach { key, element ->
            if (!element.isJSONNull || serializeNulls) {
                put(key, element.asString())
            }
        }
    }.also { operation?.invoke(it) }
}

fun <T : Any> OkFaker<T>.asFlow(): Flow<T> = flow {
    emit(execute())
}.flowOn(Dispatchers.IO)

fun <T : Any> OkFaker<T>.asLiveData(
    context: CoroutineContext = EmptyCoroutineContext,
    timeoutInMillis: Long = 5000L
): LiveData<T> = liveData(context, timeoutInMillis) {
    emit(execute())
}