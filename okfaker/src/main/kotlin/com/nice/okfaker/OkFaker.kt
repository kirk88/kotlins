@file:Suppress("unused")

package com.nice.okfaker

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.*
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class OkFaker<T> internal constructor(private val request: OkRequest<T>) {

    val isCanceled: Boolean
        get() = request.isCanceled

    val isExecuted: Boolean
        get() = request.isExecuted

    fun tag(): Any? = request.tag()

    fun <T> tag(type: Class<out T>): T? = request.tag(type)

    fun cancel() = request.cancel()

    suspend fun execute(): T = request.execute()

    suspend fun executeOrNull(): T? =
        kotlin.runCatching { request.execute() }.getOrNull()

    suspend fun executeOrElse(onFailure: (Throwable) -> T): T =
        kotlin.runCatching { request.execute() }.getOrElse(onFailure)

    companion object {

        private var config: OkConfig = OkConfig()
        fun setGlobalConfig(config: OkConfig) {
            this.config = config
        }

        val globalConfig: OkConfig get() = config

    }

}

class OkFakerBuilder<T> @PublishedApi internal constructor() {

    private val config = OkFaker.globalConfig

    private val builder: OkRequest.Builder<T> = OkRequest.Builder()

    fun client(client: OkHttpClient) = apply {
        builder.client(client)
    }

    fun method(method: OkRequestMethod) = apply {
        builder.method(method)
    }

    fun url(url: String) = apply {
        builder.url(url.toHttpUrl(config))
    }

    fun cacheControl(cacheControl: CacheControl) = apply {
        builder.cacheControl(cacheControl)
    }

    fun username(username: String) = apply {
        builder.username(username)
    }

    fun password(password: String) = apply {
        builder.password(password)
    }

    fun tag(tag: Any?) {
        builder.tag(tag)
    }

    fun <T> tag(type: Class<in T>, tag: T?) = apply {
        builder.tag(type, tag)
    }

    fun headers(buildAction: HeadersBuilder.() -> Unit) = apply {
        HeadersBuilder(builder).apply(buildAction)
    }

    fun queryParameters(buildAction: QueryParametersBuilder.() -> Unit) = apply {
        QueryParametersBuilder(builder).apply(buildAction)
    }

    fun formParameters(buildAction: FormParametersBuilder.() -> Unit) = apply {
        FormParametersBuilder(builder).apply(buildAction)
    }

    fun multipartBody(buildAction: MultipartBodyBuilder.() -> Unit) = apply {
        MultipartBodyBuilder(builder).apply(buildAction)
    }

    fun requestBody(body: RequestBody) = apply {
        builder.requestBody(body)
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

    fun extension(extension: OkExtension<T>) = apply {
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

    fun build(): OkFaker<T> = OkFaker(builder.build())

    suspend fun execute(): T = build().execute()

    suspend fun executeOrNull(): T? = build().executeOrNull()

    suspend fun executeOrElse(onFailure: (Throwable) -> T): T = build().executeOrElse(onFailure)

    init {
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

}

class HeadersBuilder internal constructor(private val builder: OkRequest.Builder<*>) {

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

class QueryParametersBuilder internal constructor(private val builder: OkRequest.Builder<*>) {

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

class FormParametersBuilder internal constructor(private val builder: OkRequest.Builder<*>) {

    fun add(name: String, value: Any?) {
        builder.addFormParameter(name, value.toStringOrEmpty())
    }

    fun addEncoded(name: String, value: Any?) {
        builder.addEncodedFormParameter(name, value.toStringOrEmpty())
    }

}

class MultipartBodyBuilder internal constructor(private val builder: OkRequest.Builder<*>) {

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

private fun Any?.toStringOrEmpty() = (this ?: "").toString()

inline fun <reified T> okFakerBuilder() = OkFakerBuilder<T>().mapResponse(typeMapper())

inline fun <reified T> buildOkFaker(buildAction: OkFakerBuilder<T>.() -> Unit): OkFaker<T> = okFakerBuilder<T>()
    .apply(buildAction)
    .build()

inline fun <reified T> okFakerFlow(buildAction: OkFakerBuilder<T>.() -> Unit): Flow<T> = okFakerBuilder<T>()
    .apply(buildAction)
    .build()
    .asFlow()

fun <T> OkFaker<T>.asFlow(): Flow<T> = flow {
    emit(execute())
}


