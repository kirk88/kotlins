package com.nice.okfaker

import okhttp3.Request

fun interface OkRequestInterceptor {

    fun intercept(request: Request): Request

}