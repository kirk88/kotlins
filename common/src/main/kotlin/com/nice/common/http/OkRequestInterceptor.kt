package com.nice.common.http

import okhttp3.Request

fun interface OkRequestInterceptor {

    fun intercept(request: Request): Request

}