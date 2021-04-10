package com.nice.kotlins.http

import okhttp3.Response

fun interface OkResponseInterceptor {

    fun shouldInterceptResponse(response: Response): Response

}