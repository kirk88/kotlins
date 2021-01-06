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

open class OkFaker<T>(method: OkRequestMethod) {

    internal val request: OkRequest<T> = OkRequest(method)

    private var onStartApplied = false
    private val startActions: MutableList<SimpleAction> by lazy { mutableListOf() }

    private var onSuccessApplied = false
    private val successActions: MutableList<Action<T>> by lazy { mutableListOf() }

    private var onErrorApplied = false
    private val errorActions: MutableList<Action<Exception>> by lazy { mutableListOf() }

    private var onCancelApplied = false
    private val cancelActions: MutableList<SimpleAction> by lazy { mutableListOf() }

    private var onCompleteApplied = false
    private val completeActions: MutableList<SimpleAction> by lazy { mutableListOf() }

    val isCanceled: Boolean
        get() = request.isCanceled

    val isExecuted: Boolean
        get() = request.isExecuted

    val tag: Any?
        get() = request.tag

    fun client(client: () -> OkHttpClient): OkFaker<T> = this.apply {
        request.client(client())
    }

    fun client(client: OkHttpClient): OkFaker<T> = this.apply {
        request.client(client)
    }

    fun url(url: () -> String): OkFaker<T> = this.apply {
        request.url(url())
    }

    fun url(url: String): OkFaker<T> = this.apply {
        request.url(url)
    }

    fun tag(tag: () -> Any): OkFaker<T> = this.apply {
        request.tag(tag())
    }

    fun tag(tag: Any): OkFaker<T> = this.apply {
        request.tag(tag)
    }

    fun cacheControl(cacheControl: () -> CacheControl): OkFaker<T> = this.apply {
        request.cacheControl(cacheControl())
    }

    fun cacheControl(cacheControl: CacheControl): OkFaker<T> = this.apply {
        request.cacheControl(cacheControl)
    }

    fun extension(extension: OkExtension) = this.apply {
        request.extension(extension)
    }

    fun extension(extension: () -> OkExtension) = this.apply {
        request.extension(extension())
    }

    fun headers(action: RequestPairs<Any?>.() -> Unit): OkFaker<T> = this.apply {
        RequestPairs<Any?>().apply(action).forEach {
            request.setHeader(it.key, it.value.toString())
        }
    }

    fun headers(headers: Map<String, Any?>): OkFaker<T> = this.apply {
        headers.forEach {
            request.setHeader(it.key, it.value.toString())
        }
    }

    fun headers(vararg headers: Pair<String, Any?>): OkFaker<T> = this.apply {
        headers.forEach {
            request.setHeader(it.first, it.second.toString())
        }
    }

    fun queryParameters(action: RequestPairs<Any?>.() -> Unit): OkFaker<T> = this.apply {
        RequestPairs<Any?>().apply(action).forEach {
            request.addQueryParameter(it.key, it.value.toString())
        }
    }

    fun queryParameters(queryParameters: Map<String, Any?>): OkFaker<T> = this.apply {
        queryParameters.forEach {
            request.addQueryParameter(it.key, it.value.toString())
        }
    }

    fun queryParameters(vararg queryParameters: Pair<String, Any?>): OkFaker<T> = this.apply {
        queryParameters.forEach {
            request.addQueryParameter(it.first, it.second.toString())
        }
    }

    fun encodedQueryParameters(action: RequestPairs<Any?>.() -> Unit): OkFaker<T> = this.apply {
        RequestPairs<Any?>().apply(action).forEach {
            request.addEncodedQueryParameter(it.key, it.value.toString())
        }
    }

    fun encodedQueryParameters(encodedQueryParameters: Map<String, Any?>): OkFaker<T> = this.apply {
        encodedQueryParameters.forEach {
            request.addEncodedQueryParameter(it.key, it.value.toString())
        }
    }

    fun encodedQueryParameters(vararg encodedQueryParameters: Pair<String, Any?>): OkFaker<T> =
        this.apply {
            encodedQueryParameters.forEach {
                request.addEncodedQueryParameter(it.first, it.second.toString())
            }
        }

    fun formParameters(action: RequestPairs<Any?>.() -> Unit): OkFaker<T> = this.apply {
        RequestPairs<Any?>().apply(action).forEach {
            request.addFormParameter(it.key, it.value.toString())
        }
    }

    fun formParameters(formParameters: Map<String, Any?>): OkFaker<T> = this.apply {
        formParameters.forEach {
            request.addFormParameter(it.key, it.value.toString())
        }
    }

    fun formParameters(vararg formParameters: Pair<String, Any?>): OkFaker<T> = this.apply {
        formParameters.forEach {
            request.addFormParameter(it.first, it.second.toString())
        }
    }

    fun encodedFormParameters(action: RequestPairs<Any?>.() -> Unit): OkFaker<T> = this.apply {
        RequestPairs<Any?>().apply(action).forEach {
            request.addEncodedFormParameter(it.key, it.value.toString())
        }
    }

    fun encodedFormParameters(encodedFormParameters: Map<String, Any?>): OkFaker<T> = this.apply {
        encodedFormParameters.forEach {
            request.addEncodedFormParameter(it.key, it.value.toString())
        }
    }

    fun encodedFormParameters(vararg encodedFormParameters: Pair<String, Any?>): OkFaker<T> =
        this.apply {
            encodedFormParameters.forEach {
                request.addEncodedFormParameter(it.first, it.second.toString())
            }
        }

