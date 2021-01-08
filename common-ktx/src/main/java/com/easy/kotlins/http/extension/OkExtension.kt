package com.easy.kotlins.http.extension

import okhttp3.Request

interface OkExtension {
    fun shouldInterceptRequest(request: Request): Request
}