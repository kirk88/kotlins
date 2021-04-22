package com.nice.kotlins.http

import okhttp3.Request

fun interface OkRequestInterceptor {

    fun intercept(request: Request): Request

}