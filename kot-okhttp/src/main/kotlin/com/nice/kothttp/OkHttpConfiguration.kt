@file:Suppress("UNUSED")

package com.nice.kothttp

import okhttp3.CacheControl
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import java.net.URL

object OkHttpConfiguration {

    var client: OkHttpClient? = null
        private set
    var baseUrl: String? = null
        private set
    var cacheControl: CacheControl? = null
        private set
    var username: String? = null
        private set
    var password: String? = null
        private set
    var headers: Map<String, String>? = null
        private set
    var queryParameters: Map<String, String>? = null
        private set
    var formParameters: Map<String, String>? = null
        private set

    class Setter {

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

        fun baseUrl(baseUrl: String) = apply {
            this.baseUrl = baseUrl
        }

        fun cacheControl(cacheControl: CacheControl) = apply {
            this.cacheControl = cacheControl
        }

        fun username(username: String) = apply {
            this.username = username
        }

        fun password(password: String) = apply {
            this.password = password
        }

        fun headers(parameters: Map<String, String>) = apply {
            if (this.headers == null) {
                this.headers = mutableMapOf()
            }
            this.headers!!.putAll(parameters)
        }

        fun headers(vararg parameters: Pair<String, String>) = headers(parameters.toMap())

        fun queryParameters(parameters: Map<String, String>) = apply {
            if (this.queryParameters == null) {
                this.queryParameters = mutableMapOf()
            }
            this.queryParameters!!.putAll(parameters)
        }

        fun queryParameters(vararg parameters: Pair<String, String>) =
            queryParameters(parameters.toMap())

        fun formParameters(parameters: Map<String, String>) = apply {
            if (this.formParameters == null) {
                this.formParameters = mutableMapOf()
            }
            this.formParameters!!.putAll(parameters)
        }

        fun formParameters(vararg parameters: Pair<String, String>) =
            formParameters(parameters.toMap())

        fun apply() = OkHttpConfiguration.also {
            if (client != null) it.client = client
            if (baseUrl != null) it.baseUrl = baseUrl
            if (cacheControl != null) it.cacheControl = cacheControl
            if (username != null) it.username = username
            if (password != null) it.password = password
            if (headers != null) it.headers = headers
            if (queryParameters != null) it.queryParameters = queryParameters
            if (formParameters != null) it.formParameters = formParameters
        }

    }

}

internal fun String.toHttpUrl(config: OkHttpConfiguration?): HttpUrl {
    val domain = config?.baseUrl
    return when {
        this.isNetworkUrl() -> this
        !domain.isNullOrEmpty() -> URL(domain).resolve(this)
        else -> throw IllegalArgumentException("Invalid url: $this")
    }.toHttpUrl()
}

private fun String.isNetworkUrl() =
    startsWith("http://", ignoreCase = true) || startsWith("https://", ignoreCase = true)

private fun URL.resolve(spec: String): String = URL(this, spec).toString()