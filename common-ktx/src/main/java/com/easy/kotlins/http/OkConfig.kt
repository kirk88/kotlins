package com.easy.kotlins.http

import okhttp3.CacheControl
import okhttp3.OkHttpClient

class OkConfig {

    internal var client: OkHttpClient? = null
        private set
    internal var baseUrl: String? = null
        private set
    internal var cacheControl: CacheControl? = null
        private set

    internal val headers: MutableMap<String, String> = mutableMapOf()
    internal val queryParameters: MutableMap<String, String> = mutableMapOf()
    internal val formParameters: MutableMap<String, String> = mutableMapOf()

    fun client(client: OkHttpClient) = apply {
        this.client = client
    }

    fun baseUrl(baseUrl: String) = apply {
        this.baseUrl = baseUrl
    }

    fun cacheControl(cacheControl: CacheControl) = apply {
        this.cacheControl = cacheControl
    }

    fun headers(vararg headers: Pair<String, String>) = apply {
        this.headers.putAll(headers)
    }

    fun headers(headers: Map<String, String>) = apply {
        this.headers.putAll(headers)
    }

    fun queryParameters(vararg parameters: Pair<String, String>) = apply {
        queryParameters.putAll(parameters)
    }

    fun queryParameters(parameters: Map<String, String>) = apply {
        queryParameters.putAll(parameters)
    }

    fun formParameters(vararg parameters: Pair<String, String>) = apply {
        formParameters.putAll(parameters)
    }

    fun formParameters(parameters: Map<String, String>) = apply {
        formParameters.putAll(parameters)
    }

}
