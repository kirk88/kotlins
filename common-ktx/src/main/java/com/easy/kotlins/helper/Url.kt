package com.easy.kotlins.helper

import android.webkit.URLUtil
import java.net.URL

fun String.toUrl(): URL = URL(this)

operator fun URL.plus(spec: String): URL = URL(this, spec)

fun String.isFileUrl(): Boolean = URLUtil.isFileUrl(this)

fun String.isContentUrl(): Boolean = URLUtil.isContentUrl(this)

fun String.isAssetUrl(): Boolean = URLUtil.isAssetUrl(this)

fun String.isNetworkUrl(): Boolean = URLUtil.isNetworkUrl(this)

fun String.isValidUrl(): Boolean = URLUtil.isValidUrl(this)