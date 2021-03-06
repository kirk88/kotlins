@file:Suppress("unused")

package com.nice.kotlins.http

import com.nice.kotlins.helper.isNetworkUrl
import com.nice.kotlins.helper.plus
import com.nice.kotlins.helper.toUrl
import okhttp3.CacheControl
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient

class OkConfig internal constructor(
    internal val client: OkHttpClient? = null,
    internal val baseUrl: String? = null,
    internal val cacheControl: CacheControl? = null,
    internal val username: String? = null,
    internal val password: String? = null,
    internal val headers: Map<String, String>? = null,
    internal val queryParameters: Map<String, String>? = null,
    internal val formParameters: Map<String, String>? = null
) {

    fun newBuilder() = Builder(this)

    class Builder {

        private var client: OkHttpClient? = null
        private var baseUrl: String? = null
        private var cacheControl: CacheControl? = null
        private var username: String? = null
        private var password: String? = null

        private var headers: MutableMap<String, String>? = null
        private var queryParameters: MutableMap<String, String>? = null
        private var formParameters: MutableMap<String, String>? = null

        constructor()

        internal constructor(config: OkConfig) {
            this.client = config.client
            this.baseUrl = config.baseUrl
            this.cacheControl = config.cacheControl
            this.username = config.username
            this.password = config.password
            this.headers = config.headers?.toMutableMap()
            this.queryParameters = config.queryParameters?.toMutableMap()
            this.formParameters = config.formParameters?.toMutableMap()
        }

        fun client(client: OkHttpClient) = apply {
            this.client = client
        }

        fun client(client: () -> OkHttpClient) = apply {
            this.client = client()
        }

        fun baseUrl(baseUrl: String) = apply {
            this.baseUrl = baseUrl
        }

        fun baseUrl(baseUrl: () -> String) = apply {
            this.baseUrl = baseUrl()
        }

        fun cacheControl(cacheControl: CacheControl) = apply {
            this.cacheControl = cacheControl
        }

        fun cacheControl(cacheControl: () -> CacheControl) = apply {
            this.cacheControl = cacheControl()
        }

        fun username(username: String) = apply {
            this.username = username
        }

        fun username(username: () -> String) = apply {
            this.username = username()
        }

        fun password(password: String) = apply {
            this.password = password
        }

        fun password(password: () -> String) = apply {
            this.password = password()
        }

        fun headers(parameters: Map<String, String>) = apply {
            if (this.headers == null) {
                this.headers = mutableMapOf()
            }
            this.headers!!.putAll(parameters)
        }

        fun headers(vararg parameters: Pair<String, String>) = headers(parameters.toMap())

        fun headers(operation: RequestPairs<String, String>.() -> Unit) = headers(RequestPairs<String, String>().apply(operation).toMap())

        fun removeHeader(name: String) = apply {
            this.headers?.remove(name)
        }

        fun queryParameters(parameters: Map<String, String>) = apply {
            if (this.queryParameters == null) {
                this.queryParameters = mutableMapOf()
            }
            this.queryParameters!!.putAll(parameters)
        }

        fun queryParameters(vararg parameters: Pair<String, String>) = queryParameters(parameters.toMap())

        fun queryParameters(operation: RequestPairs<String, String>.() -> Unit) = queryParameters(RequestPairs<String, String>().apply(operation).toMap())

        fun removeQueryParameter(name: String) = apply {
            this.queryParameters?.remove(name)
        }

        fun formParameters(parameters: Map<String, String>) = apply {
            if (this.formParameters == null) {
                this.formParameters = mutableMapOf()
            }
            this.formParameters!!.putAll(parameters)
        }

        fun formParameters(vararg parameters: Pair<String, String>) = formParameters(parameters.toMap())

        fun formParameters(operation: RequestPairs<String, String>.() -> Unit) = formParameters(RequestPairs<String, String>().apply(operation).toMap())

        fun removeFormParameter(name: String) = apply {
            this.formParameters?.remove(name)
        }

        fun build(): OkConfig = OkConfig(
            client,
            baseUrl,
            cacheControl,
            username,
            password,
            headers,
            queryParameters,
            formParameters
        )
    }

}

internal fun String.toHttpUrl(config: OkConfig?): HttpUrl {
    val baseUrl = config?.baseUrl
    return when {
        this.isNetworkUrl() -> this
        !baseUrl.isNullOrEmpty() -> (baseUrl.toUrl() + this).toString()
        else -> throw IllegalArgumentException("Invalid url: $this")
    }.toHttpUrl()
}