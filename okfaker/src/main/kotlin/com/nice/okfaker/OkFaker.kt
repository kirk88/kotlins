@file:Suppress("unused")

package com.nice.okfaker

import okhttp3.*
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class OkFaker<T> private constructor(
    private val request: OkRequest<T>,
    private val onStartActions: List<SimpleAction>?,
    private val onSuccessActions: List<Action<T>>?,
    private val onErrorActions: List<Action<Throwable>>?,
    private val onCompleteActions: List<SimpleAction>?,
    private val onCancelActions: List<SimpleAction>?
) {

    val isCanceled: Boolean
        get() = request.isCanceled

    val isExecuted: Boolean
        get() = request.isExecuted

    fun tag(): Any? = request.tag()

    fun <T> tag(type: Class<out T>): T? = request.tag(type)

    fun cancel() = request.cancel()

    fun execute(): T = request.execute()

    fun executeOrElse(onError: (Throwable) -> T): T = runCatching { execute() }.getOrElse(onError)

    fun executeOrNull(): T? = runCatching { execute() }.getOrNull()

    fun enqueue() = apply {
        request.enqueue(OkCallbackWrapper(object : OkCallback<T> {
            override fun onStart() {
                onStartActions?.forEach { it.invoke() }
            }

            override fun onSuccess(result: T) {
                onSuccessActions?.forEach { it.invoke(result) }
            }

            override fun onError(error: Throwable) {
                onErrorActions?.forEach { it.invoke(error) }
            }

            override fun onComplete() {
                onCompleteActions?.forEach { it.invoke() }
            }

            override fun onCancel() {
                onCancelActions?.forEach { it.invoke() }
            }
        }))
    }

    private class OkCallbackWrapper<T>(
        private val callback: OkCallback<T>
    ) : OkCallback<T> {
        override fun onStart() {
            OkCallbacks.onStart(callback)
        }

        override fun onSuccess(result: T) {
            OkCallbacks.onSuccess(callback, result)
        }

        override fun onError(error: Throwable) {
            OkCallbacks.onError(callback, error)
        }

        override fun onComplete() {
            OkCallbacks.onComplete(callback)
        }

        override fun onCancel() {
            OkCallbacks.onCancel(callback)
        }
    }

    class Builder<T> {

        private val builder: OkRequest.Builder<T>

        private var onStartApplied = false
        private val onStartActions: MutableList<SimpleAction> by lazy { mutableListOf() }

        private var onSuccessApplied = false
        private val onSuccessActions: MutableList<Action<T>> by lazy { mutableListOf() }

        private var onErrorApplied = false
        private val onErrorActions: MutableList<Action<Throwable>> by lazy { mutableListOf() }

        private var onCompleteApplied = false
        private val onCompleteActions: MutableList<SimpleAction> by lazy { mutableListOf() }

        private var onCancelApplied = false
        private val onCancelActions: MutableList<SimpleAction> by lazy { mutableListOf() }

        private val config: OkConfig?

        @PublishedApi
        internal constructor(method: OkRequestMethod) {
            builder = OkRequest.Builder(method)
            this.config = null
        }

        @PublishedApi
        internal constructor(method: OkRequestMethod, config: OkConfig) {
            builder = OkRequest.Builder(method)
            this.config = config

            config.client?.let { client(it) }
            config.cacheControl?.let { cacheControl(it) }
            config.username?.let { username(it) }
            config.password?.let { password(it) }
            config.headers?.forEach {
                builder.header(it.key, it.value)
            }
            config.queryParameters?.forEach {
                builder.setQueryParameter(it.key, it.value)
            }
            config.formParameters?.forEach {
                builder.addFormParameter(it.key, it.value)
            }
        }

        fun client(client: OkHttpClient) = apply {
            builder.client(client)
        }

        fun client(client: () -> OkHttpClient) = apply {
            builder.client(client())
        }

        fun url(url: String) = apply {
            builder.url(url.toHttpUrl(config))
        }

        fun url(url: () -> String) = apply {
            builder.url(url().toHttpUrl(config))
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

        fun tag(tag: Any?) = apply {
            builder.tag(tag)
        }

        fun tag(tag: () -> Any) = apply {
            builder.tag(tag)
        }

        fun <T> tag(type: Class<in T>, tag: T?) = apply {
            builder.tag(type, tag)
        }

        fun <T> tag(type: Class<in T>, tag: () -> T) = apply {
            builder.tag(type, tag())
        }

        fun headers(buildAction: HeadersBuilder.() -> Unit) = apply {
            HeadersBuilder().apply(buildAction)
        }

        fun queryParameters(buildAction: QueryParametersBuilder.() -> Unit) = apply {
            QueryParametersBuilder().apply(buildAction)
        }

        fun formParameters(buildAction: FormParametersBuilder.() -> Unit) = apply {
            FormParametersBuilder().apply(buildAction)
        }

        fun multipartBody(buildAction: MultipartBodyBuilder.() -> Unit) = apply {
            MultipartBodyBuilder().apply(buildAction)
        }

        fun requestBody(body: RequestBody) = apply {
            builder.requestBody(body)
        }

        fun requestBody(body: () -> RequestBody) = apply {
            builder.requestBody(body())
        }

        fun interceptRequest(interceptor: OkRequestInterceptor) = apply {
            builder.addRequestInterceptor(interceptor)
        }

        fun interceptResponse(interceptor: OkResponseInterceptor) = apply {
            builder.addResponseInterceptor(interceptor)
        }

        fun mapResponse(mapper: OkResponseMapper<T>) = apply {
            builder.mapResponse(mapper)
        }

        fun mapError(mapper: OkErrorMapper<T>) = apply {
            builder.mapError(mapper)
        }

        fun extension(extension: OkExtension<T>) {
            builder.addRequestInterceptor {
                extension.shouldInterceptRequest(it)
            }
            builder.addResponseInterceptor {
                extension.shouldInterceptResponse(it)
            }
            builder.mapResponse {
                extension.map(it)
            }
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
            onCompleteApplied = true
            onCompleteActions.add(action)
        }

        fun onCancel(action: SimpleAction) = apply {
            onCancelApplied = true
            onCancelActions.add(action)
        }

        fun build(): OkFaker<T> = OkFaker(
            builder.build(),
            if (onStartApplied) onStartActions else null,
            if (onSuccessApplied) onSuccessActions else null,
            if (onErrorApplied) onErrorActions else null,
            if (onCompleteApplied) onCompleteActions else null,
            if (onCancelApplied) onCancelActions else null
        )

        fun execute(): T = build().execute()

        fun executeOrElse(onError: (Throwable) -> T): T = build().executeOrElse(onError)

        fun executeOrNull(): T? = build().executeOrNull()

        fun enqueue(): OkFaker<T> = build().enqueue()

        inner class HeadersBuilder internal constructor() {

            fun add(name: String, value: Any?) {
                builder.addHeader(name, value.toStringOrEmpty())
            }

            fun set(name: String, value: Any?) {
                builder.header(name, value.toStringOrEmpty())
            }

            fun remove(name: String) {
                builder.removeHeaders(name)
            }

        }

        inner class QueryParametersBuilder internal constructor() {

            fun add(name: String, value: Any?) {
                builder.addQueryParameter(name, value.toStringOrEmpty())
            }

            fun addEncoded(name: String, value: Any?) {
                builder.addEncodedQueryParameter(name, value.toStringOrEmpty())
            }

            fun set(name: String, value: Any?) {
                builder.setQueryParameter(name, value.toStringOrEmpty())
            }

            fun setEncoded(name: String, value: Any?) {
                builder.setEncodedQueryParameter(name, value.toStringOrEmpty())
            }

            fun remove(name: String) {
                builder.removeQueryParameters(name)
            }

            fun removeEncoded(name: String) {
                builder.removeEncodedQueryParameters(name)
            }

        }

        inner class FormParametersBuilder internal constructor() {

            fun add(name: String, value: Any?) {
                builder.addFormParameter(name, value.toStringOrEmpty())
            }

            fun addEncoded(name: String, value: Any?) {
                builder.addEncodedFormParameter(name, value.toStringOrEmpty())
            }

        }

        inner class MultipartBodyBuilder internal constructor() {

            fun add(name: String, value: Any?) {
                builder.addFormDataPart(name, value.toStringOrEmpty())
            }

            fun add(name: String, filename: String?, body: RequestBody) {
                builder.addFormDataPart(name, filename, body)
            }

            fun add(name: String, contentType: MediaType?, file: File) {
                builder.addFormDataPart(name, file.name, file.asRequestBody(contentType))
            }

            fun add(part: MultipartBody.Part) {
                builder.addPart(part)
            }

            fun add(body: RequestBody) {
                builder.addPart(body)
            }

        }

    }

    companion object {

        private var config: OkConfig = OkConfig()
        fun setGlobalConfig(config: OkConfig) {
            this.config = config
        }

        val globalConfig: OkConfig get() = config

        inline fun <T> builder(
            method: OkRequestMethod,
            config: OkConfig,
            block: Builder<T>.() -> Unit = {}
        ): Builder<T> = Builder<T>(method, config).apply(block)

        inline fun <T> builder(
            method: OkRequestMethod,
            block: Builder<T>.() -> Unit = {}
        ): Builder<T> = Builder<T>(method).apply(block)

        inline fun <reified T> get(block: Builder<T>.() -> Unit = {}): Builder<T> = Builder<T>(OkRequestMethod.GET, globalConfig)
            .mapResponse(typeMapper())
            .apply(block)

        inline fun <reified T> post(block: Builder<T>.() -> Unit = {}): Builder<T> = Builder<T>(OkRequestMethod.POST, globalConfig)
            .mapResponse(typeMapper())
            .apply(block)

        inline fun <reified T> delete(block: Builder<T>.() -> Unit = {}): Builder<T> = Builder<T>(OkRequestMethod.DELETE, globalConfig)
            .mapResponse(typeMapper())
            .apply(block)

        inline fun <reified T> put(block: Builder<T>.() -> Unit = {}): Builder<T> = Builder<T>(OkRequestMethod.PUT, globalConfig)
            .mapResponse(typeMapper())
            .apply(block)

        inline fun <reified T> head(block: Builder<T>.() -> Unit = {}): Builder<T> = Builder<T>(OkRequestMethod.HEAD, globalConfig)
            .mapResponse(typeMapper())
            .apply(block)

        inline fun <reified T> patch(block: Builder<T>.() -> Unit = {}): Builder<T> = Builder<T>(OkRequestMethod.PATCH, globalConfig)
            .mapResponse(typeMapper())
            .apply(block)

    }

}

typealias Action<T> = (T) -> Unit
typealias SimpleAction = () -> Unit

private fun Any?.toStringOrEmpty() = (this ?: "").toString()