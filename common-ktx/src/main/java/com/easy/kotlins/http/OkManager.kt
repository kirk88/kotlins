package com.easy.kotlins.http

import androidx.annotation.MainThread
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Response
import java.net.URI
import java.net.URL

typealias Action<T> = (T) -> Unit
typealias SimpleAction = () -> Unit
typealias ProgressAction = (bytes: Long, totalBytes: Long) -> Unit

abstract class OkManager<T, R : OkRequest<T>>(protected val request: R) {

    private var onStartApplied = false
    private val startActions: MutableList<SimpleAction> by lazy { mutableListOf() }

    private var onProgressApplied = false
    private val progressActions: MutableList<ProgressAction> by lazy { mutableListOf() }

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

    fun client(client: OkHttpClient) {
        request.client(client)
    }

    fun url(url: String) {
        request.url(url)
    }

    fun url(url: URL) {
        request.url(url)
    }

    fun uri(uri: URI) {
        request.url(uri)
    }

    fun tag(tag: Any) {
        request.tag(tag)
    }

    fun cacheControl(cacheControl: CacheControl) {
        request.cacheControl(cacheControl)
    }

    fun headers(operation: RequestPairs<Any?>.() -> Unit) {
        RequestPairs<Any?>().apply(operation).forEach {
            request.setHeader(it.key, it.value.toString())
        }
    }

    fun headers(headers: Map<String, Any?>) {
        headers.forEach {
            request.setHeader(it.key, it.value.toString())
        }
    }

    fun headers(vararg headers: Pair<String, Any?>) {
        headers.forEach {
            request.setHeader(it.first, it.second.toString())
        }
    }

    fun addHeaders(operation: RequestPairs<Any?>.() -> Unit) {
        RequestPairs<Any?>().apply(operation).forEach {
            request.addHeader(it.key, it.value.toString())
        }
    }

    fun addHeaders(headers: Map<String, Any?>) {
        headers.forEach {
            request.addHeader(it.key, it.value.toString())
        }
    }

    fun addHeaders(vararg headers: Pair<String, Any?>) {
        headers.forEach {
            request.addHeader(it.first, it.second.toString())
        }
    }

    fun queryParameters(operation: RequestPairs<Any?>.() -> Unit) {
        RequestPairs<Any?>().apply(operation).forEach {
            request.setQueryParameter(it.key, it.value.toString())
        }
    }

    fun queryParameters(queryParameters: Map<String, Any?>) {
        queryParameters.forEach {
            request.setQueryParameter(it.key, it.value.toString())
        }
    }

    fun queryParameters(vararg queryParameters: Pair<String, Any?>) {
        queryParameters.forEach {
            request.setQueryParameter(it.first, it.second.toString())
        }
    }

    fun addQueryParameters(operation: RequestPairs<Any?>.() -> Unit) {
        RequestPairs<Any?>().apply(operation).forEach {
            request.addQueryParameter(it.key, it.value.toString())
        }
    }

    fun addQueryParameters(queryParameters: Map<String, Any?>) {
        queryParameters.forEach {
            request.addQueryParameter(it.key, it.value.toString())
        }
    }

    fun addQueryParameters(vararg queryParameters: Pair<String, Any?>) {
        queryParameters.forEach {
            request.addQueryParameter(it.first, it.second.toString())
        }
    }

    fun encodedQueryParameters(operation: RequestPairs<Any?>.() -> Unit) {
        RequestPairs<Any?>().apply(operation).forEach {
            request.setEncodedQueryParameter(it.key, it.value.toString())
        }
    }

    fun encodedQueryParameters(encodedQueryParameters: Map<String, Any?>) {
        encodedQueryParameters.forEach {
            request.setEncodedQueryParameter(it.key, it.value.toString())
        }
    }

    fun encodedQueryParameters(vararg encodedQueryParameters: Pair<String, Any?>) {
        encodedQueryParameters.forEach {
            request.setEncodedQueryParameter(it.first, it.second.toString())
        }
    }

    fun addEncodedQueryParameters(operation: RequestPairs<Any?>.() -> Unit) {
        RequestPairs<Any?>().apply(operation).forEach {
            request.addEncodedQueryParameter(it.key, it.value.toString())
        }
    }

    fun addEncodedQueryParameters(encodedQueryParameters: Map<String, Any?>) {
        encodedQueryParameters.forEach {
            request.addEncodedQueryParameter(it.key, it.value.toString())
        }
    }

    fun addEncodedQueryParameters(vararg encodedQueryParameters: Pair<String, Any?>) {
        encodedQueryParameters.forEach {
            request.addEncodedQueryParameter(it.first, it.second.toString())
        }
    }

    fun mapResponse(mapper: OkMapper<Response, T>) {
        request.mapResponse(mapper)
    }

    fun mapError(mapper: OkMapper<Exception, T>) {
        request.mapError(mapper)
    }

    fun onStart(action: SimpleAction) {
        onStartApplied = true
        startActions.add(action)
    }

    fun onProgress(action: ProgressAction) {
        onProgressApplied = true
        progressActions.add(action)
    }

    fun onSuccess(action: Action<T>) {
        onSuccessApplied = true
        successActions.add(action)
    }

    fun onError(action: Action<Exception>) {
        onErrorApplied = true
        errorActions.add(action)
    }

    fun onCancel(action: SimpleAction) {
        onCancelApplied = true
        cancelActions.add(action)
    }

    fun onComplete(action: SimpleAction) {
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

    open fun enqueue() {
        request.enqueue()
    }

    @MainThread
    protected fun callOnStart() {
        if (!onStartApplied) return

        for (action in startActions) {
            action()
        }
    }

    @MainThread
    protected fun callOnProgress(bytes: Long, totalBytes: Long) {
        if (!onProgressApplied) return

        for (action in progressActions) {
            action(bytes, totalBytes)
        }

    }

    @MainThread
    protected fun callOnSuccess(result: T) {
        if (!onSuccessApplied) return

        for (action in successActions) {
            action(result)
        }
    }

    @MainThread
    protected fun callOnError(error: Exception) {
        if (!onErrorApplied) return

        for (action in errorActions) {
            action(error)
        }
    }

    @MainThread
    protected fun callOnCancel() {
        if (!onCancelApplied) return

        for (action in cancelActions) {
            action()
        }
    }

    @MainThread
    protected fun callOnComplete() {
        if (!onCompleteApplied) return

        for (action in completeActions) {
            action()
        }
    }

    init {
        request.setCallback(object : OkCallback<T> {

            override fun onStart() {
                callOnStart()
            }

            override fun onProgress(bytes: Long, totalBytes: Long) {
                callOnProgress(bytes, totalBytes)
            }

            override fun onSuccess(result: T) {
                callOnSuccess(result)
            }

            override fun onError(error: Exception) {
                callOnError(error)
            }

            override fun onCancel() {
                callOnCancel()
            }

            override fun onComplete() {
                callOnComplete()
            }

        })
    }
}