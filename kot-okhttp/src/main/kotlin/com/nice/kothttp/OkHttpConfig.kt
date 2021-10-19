@file:Suppress("unused")

package com.nice.kothttp

import okhttp3.CacheControl
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import java.net.URL

object OkHttpConfig {

    internal var client: OkHttpClient? = null
    internal var domain: String? = null
    internal var cacheControl: CacheControl? = null
    internal var username: String? = null
    internal var password: String? = null
    internal var headers: Map<String, String>? = null
    internal var queryParameters: Map<String, String>? = null
    internal var formParameters: Map<String, String>? = null

    class Setter {

        private var client: OkHttpClient? = null
        private var domain: String? = null
        private var cacheControl: CacheControl? = null
        private var username: String? = null
        private var password: String? = null

        private var headers: MutableMap<String, String>? = null
        private var queryParameters: MutableMap<String, String>? = null
        private var formParameters: MutableMap<String, String>? = null

        fun client(client: OkHttpClient) = apply {
            this.client = client
        }

        fun domain(domain: String) = apply {
            this.domain = domain
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

        fun apply() = OkHttpConfig.also {
            if (client != null) it.client = client
            if (domain != null) it.domain = domain
            if (cacheControl != null) it.cacheControl = cacheControl
            if (username != null) it.username = username
            if (password != null) it.password = password
            if (headers != null) it.headers = headers
            if (queryParameters != null) it.queryParameters = queryParameters
            if (formParameters != null) it.formParameters = formParameters
        }

    }

}

internal fun String.toHttpUrl(config: OkHttpConfig?): HttpUrl {
    val domain = config?.domain
    return when {
        this.isNetworkUrl() -> this
        !domain.isNullOrEmpty() -> URL(domain).resolve(this)
        else -> throw IllegalArgumentException("Invalid url: $this")
    }.toHttpUrl()
}

private fun String.isNetworkUrl() =
    startsWith("http://", ignoreCase = true) || startsWith("https://", ignoreCase = true)

private fun URL.resolve(spec: String): String = URL(this, spec).toString()