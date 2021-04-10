@file:Suppress("unused")

package com.nice.kotlins.http

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.nice.kotlins.helper.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.*
import java.io.File
import java.io.IOException
import kotlin.collections.set
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class OkFaker<T> internal constructor(
    private val request: OkRequest,
    private val transformer: OkTransformer<T>,
    private val onStartActions: List<SimpleAction>?,
    private val onSuccessActions: List<Action<T>>?,
    private val onErrorActions: List<Action<Throwable>>?,
    private val onCompletionActions: List<SimpleAction>?,
    private val onCancelActions: List<SimpleAction>?
) {

    val isCanceled: Boolean
        get() = request.isCanceled

    val isExecuted: Boolean
        get() = request.isExecuted

    fun tag(): Any? = request.tag()

    fun <T> tag(type: Class<out T>): T? = request.tag(type)

    fun cancel() = request.cancel()

    @Throws(IOException::class)
    fun get(): T = transformer.transformResponse(request.execute())

    fun getOrNull(): T? = runCatching { get() }.getOrNull()

    fun getOrElse(defaultValue: () -> T): T = getOrNull() ?: defaultValue()

    fun start() = apply {
        request.enqueue(OkCalbackWrapper(transformer, object : OkCallback<T> {
            override fun onStart() {
                onStartActions?.forEach { action -> action.onAction()}
            }

            override fun onSuccess(result: T) {
                onSuccessActions?.forEach { action -> action.onAction(result) }
            }

            override fun onFailure(error: Throwable) {
                onErrorActions?.forEach { action -> action.onAction(error) }
            }

            override fun onCompletion() {
                onCompletionActions?.forEach { action -> action.onAction() }
            }

            override fun onCancel() {
                onCancelActions?.forEach { action -> action.onAction() }
            }
        }))
    }

    private class OkCalbackWrapper<T>(
        private val transformer: OkTransformer<T>,
        private val callback: OkCallback<T>
    ) : OkCallback<Response> {
        override fun onStart() {
            OkCallbacks.onStart(callback)
        }

        override fun onSuccess(result: Response) {
            OkCallbacks.onSuccess(callback) { transformer.transformResponse(result) }
        }

        override fun onFailure(error: Throwable) {
            OkCallbacks.onFailure(callback) { error }
        }

        override fun onCompletion() {
            OkCallbacks.onCompletion(callback)
        }

        override fun onCancel() {
            OkCallbacks.onCancel(callback)
        }
    }

    class Builder<T> internal constructor(method: OkRequestMethod, config: OkConfig) {
        private val builder = OkRequest.Builder(method, config)

        private var onStartApplied = false
        private val onStartActions: MutableList<SimpleAction> by lazy { mutableListOf() }

        private var onSuccessApplied = false
        private val onSuccessActions: MutableList<Action<T>> by lazy { mutableListOf() }

        private var onErrorApplied = false
        private val onErrorActions: MutableList<Action<Throwable>> by lazy { mutableListOf() }

        private var onCompletionApplied = false
        private val onCompletionActions: MutableList<SimpleAction> by lazy { mutableListOf() }

        private var onCancelApplied = false
        private val onCancelActions: MutableList<SimpleAction> by lazy { mutableListOf() }

        private val transformer = OkTransformer<T>()

        fun client(client: OkHttpClient) = apply {
            builder.client(client)
        }

        fun client(client: () -> OkHttpClient) = apply {
            builder.client(client())
        }

        fun url(url: String) = apply {
            builder.url(url)
        }

        fun url(url: () -> String) = apply {
            builder.url(url())
        }

        fun cacheControl(cacheControl: CacheControl) = apply {
            builder.cacheControl(cacheControl)
        }

        fun cacheControl(cacheControl: () -> CacheControl) = apply {
            builder.cacheControl(cacheControl())
        }

        fun username(username: String) = apply {
            builder.username(username)
        }

        fun username(username: () -> String) = apply {
            builder.username(username())
        }

        fun password(password: String) = apply {
            builder.password(password)
        }

        fun password(password: () -> String) = apply {
            builder.password(password())
        }

        fun headers(operation: RequestPairs<String, Any?>.() -> Unit) = apply {
            requestPairsOf(operation).forEach {
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

        fun addHeaders(operation: RequestPairs<String, Any?>.() -> Unit) = apply {
            requestPairsOf(operation).forEach {
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

        fun removeHeaders(name: String) = apply {
            builder.removeHeaders(name)
        }

        fun queryParameters(operation: RequestPairs<String, Any?>.() -> Unit) = apply {
            requestPairsOf(operation).forEach {
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

        fun addQueryParameters(operation: RequestPairs<String, Any?>.() -> Unit) = apply {
            requestPairsOf(operation).forEach {
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

        fun encodedQueryParameters(operation: RequestPairs<String, Any?>.() -> Unit) = apply {
            requestPairsOf(operation).forEach {
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

        fun addEncodedQueryParameters(operation: RequestPairs<String, Any?>.() -> Unit) = apply {
            requestPairsOf(operation).forEach {
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

        fun removeQueryParameters(name: String) = apply {
            builder.removeQueryParameters(name)
        }

        fun removeEncodedQueryParameters(name: String) = apply {
            builder.removeEncodedQueryParameters(name)
        }

        fun formParameters(operation: RequestPairs<String, Any?>.() -> Unit) = apply {
            requestPairsOf(operation).forEach {
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

        fun encodedFormParameters(operation: RequestPairs<String, Any?>.() -> Unit) = apply {
            requestPairsOf(operation).forEach {
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

        fun formDataParts(operation: RequestPairs<String, Any?>.() -> Unit) = apply {
            requestPairsOf(operation).forEach {
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

        fun multiparts(operation: MutableList<MultipartBody.Part>.() -> Unit) = apply {
            mutableListOf<MultipartBody.Part>().apply(operation).forEach {
                builder.addPart(it)
            }
        }

        fun multiparts(vararg parts: MultipartBody.Part) = apply {
            parts.forEach {
                builder.addPart(it)
            }
        }

        fun stringBody(contentType: MediaType? = null, body: String) = apply {
            builder.stringBody(contentType, body)
        }

        fun stringBody(contentType: MediaType? = null, body: () -> String) = apply {
            builder.stringBody(contentType, body())
        }

        fun fileBody(contentType: MediaType? = null, file: File) = apply {
            builder.fileBody(contentType, file)
        }

        fun fileBody(contentType: MediaType? = null, file: () -> File) = apply {
            builder.fileBody(contentType, file())
        }

        fun requestBody(body: RequestBody) = apply {
            builder.requestBody(body)
        }

        fun requestBody(body: () -> RequestBody) = apply {
            builder.requestBody(body())
        }

        fun tag(tag: Any?) = apply {
            builder.tag(tag)
        }

        fun <T> tag(type: Class<in T>, tag: T?) = apply {
            builder.tag(type, tag)
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
                builder.addRequestInterceptor(mapper.requestInterceptor)
            }
        }

        fun mapError(mapper: OkMapper<Throwable, T>) = apply {
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

        fun onError(action: Action<Throwable>) = apply {
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

        @Throws(IOException::class)
        fun get(): T = build().get()

        fun getOrNull(): T? = build().getOrNull()

        fun getOrElse(defaultValue: () -> T): T = build().getOrElse(defaultValue)

        fun start(): OkFaker<T> = build().start()

    }

    companion object {

        private val CONFIG = OkConfig()

        @JvmStatic
        fun configSetter(): OkConfig.Setter = CONFIG.setter()

        @JvmStatic
        @JvmOverloads
        @JvmName("httpGet")
        fun <T> get(block: Builder<T>.() -> Unit = {}): Builder<T> =
            Builder<T>(OkRequestMethod.GET, CONFIG).apply(block)

        @JvmStatic
        @JvmOverloads
        @JvmName("httpPost")
        fun <T> post(block: Builder<T>.() -> Unit = {}): Builder<T> =
            Builder<T>(OkRequestMethod.POST, CONFIG).apply(block)

        @JvmStatic
        @JvmOverloads
        @JvmName("httpDelete")
        fun <T> delete(block: Builder<T>.() -> Unit = {}): Builder<T> =
            Builder<T>(OkRequestMethod.DELETE, CONFIG).apply(block)

        @JvmStatic
        @JvmOverloads
        @JvmName("httpPut")
        fun <T> put(block: Builder<T>.() -> Unit = {}): Builder<T> =
            Builder<T>(OkRequestMethod.PUT, CONFIG).apply(block)

        @JvmStatic
        @JvmOverloads
        @JvmName("httpHead")
        fun <T> head(block: Builder<T>.() -> Unit = {}): Builder<T> =
            Builder<T>(OkRequestMethod.HEAD, CONFIG).apply(block)

        @JvmStatic
        @JvmOverloads
        @JvmName("httpPatch")
        fun <T> patch(block: Builder<T>.() -> Unit = {}): Builder<T> =
            Builder<T>(OkRequestMethod.PATCH, CONFIG).apply(block)

    }

}

fun interface Action<T> {
    fun onAction(value: T)
}

fun interface SimpleAction {
    fun onAction()
}

class BodyFormDataPart(val body: RequestBody, val filename: String? = null)
class FileFormDataPart(val file: File, val contentType: MediaType? = null)

class RequestPairs<K, V>(
    pairs: Map<K, V> = mutableMapOf()
) : Iterable<Map.Entry<K, V>> {

    private val pairs: MutableMap<K, V> = pairs.toMutableMap()

    infix fun K.and(value: V) {
        pairs[this] = value
    }

    fun put(key: K, value: V): V? {
        return pairs.put(key, value)
    }

    fun putAll(pairsForm: RequestPairs<out K, V>) {
        pairs.putAll(pairsForm.pairs)
    }

    fun remove(key: K): V? {
        return pairs.remove(key)
    }

    override fun toString(): String {
        return pairs.toJson()
    }

    override fun iterator(): Iterator<Map.Entry<K, V>> {
        return pairs.iterator()
    }

}

fun <K, V> RequestPairs<K, V>.putAll(pairsFrom: Map<out K, V>) = putAll(RequestPairs(pairsFrom))

fun <K, V> RequestPairs<K, V>.putAll(vararg pairsFrom: Pair<K, V>) = putAll(pairsFrom.toMap())

fun <K, V> RequestPairs<K, V>.toMap(): Map<K, V> = map { it.key to it.value }.toMap()

fun <K, V, M : MutableMap<in K, in V>> RequestPairs<K, V>.toMap(destination: M): M =
    map { it.key to it.value }.toMap(destination)

inline fun requestPairsOf(crossinline operation: RequestPairs<String, Any?>.() -> Unit): RequestPairs<String, Any?> {
    return RequestPairs<String, Any?>().apply(operation)
}

fun requestPairsOf(
    vararg pairs: Pair<String, Any?>,
    operation: (RequestPairs<String, Any?>.() -> Unit)? = null
): RequestPairs<String, Any?> {
    return RequestPairs(pairs.toMap()).also {
        operation?.invoke(it)
    }
}

fun requestPairsOf(
    copyFrom: Any,
    operation: (RequestPairs<String, Any?>.() -> Unit)? = null
): RequestPairs<String, Any?> {
    return RequestPairs<String, Any?>().apply {
        if (copyFrom is RequestPairs<*, *>) {
            for ((key, value) in copyFrom) {
                if (key == null) continue
                put(key.toString(), value)
            }
        } else {
            copyFrom.toJsonObject().forEach { key, value ->
                when {
                    value.isJsonPrimitive -> put(key, value.asString)
                    value.isJsonNull -> put(key, null)
                }
            }
        }
    }.also { operation?.invoke(it) }
}

fun <T : Any> OkFaker<T>.asFlow(): Flow<T> = flow {
    emit(suspendExecutor(CoroutineExecutors.IO) { get() })
}

fun <T : Any> OkFaker<T>.asLiveData(
    context: CoroutineContext = EmptyCoroutineContext,
    timeoutInMillis: Long = 5000L
): LiveData<T> = liveData(context, timeoutInMillis) {
    emit(suspendExecutor(CoroutineExecutors.IO) { get() })
}