    fun formDataParts(action: RequestPairs<Any?>.() -> Unit): OkFaker<T> = this.apply {
        RequestPairs<Any?>().apply(action).forEach {
            request.addFormDataPart(it.key, it.value.toString())
        }
    }

    fun formDataParts(formDataParts: Map<String, Any?>): OkFaker<T> = this.apply {
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

    fun formDataParts(vararg formDataParts: Pair<String, Any?>): OkFaker<T> = this.apply {
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

    fun parts(action: ArrayList<RequestBody>.() -> Unit): OkFaker<T> = this.apply {
        arrayListOf<RequestBody>().apply(action).forEach {
            request.addPart(it)
        }
    }

    fun multiParts(action: ArrayList<MultipartBody.Part>.() -> Unit): OkFaker<T> = this.apply {
        arrayListOf<MultipartBody.Part>().apply(action).forEach {
            request.addPart(it)
        }
    }

    fun body(body: RequestBody): OkFaker<T> = this.apply {
        request.body(body)
    }

    fun body(body: () -> RequestBody): OkFaker<T> = this.apply {
        request.body(body())
    }

    fun mapResponse(transform: (response: Response) -> T): OkFaker<T> = this.apply {
        request.mapResponse { value ->
            OkResult.Success(transform(value))
        }
    }

    fun mapError(transform: (error: Exception) -> T): OkFaker<T> = this.apply {
        request.mapError { value ->
            OkResult.Success(transform(value))
        }
    }

    fun onStart(action: SimpleAction): OkFaker<T> = this.apply {
        onStartApplied = true
        startActions.add(action)
    }

    fun onSuccess(action: Action<T>): OkFaker<T> = this.apply {
        onSuccessApplied = true
        successActions.add(action)
    }

    fun onError(action: Action<Exception>): OkFaker<T> = this.apply {
        onErrorApplied = true
        errorActions.add(action)
    }

    fun onCancel(action: SimpleAction): OkFaker<T> = this.apply {
        onCancelApplied = true
        cancelActions.add(action)
    }

    fun onComplete(action: SimpleAction): OkFaker<T> = this.apply {
        onCompleteApplied = true
        completeActions.add(action)
    }

    fun cancel() {
        request.cancel()
    }

    @Throws(Exception::class)
    open fun execute(): T {
        return request.execute()
    }

    open fun safeExecute(): T? {
        return request.safeExecute()
    }

    open fun enqueue(): OkFaker<T> = this.apply {
        request.enqueue(object : OkCallback<T> {
            override fun onStart() {
                dispatchStartAction()
            }

            override fun onSuccess(result: T) {
                dispatchSuccessAction(result)
            }

            override fun onError(error: Exception) {
                dispatchErrorAction(error)
            }

            override fun onCancel() {
                dispatchCancelAction()
            }

            override fun onComplete() {
                dispatchCompleteAction()
            }
        })
    }

    protected fun dispatchStartAction() {
        if (!onStartApplied) return

        for (action in startActions) {
            action()
        }
    }

    protected fun dispatchSuccessAction(result: T) {
        if (!onSuccessApplied) return

        for (action in successActions) {
            action(result)
        }
    }

    protected fun dispatchErrorAction(error: Exception) {
        if (!onErrorApplied) return

        for (action in errorActions) {
            action(error)
        }
    }

    protected fun dispatchCancelAction() {
        if (!onCancelApplied) return

        for (action in cancelActions) {
            action()
        }
    }

    protected fun dispatchCompleteAction() {
        if (!onCompleteApplied) return

        for (action in completeActions) {
            action()
        }
    }

    companion object {

        fun <T> get(action: OkFaker<T>.() -> Unit): OkFaker<T> =
            OkFaker<T>(OkRequestMethod.GET).apply(action)

        fun <T> post(action: OkFaker<T>.() -> Unit): OkFaker<T> =
            OkFaker<T>(OkRequestMethod.POST).apply(action)

        fun <T> delete(action: OkFaker<T>.() -> Unit): OkFaker<T> =
            OkFaker<T>(OkRequestMethod.DELETE).apply(action)

        fun <T> put(action: OkFaker<T>.() -> Unit): OkFaker<T> =
            OkFaker<T>(OkRequestMethod.PUT).apply(action)

        fun <T> head(action: OkFaker<T>.() -> Unit): OkFaker<T> =
            OkFaker<T>(OkRequestMethod.HEAD).apply(action)

        fun <T> patch(action: OkFaker<T>.() -> Unit): OkFaker<T> =
            OkFaker<T>(OkRequestMethod.PATCH).apply(action)
    }

}

typealias Action<T> = (T) -> Unit
typealias SimpleAction = () -> Unit

data class BodyFromDataPart(val body: RequestBody, val filename: String? = null)
data class FileFormDataPart(val file: File, val type: MediaType? = null)

class RequestPairs<T> : Iterable<Map.Entry<String, T>> {

    private val pairs: MutableMap<String, T> = mutableMapOf()

    infix fun String.of(value: T) {
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
                element.isJsonNull && serializeNulls -> put(key, element.toString())
                element.isJsonArray || element.isJsonObject -> put(key, element.toString())
                element.isJsonPrimitive -> put(key, element.asString)
            }
        }
    }.apply(action)
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