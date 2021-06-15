@file:Suppress("unused")

package com.nice.kotlins.http

import com.nice.kotlins.helper.isNetworkUrl
import com.nice.kotlins.helper.plus
import com.nice.kotlins.helper.toUrl
import okhttp3.CacheControl
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient

class OkConfig internal constructor() {

    internal var client: OkHttpClient? = null
        private set
    internal var baseUrl: String? = null
        private set
    internal var cacheControl: CacheControl? = null
        private set
    internal var username: String? = null
        private set
    internal var password: String? = null
        private set
    internal var headers: Map<String, String>? = null
        private set
    internal var queryParameters: Map<String, String>? = null
        private set
    internal var formParameters: Map<String, String>? = null
        private set

    fun newSetter() = Setter(this)

    class Setter internal constructor(private val config: OkConfig) {

        private var client: OkHttpClient? = null
        private var baseUrl: String? = null
        private var cacheControl: CacheControl? = null
        private var username: String? = null
        private var password: String? = null

        private var headers: MutableMap<String, String>? = null
        private var queryParameters: MutableMap<String, String>? = null
        private var formParameters: MutableMap<String, String>? = null

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

        fun headers(vararg parameters: Pair<String, String>) = apply {
            if (this.headers == null) {
                this.headers = mutableMapOf()
            }
            this.headers!!.putAll(parameters)
        }

        fun headers(operation: RequestPairs<String, String>.() -> Unit) = apply {
            if (this.headers == null) {
                this.headers = mutableMapOf()
            }
            this.headers!!.putAll(RequestPairs<String, String>().apply(operation).toMap())
        }

        fun removeHeader(name: String) = apply {
            this.headers?.remove(name)
        }

        fun queryParameters(vararg parameters: Pair<String, String>) = apply {
            if (this.queryParameters == null) {
                this.queryParameters = mutableMapOf()
            }
            this.queryParameters!!.putAll(parameters)
        }

        fun queryParameters(operation: RequestPairs<String, String>.() -> Unit) = apply {
            if (this.queryParameters == null) {
                this.queryParameters = mutableMapOf()
            }
            this.queryParameters!!.putAll(RequestPairs<String, String>().apply(operation).toMap())
        }

        fun removeQueryParameter(name: String) = apply {
            this.queryParameters?.remove(name)
        }

        fun formParameters(vararg parameters: Pair<String, String>) = apply {
            if (this.formParameters == null) {
                this.formParameters = mutableMapOf()
            }
            this.formParameters!!.putAll(parameters)
        }

        fun formParameters(operation: RequestPairs<String, String>.() -> Unit) = apply {
            if (this.formParameters == null) {
                this.formParameters = mutableMapOf()
            }
            this.formParameters!!.putAll(RequestPairs<String, String>().apply(operation).toMap())
        }

        fun removeFormParameter(name: String) = apply {
            this.formParameters?.remove(name)
        }

        fun apply(): OkConfig {
            if (client != null) config.client = client
            if (baseUrl != null) config.baseUrl = baseUrl
            if (cacheControl != null) config.cacheControl = cacheControl
            if (username != null) config.username = username
            if (password != null) config.password = password
            if (headers != null) config.headers =
                mutableMapOf<String, String>().apply {
                    config.headers?.let { headers -> putAll(headers) }
                    putAll(headers!!)
                }
            if (queryParameters != null) config.queryParameters =
                mutableMapOf<String, String>().apply {
                    config.queryParameters?.let { parameters -> putAll(parameters) }
                    putAll(queryParameters!!)
                }
            if (formParameters != null) config.formParameters =
                mutableMapOf<String, String>().apply {
                    config.formParameters?.let { parameters -> putAll(parameters) }
                    putAll(formParameters!!)
                }
            return config
        }

        fun commit(): OkConfig {
            config.client = client
            config.baseUrl = baseUrl
            config.cacheControl = cacheControl
            config.username = username
            config.password = password
            config.headers = headers
            config.queryParameters = queryParameters
            config.formParameters = formParameters
            return config
        }
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