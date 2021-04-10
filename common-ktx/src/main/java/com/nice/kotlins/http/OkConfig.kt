@file:Suppress("unused")

package com.nice.kotlins.http

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

    fun setter(): Setter = Setter(this)

    override fun toString(): String {
        return "OkConfig(client=$client, baseUrl=$baseUrl, cacheControl=$cacheControl, username=$username, password=$password)"
    }

    class Setter internal constructor(private val config: OkConfig) {

        private var client: OkHttpClient? = null
        private var baseUrl: String? = null
        private var cacheControl: CacheControl? = null
        private var username: String? = null
        private var password: String? = null

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

        fun apply(): OkConfig {
            if (client != null) config.client = client
            if (baseUrl != null) config.baseUrl = baseUrl
            if (cacheControl != null) config.cacheControl = cacheControl
            if (username != null) config.username = username
            if (password != null) config.password = password
            return config
        }

        fun commit(): OkConfig {
            config.client = client
            config.baseUrl = baseUrl
            config.cacheControl = cacheControl
            config.username = username
            config.password = password
            return config
        }
    }

}
