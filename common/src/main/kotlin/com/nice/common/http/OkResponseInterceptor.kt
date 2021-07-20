package com.nice.common.http

import okhttp3.Response

fun interface OkResponseInterceptor {

    fun intercept(response: Response): Response

}