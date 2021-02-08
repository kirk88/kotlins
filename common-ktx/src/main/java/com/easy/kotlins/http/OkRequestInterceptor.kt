package com.easy.kotlins.http

import okhttp3.Request

fun interface OkRequestInterceptor {

    fun shouldInterceptRequest(request: Request): Request

}