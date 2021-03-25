@file:Suppress("unused")

package com.easy.kotlins.helper

import java.net.URL

fun String.toUrl(): URL = URL(this)

operator fun URL.plus(spec: String): URL = URL(this, spec)

fun String.isFileUrl(): Boolean = UrlGuess.isFileUrl(this)

fun String.isContentUrl(): Boolean = UrlGuess.isContentUrl(this)

fun String.isAssetUrl(): Boolean = UrlGuess.isAssetUrl(this)

fun String.isNetworkUrl(): Boolean = UrlGuess.isNetworkUrl(this)

internal object UrlGuess {

    const val ASSET_BASE = "file:///android_asset/"

    const val RESOURCE_BASE = "file:///android_res/"
    const val FILE_BASE = "file:"
    const val PROXY_BASE = "file:///cookieless_proxy/"
    const val CONTENT_BASE = "content:"
    const val HTTP_BASE = "http://"
    const val HTTPS_BASE = "https://"

    fun isFileUrl(url: String): Boolean {
        return url.startsWith(FILE_BASE) &&
                !url.startsWith(ASSET_BASE) &&
                !url.startsWith(PROXY_BASE)
    }

    fun isContentUrl(url: String): Boolean {
        return url.startsWith(CONTENT_BASE)
    }

    fun isAssetUrl(url: String): Boolean {
        return url.startsWith(ASSET_BASE)
    }

    fun isNetworkUrl(url: String): Boolean {
        return if (url.isEmpty()) {
            false
        } else isHttpUrl(url) || isHttpsUrl(url)
    }


    private fun isHttpUrl(url: String): Boolean {
        return url.length > 6 &&
                url.substring(0, 7).equals(HTTP_BASE, ignoreCase = true)
    }

    private fun isHttpsUrl(url: String): Boolean {
        return url.length > 7 &&
                url.substring(0, 8).equals(HTTPS_BASE, ignoreCase = true)
    }


}