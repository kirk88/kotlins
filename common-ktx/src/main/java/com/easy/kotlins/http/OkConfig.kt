package com.easy.kotlins.http

import okhttp3.CacheControl
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

    internal val headers: MutableMap<String, String> = mutableMapOf()
    internal val queryParameters: MutableMap<String, String> = mutableMapOf()
    internal val formParameters: MutableMap<String, String> = mutableMapOf()

    fun setter(): Setter = Setter(this)

    class Setter internal constructor(private val config: OkConfig) {

        private var client: OkHttpClient? = null
        private var baseUrl: String? = null
        private var cacheControl: CacheControl? = null
        private var username: String? = null
        private var password: String? = null
        private var headersApplied = false
        private val headers: MutableMap<String, Any> by lazy { mutableMapOf() }
        private var queryParametersApplied = false
        private val queryParameters: MutableMap<String, Any> by lazy { mutableMapOf() }
        private var formParametersApplied = false
        private val formParameters: MutableMap<String, Any> by lazy { mutableMapOf() }

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

        fun headers(vararg headers: Pair<String, Any>) = apply {
            headersApplied = true
            this.headers.putAll(headers)
        }

        fun headers(headers: Map<String, Any>) = apply {
            headersApplied = true
            this.headers.putAll(headers)
        }

        fun headers(headers: RequestPairs<String, Any>.() -> Unit) = apply {
            headersApplied = true
            this.headers.putAll(RequestPairs<String, Any>().apply(headers).toMap())
        }

        fun queryParameters(vararg parameters: Pair<String, Any>) = apply {
            queryParametersApplied = true
            queryParameters.putAll(parameters)
        }

        fun queryParameters(parameters: Map<String, Any>) = apply {
            queryParametersApplied = true
            queryParameters.putAll(parameters)
        }

        fun queryParameters(parameters: RequestPairs<String, Any>.() -> Unit) = apply {
            queryParametersApplied = true
            queryParameters.putAll(RequestPairs<String, Any>().apply(parameters).toMap())
        }

        fun formParameters(vararg parameters: Pair<String, Any>) = apply {
            formParametersApplied = true
            formParameters.putAll(parameters)
        }

        fun formParameters(parameters: Map<String, Any>) = apply {
            formParametersApplied = true
            formParameters.putAll(parameters)
        }

        fun formParameters(parameters: RequestPairs<String, Any>.() -> Unit) = apply {
            formParametersApplied = true
            formParameters.putAll(RequestPairs<String, Any>().apply(parameters).toMap())
        }

        fun apply() {
            if (client != null) config.client = client
            if (baseUrl != null) config.baseUrl = baseUrl
            if (cacheControl != null) config.cacheControl = cacheControl
            if (username != null) config.username = username
            if (password != null) config.password = password
            if (headersApplied) config.headers.putAll(headers.mapValues { it.value.toString() })
            if (queryParametersApplied) config.queryParameters.putAll(queryParameters.mapValues { it.value.toString() })
            if (formParametersApplied) config.formParameters.putAll(formParameters.mapValues { it.value.toString() })
        }

        fun commit() {
            config.client = client
            config.baseUrl = baseUrl
            config.cacheControl = cacheControl
            config.username = username
            config.password = password
            config.headers.clear()
            config.queryParameters.clear()
            config.formParameters.clear()
            if (headersApplied) config.headers.putAll(headers.mapValues { it.value.toString() })
            if (queryParametersApplied) config.queryParameters.putAll(queryParameters.mapValues { it.value.toString() })
            if (formParametersApplied) config.formParameters.putAll(formParameters.mapValues { it.value.toString() })
        }
    }

}
