package com.nice.kothttp

import okhttp3.Request
import okhttp3.Response

fun interface OkRequestInterceptor {
    suspend fun intercept(request: Request): Request
}

fun interface OkResponseInterceptor {
    suspend fun intercept(response: Response): Response
}

suspend operator fun OkRequestInterceptor.invoke(request: Request) = intercept(request)

suspend operator fun OkResponseInterceptor.invoke(response: Response) = intercept(response)