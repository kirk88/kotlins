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
import java.lang.reflect.Type
import kotlin.collections.set
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class OkFaker<T> private constructor(
    private val request: OkRequest,
    private val transformer: OkTransformer<T>,
    private val onStartActions: List<SimpleAction>?,
    private val onSuccessActions: List<Action<T>>?,
    private val onErrorActions: List<Action<Throwable>>?,
    private val onCompletionActions: List<SimpleAction>?,
    private val onCancelActions: List<SimpleAction>?,
) {

    val isCanceled: Boolean
        get() = request.isCanceled

    val isExecuted: Boolean
        get() = request.isExecuted

    fun tag(): Any? = request.tag()

    fun <T> tag(type: Class<out T>): T? = request.tag(type)

    fun cancel() = request.cancel()

    @Throws(IOException::class)
    fun execute(): T = transformer.transformResponse(request.execute())

    fun executeOrElse(onError: (Throwable) -> T): T = runCatching { execute() }.getOrElse(onError)

    fun executeOrNull(): T? = runCatching { execute() }.getOrNull()

    fun enqueue() = apply {
        request.enqueue(OkCallbackWrapper(transformer, object : OkCallback<T> {
            override fun onStart() {
                onStartActions?.forEach { action -> action.onAction() }
            }

            override fun onSuccess(result: T) {
                onSuccessActions?.forEach { action -> action.onAction(result) }
            }

            override fun onError(error: Throwable) {
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

    private class OkCallbackWrapper<T>(
        private val transformer: OkTransformer<T>,
        private val callback: OkCallback<T>,
    ) : OkCallback<Response> {
        override fun onStart() {
            OkCallbacks.onStart(callback)
        }

        override fun onSuccess(result: Response) {
            OkCallbacks.onSuccess(callback) { transformer.transformResponse(result) }
        }

        override fun onError(error: Throwable) {
            OkCallbacks.onError(callback) { error }
        }

        override fun onCompletion() {
            OkCallbacks.onCompletion(callback)
        }

        override fun onCancel() {
            OkCallbacks.onCancel(callback)
        }
    }

    class Builder<T> @JvmOverloads constructor(
        method: OkRequestMethod,
        private val config: OkConfig? = null,
    ) {
        private val builder = OkRequest.Builder(method)

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

        init {
            val client = config?.client
            if (client != null) {
                client(client)
            }

            val cacheControl = config?.cacheControl
            if (cacheControl != null) {
                cacheControl(cacheControl)
            }

            val username = config?.username
            if (!username.isNullOrEmpty()) {
                username(username)
                password(config?.password.orEmpty())
            }

            val headers = config?.headers
            if (headers != null) {
                headers(headers)
            }

            val queryParameters = config?.queryParameters
            if (queryParameters != null) {
                queryParameters(queryParameters)
            }

            val formParameters = config?.formParameters
            if (formParameters != null) {
                formParameters(formParameters)
            }
        }

        fun client(client: OkHttpClient) = apply {
            builder.client(client)
        }

        fun client(client: () -> OkHttpClient) = apply {
            builder.client(client())
        }

        fun url(url: String) = apply {
            builder.url(config?.baseUrl, url)
        }

        fun url(url: () -> String) = apply {
            builder.url(config?.baseUrl, url())
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

        fun headers(headers: Map<String, Any?>) = apply {
            headers.forEach {
                builder.header(it.key, it.value.toString())
            }
        }

        fun headers(vararg headers: Pair<String, Any?>) = headers(mapOf(*headers))

        fun headers(operation: RequestPairs<String, Any?>.() -> Unit) =
            headers(requestPairsOf(operation).toMap())

        fun addHeaders(headers: Map<String, Any?>) = apply {
            headers.forEach {
                builder.addHeader(it.key, it.value.toString())
            }
        }

        fun addHeaders(vararg headers: Pair<String, Any?>) = addHeaders(mapOf(*headers))

        fun addHeaders(operation: RequestPairs<String, Any?>.() -> Unit) =
            addHeaders(requestPairsOf(operation).toMap())

        fun removeHeaders(name: String) = apply {
            builder.removeHeaders(name)
        }

        fun queryParameters(queryParameters: Map<String, Any?>) = apply {
            queryParameters.forEach {
                builder.setQueryParameter(it.key, it.value.toString())
            }
        }

        fun queryParameters(vararg queryParameters: Pair<String, Any?>) =
            queryParameters(mapOf(*queryParameters))

        fun queryParameters(operation: RequestPairs<String, Any?>.() -> Unit) =
            queryParameters(requestPairsOf(operation).toMap())

        fun addQueryParameters(queryParameters: Map<String, Any?>) = apply {
            queryParameters.forEach {
                builder.addQueryParameter(it.key, it.value.toString())
            }
        }

        fun addQueryParameters(vararg queryParameters: Pair<String, Any?>) =
            addQueryParameters(mapOf(*queryParameters))

        fun addQueryParameters(operation: RequestPairs<String, Any?>.() -> Unit) =
            addQueryParameters(requestPairsOf(operation).toMap())

        fun encodedQueryParameters(encodedQueryParameters: Map<String, Any?>) = apply {
            encodedQueryParameters.forEach {
                builder.setEncodedQueryParameter(it.key, it.value.toString())
            }
        }

        fun encodedQueryParameters(vararg encodedQueryParameters: Pair<String, Any?>) =
            encodedQueryParameters(mapOf(*encodedQueryParameters))

        fun encodedQueryParameters(operation: RequestPairs<String, Any?>.() -> Unit) =
            encodedQueryParameters(requestPairsOf(operation).toMap())

        fun addEncodedQueryParameters(encodedQueryParameters: Map<String, Any?>) = apply {
            encodedQueryParameters.forEach {
                builder.addEncodedQueryParameter(it.key, it.value.toString())
            }
        }

        fun addEncodedQueryParameters(vararg encodedQueryParameters: Pair<String, Any?>) =
            addEncodedQueryParameters(mapOf(*encodedQueryParameters))

        fun addEncodedQueryParameters(operation: RequestPairs<String, Any?>.() -> Unit) =
            addEncodedQueryParameters(requestPairsOf(operation).toMap())

        fun removeQueryParameters(name: String) = apply {
            builder.removeQueryParameters(name)
        }

        fun removeEncodedQueryParameters(name: String) = apply {
            builder.removeEncodedQueryParameters(name)
        }

        fun formParameters(formParameters: Map<String, Any?>) = apply {
            formParameters.forEach {
                builder.addFormParameter(it.key, it.value.toString())
            }
        }

        fun formParameters(vararg formParameters: Pair<String, Any?>) =
            formParameters(mapOf(*formParameters))

        fun formParameters(operation: RequestPairs<String, Any?>.() -> Unit) =
            formParameters(requestPairsOf(operation).toMap())

        fun encodedFormParameters(encodedFormParameters: Map<String, Any?>) = apply {
            encodedFormParameters.forEach {
                builder.addEncodedFormParameter(it.key, it.value.toString())
            }
        }

        fun encodedFormParameters(vararg encodedFormParameters: Pair<String, Any?>) =
            encodedFormParameters(mapOf(*encodedFormParameters))

        fun encodedFormParameters(operation: RequestPairs<String, Any?>.() -> Unit) =
            encodedFormParameters(requestPairsOf(operation).toMap())

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

        fun formDataParts(vararg formDataParts: Pair<String, Any?>) =
            formDataParts(mapOf(*formDataParts))

        fun formDataParts(operation: RequestPairs<String, Any?>.() -> Unit) =
            formDataParts(requestPairsOf(operation).toMap())

        fun parts(bodies: Collection<RequestBody>) = apply {
            bodies.forEach {
                builder.addPart(it)
            }
        }

        fun parts(vararg bodies: RequestBody) = apply {
            bodies.forEach {
                builder.addPart(it)
            }
        }

        fun multiparts(multiparts: Collection<MultipartBody.Part>) = apply {
            multiparts.forEach {
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

        fun addRequestInterceptor(interceptor: OkRequestInterceptor) = apply {
            builder.addRequestInterceptor(interceptor)
        }

        fun addResponseInterceptor(interceptor: OkResponseInterceptor) = apply {
            builder.addResponseInterceptor(interceptor)
        }

        fun mapResponse(mapper: OkMapper<Response, T>) = apply {
            if (mapper is OkDownloadMapper) {
                builder.addRequestInterceptor(mapper.requestInterceptor)
                builder.addResponseInterceptor(mapper.responseInterceptor)
            }
            transformer.mapResponse(mapper)
        }

        fun mapResponse(clazz: Class<T>) = apply {
            transformer.mapResponse(clazz)
        }

        fun mapResponse(type: Type) = apply {
            transformer.mapResponse(type)
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
        fun execute(): T = build().execute()

        fun executeOrElse(onError: (Throwable) -> T): T = build().executeOrElse(onError)

        fun executeOrNull(): T? = build().executeOrNull()

        fun enqueue(): OkFaker<T> = build().enqueue()

    }

    companion object {

        @PublishedApi
        internal val CONFIG = OkConfig()

        @JvmStatic
        fun configSetter(): OkConfig.Setter = CONFIG.newSetter()

        @JvmStatic
        fun <T> with(method: OkRequestMethod, block: Builder<T>.() -> Unit = {}): Builder<T> =
            Builder<T>(method, CONFIG).apply(block)

        @JvmStatic
        inline fun <reified T> get(block: Builder<T>.() -> Unit = {}): Builder<T> =
            Builder<T>(OkRequestMethod.GET, CONFIG).mapResponse(T::class.java).apply(block)

        @JvmStatic
        inline fun <reified T> post(block: Builder<T>.() -> Unit = {}): Builder<T> =
            Builder<T>(OkRequestMethod.POST, CONFIG).mapResponse(T::class.java).apply(block)

        @JvmStatic
        inline fun <reified T> delete(block: Builder<T>.() -> Unit = {}): Builder<T> =
            Builder<T>(OkRequestMethod.DELETE, CONFIG).mapResponse(T::class.java).apply(block)

        @JvmStatic
        inline fun <reified T> put(block: Builder<T>.() -> Unit = {}): Builder<T> =
            Builder<T>(OkRequestMethod.PUT, CONFIG).mapResponse(T::class.java).apply(block)

        @JvmStatic
        inline fun <reified T> head(block: Builder<T>.() -> Unit = {}): Builder<T> =
            Builder<T>(OkRequestMethod.HEAD, CONFIG).mapResponse(T::class.java).apply(block)

        @JvmStatic
        inline fun <reified T> patch(block: Builder<T>.() -> Unit = {}): Builder<T> =
            Builder<T>(OkRequestMethod.PATCH, CONFIG).mapResponse(T::class.java).apply(block)

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
    pairs: Map<K, V> = mutableMapOf(),
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
    operation: (RequestPairs<String, Any?>.() -> Unit)? = null,
): RequestPairs<String, Any?> {
    return RequestPairs(pairs.toMap()).also {
        operation?.invoke(it)
    }
}

fun requestPairsOf(
    copyFrom: Any,
    operation: (RequestPairs<String, Any?>.() -> Unit)? = null,
): RequestPairs<String, Any?> {
    return RequestPairs<String, Any?>().apply {
        if (copyFrom is RequestPairs<*, *>) {
            for ((key, value) in copyFrom) {
                put(key.toString(), value)
            }
        } else {
            for ((key, value) in copyFrom.toJsonObject()) {
                when {
                    value.isJsonPrimitive -> put(key, value.asString)
                    value.isJsonNull -> put(key, null)
                }
            }
        }
    }.also { operation?.invoke(it) }
}

fun <T : Any> OkFaker<T>.asFlow(): Flow<T> = flow {
    emit(suspendBlocking(CoroutineExecutors.IO) { execute() })
}

fun <T : Any> OkFaker<T>.asLiveData(
    context: CoroutineContext = EmptyCoroutineContext,
    timeoutInMillis: Long = 5000L,
): LiveData<T> = liveData(context, timeoutInMillis) {
    emit(suspendBlocking(CoroutineExecutors.IO) { execute() })
}

fun main() {
    val result =
        OkFaker.get<String>().client(OkHttpClient()).url("https://www.baidu.com/").executeOrNull()
    println(result)
}