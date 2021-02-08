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
import okhttp3.Headers.Companion.toHeaders
import java.io.File
import java.net.URI
import java.net.URL
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class OkFaker<T> internal constructor(
    private val request: OkRequest,
    private val transformer: OkTransformer<T>,
    private val onStartActions: List<SimpleAction>?,
    private val onSuccessActions: List<Action<T>>?,
    private val onErrorActions: List<Action<Exception>>?,
    private val onCompletionActions: List<SimpleAction>?,
    private val onCancelActions: List<SimpleAction>?
) {

    val isCanceled: Boolean
        get() = request.isCanceled

    val isExecuted: Boolean
        get() = request.isExecuted


    fun cancel() = request.cancel()

    @Throws(Exception::class)
    fun get(): T = transformer.transformResponse(request.execute())

    fun getOrNull(): T? = runCatching { get() }.getOrNull()

    fun getOrElse(defaultValue: () -> T): T = getOrNull() ?: defaultValue()

    fun start() = apply {
        request.enqueue(ResponseCallback(transformer, object : OkCallback<T> {
            override fun onStart() {
                if (onStartActions != null) {
                    for (action in onStartActions) action()
                }
            }

            override fun onSuccess(result: T) {
                if (onSuccessActions != null) {
                    for (action in onSuccessActions) action(result)
                }
            }

            override fun onFailure(error: Exception) {
                if (onErrorActions != null) {
                    for (action in onErrorActions) action(error)
                }
            }

            override fun onCompletion() {
                if (onCompletionActions != null) {
                    for (action in onCompletionActions) action()
                }
            }

            override fun onCancel() {
                if (onCancelActions != null) {
                    for (action in onCancelActions) action()
                }
            }
        }))
    }

    private class ResponseCallback<T>(
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
                OkCallbacks.onFailure(callback, Exception(it))
            }.onSuccess {
                OkCallbacks.onSuccess(callback, it)
            }
        }

        override fun onFailure(error: Exception) {
            OkCallbacks.onFailure(callback, error)
        }

        override fun onCompletion() {
            OkCallbacks.onCompletion(callback)
        }

        override fun onCancel() {
            OkCallbacks.onCancel(callback)
        }
    }

    class Builder<T>(method: OkRequestMethod) {
        private val builder = OkRequest.Builder(method)

        private var onStartApplied = false
        private val onStartActions: MutableList<SimpleAction> by lazy { mutableListOf() }

        private var onSuccessApplied = false
        private val onSuccessActions: MutableList<Action<T>> by lazy { mutableListOf() }

        private var onErrorApplied = false
        private val onErrorActions: MutableList<Action<Exception>> by lazy { mutableListOf() }

        private var onCompletionApplied = false
        private val onCompletionActions: MutableList<SimpleAction> by lazy { mutableListOf() }

        private var onCancelApplied = false
        private val onCancelActions: MutableList<SimpleAction> by lazy { mutableListOf() }

        private val transformer = OkTransformer<T>()

        fun client(client: OkHttpClient) = apply {
            builder.client(client)
        }

        fun url(url: String) = apply {
            builder.url(url)
        }

        fun url(url: URL) = apply {
            builder.url(url)
        }

        fun uri(uri: URI) = apply {
            builder.url(uri)
        }

        fun tag(tag: Any?) = apply {
            builder.tag(tag)
        }

        fun <T> tag(type: Class<in T>, tag: T?) = apply {
            builder.tag(type, tag)
        }

        fun cacheControl(cacheControl: CacheControl) = apply {
            builder.cacheControl(cacheControl)
        }

        fun headers(operation: RequestPairs<Any?>.() -> Unit) = apply {
            RequestPairs<Any?>().apply(operation).forEach {
                builder.header(it.key, it.value.toString())
            }
        }

        fun headers(headers: Map<String, Any?>) = apply {
            headers.forEach {
                builder.header(it.key, it.value.toString())
            }
        }

        fun headers(vararg headers: Pair<String, Any?>) = apply {
            headers.forEach {
                builder.header(it.first, it.second.toString())
            }
        }

        fun addHeaders(operation: RequestPairs<Any?>.() -> Unit) = apply {
            RequestPairs<Any?>().apply(operation).forEach {
                builder.addHeader(it.key, it.value.toString())
            }
        }

        fun addHeaders(headers: Map<String, Any?>) = apply {
            headers.forEach {
                builder.addHeader(it.key, it.value.toString())
            }
        }

        fun addHeaders(vararg headers: Pair<String, Any?>) = apply {
            headers.forEach {
                builder.addHeader(it.first, it.second.toString())
            }
        }

        fun username(username: String) = apply {
            builder.username(username)
        }

        fun password(password: String) = apply {
            builder.password(password)
        }

        fun queryParameters(operation: RequestPairs<Any?>.() -> Unit) = apply {
            RequestPairs<Any?>().apply(operation).forEach {
                builder.setQueryParameter(it.key, it.value.toString())
            }
        }

        fun queryParameters(queryParameters: Map<String, Any?>) = apply {
            queryParameters.forEach {
                builder.setQueryParameter(it.key, it.value.toString())
            }
        }

        fun queryParameters(vararg queryParameters: Pair<String, Any?>) = apply {
            queryParameters.forEach {
                builder.setQueryParameter(it.first, it.second.toString())
            }
        }

        fun addQueryParameters(operation: RequestPairs<Any?>.() -> Unit) = apply {
            RequestPairs<Any?>().apply(operation).forEach {
                builder.addQueryParameter(it.key, it.value.toString())
            }
        }

        fun addQueryParameters(queryParameters: Map<String, Any?>) = apply {
            queryParameters.forEach {
                builder.addQueryParameter(it.key, it.value.toString())
            }
        }

        fun addQueryParameters(vararg queryParameters: Pair<String, Any?>) = apply {
            queryParameters.forEach {
                builder.addQueryParameter(it.first, it.second.toString())
            }
        }

        fun encodedQueryParameters(operation: RequestPairs<Any?>.() -> Unit) = apply {
            RequestPairs<Any?>().apply(operation).forEach {
                builder.setEncodedQueryParameter(it.key, it.value.toString())
            }
        }

        fun encodedQueryParameters(encodedQueryParameters: Map<String, Any?>) = apply {
            encodedQueryParameters.forEach {
                builder.setEncodedQueryParameter(it.key, it.value.toString())
            }
        }

        fun encodedQueryParameters(vararg encodedQueryParameters: Pair<String, Any?>) = apply {
            encodedQueryParameters.forEach {
                builder.setEncodedQueryParameter(it.first, it.second.toString())
            }
        }

        fun addEncodedQueryParameters(operation: RequestPairs<Any?>.() -> Unit) = apply {
            RequestPairs<Any?>().apply(operation).forEach {
                builder.addEncodedQueryParameter(it.key, it.value.toString())
            }
        }

        fun addEncodedQueryParameters(encodedQueryParameters: Map<String, Any?>) = apply {
            encodedQueryParameters.forEach {
                builder.addEncodedQueryParameter(it.key, it.value.toString())
            }
        }

        fun addEncodedQueryParameters(vararg encodedQueryParameters: Pair<String, Any?>) = apply {
            encodedQueryParameters.forEach {
                builder.addEncodedQueryParameter(it.first, it.second.toString())
            }
        }

        fun formParameters(operation: RequestPairs<Any?>.() -> Unit) = apply {
            RequestPairs<Any?>().apply(operation).forEach {
                builder.addFormParameter(it.key, it.value.toString())
            }
        }

        fun formParameters(formParameters: Map<String, Any?>) = apply {
            formParameters.forEach {
                builder.addFormParameter(it.key, it.value.toString())
            }
        }

        fun formParameters(vararg formParameters: Pair<String, Any?>) = apply {
            formParameters.forEach {
                builder.addFormParameter(it.first, it.second.toString())
            }
        }

        fun encodedFormParameters(operation: RequestPairs<Any?>.() -> Unit) = apply {
            RequestPairs<Any?>().apply(operation).forEach {
                builder.addEncodedFormParameter(it.key, it.value.toString())
            }
        }

        fun encodedFormParameters(encodedFormParameters: Map<String, Any?>) = apply {
            encodedFormParameters.forEach {
                builder.addEncodedFormParameter(it.key, it.value.toString())
            }
        }

        fun encodedFormParameters(vararg encodedFormParameters: Pair<String, Any?>) = apply {
            encodedFormParameters.forEach {
                builder.addEncodedFormParameter(it.first, it.second.toString())
            }
        }

        fun formDataParts(operation: RequestPairs<Any?>.() -> Unit) = apply {
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

        fun formDataParts(formDataParts: Map<String, Any?>) = apply {
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

        fun formDataParts(vararg formDataParts: Pair<String, Any?>) = apply {
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

        fun parts(operation: MutableList<RequestBody>.() -> Unit) = apply {
            mutableListOf<RequestBody>().apply(operation).forEach {
                builder.addPart(it)
            }
        }

        fun parts(vararg parts: RequestBody) = apply {
            parts.forEach {
                builder.addPart(it)
            }
        }

        fun parts(vararg parts: BodyPart) = apply {
            parts.forEach {
                builder.addPart(it.headers, it.body)
            }
        }

        fun multiParts(operation: MutableList<MultipartBody.Part>.() -> Unit) = apply {
            mutableListOf<MultipartBody.Part>().apply(operation).forEach {
                builder.addPart(it)
            }
        }

        fun multiParts(vararg parts: MultipartBody.Part) = apply {
            parts.forEach {
                builder.addPart(it)
            }
        }

        fun body(body: RequestBody) = apply {
            builder.body(body)
        }

        fun body(body: () -> RequestBody) = apply {
            builder.body(body())
        }

        fun addRequestInterceptor(interceptor: OkRequestInterceptor) {
            builder.addRequestInterceptor(interceptor)
        }

        fun addResponseInterceptor(interceptor: OkResponseInterceptor) {
            builder.addResponseInterceptor(interceptor)
        }

        fun mapResponse(mapper: OkMapper<Response, T>) = apply {
            transformer.mapResponse(mapper)
            if (mapper is OkDownloadMapper) {
                builder.addRequestInterceptor(mapper)
            }
        }

        fun mapError(mapper: OkMapper<Exception, T>) = apply {
            transformer.mapError(mapper)
        }

        fun onStart(action: SimpleAction) = apply {
            onStartApplied = true
            onStartActions.add(action)
        }

        fun onSuccess(action: Action<T>) = apply {
            onSuccessApplied = true
            onSuccessActions.add(action)
        }

        fun onError(action: Action<Exception>) = apply {
            onErrorApplied = true
            onErrorActions.add(action)
        }

        fun onComplete(action: SimpleAction) = apply {
            onCompletionApplied = true
            onCompletionActions.add(action)
        }

        fun onCancel(action: SimpleAction) = apply {
            onCancelApplied = true
            onCancelActions.add(action)
        }

        fun build(): OkFaker<T> = OkFaker(
            builder.build(),
            transformer,
            if (onStartApplied) onStartActions else null,
            if (onSuccessApplied) onSuccessActions else null,
            if (onErrorApplied) onErrorActions else null,
            if (onCompletionApplied) onCompletionActions else null,
            if (onCancelApplied) onCancelActions else null
        )

        @Throws(Exception::class)
        fun get(): T = build().get()

        fun getOrNull(): T? = build().getOrNull()

        fun getOrElse(defaultValue: () -> T): T = build().getOrElse(defaultValue)

        fun start(): OkFaker<T> = build().start()

    }

    companion object {

        fun <T> get(block: (Builder<T>.() -> Unit)? = null): Builder<T> =
            Builder<T>(OkRequestMethod.GET).apply {
                block?.invoke(this)
            }

        fun <T> post(block: (Builder<T>.() -> Unit)? = null): Builder<T> =
            Builder<T>(OkRequestMethod.POST).apply {
                block?.invoke(this)
            }

        fun <T> delete(block: (Builder<T>.() -> Unit)? = null): Builder<T> =
            Builder<T>(OkRequestMethod.DELETE).apply {
                block?.invoke(this)
            }

        fun <T> put(block: (Builder<T>.() -> Unit)? = null): Builder<T> =
            Builder<T>(OkRequestMethod.PUT).apply {
                block?.invoke(this)
            }

        fun <T> head(block: (Builder<T>.() -> Unit)? = null): Builder<T> =
            Builder<T>(OkRequestMethod.HEAD).apply {
                block?.invoke(this)
            }

        fun <T> patch(block: (Builder<T>.() -> Unit)? = null): Builder<T> =
            Builder<T>(OkRequestMethod.PATCH).apply {
                block?.invoke(this)
            }

    }
}

typealias Action<T> = (T) -> Unit
typealias SimpleAction = () -> Unit

class BodyPart(val body: RequestBody, vararg headers: Pair<String, String>){
    val headers: Headers = headers.toMap().toHeaders()
}
class BodyFormDataPart(val body: RequestBody, val filename: String? = null)
class FileFormDataPart(val file: File, val contentType: MediaType? = null)

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
    emit(get())
}.flowOn(Dispatchers.IO)

fun <T : Any> OkFaker<T>.asLiveData(
    context: CoroutineContext = EmptyCoroutineContext,
    timeoutInMillis: Long = 5000L
): LiveData<T> = liveData(context, timeoutInMillis) {
    emit(get())
}