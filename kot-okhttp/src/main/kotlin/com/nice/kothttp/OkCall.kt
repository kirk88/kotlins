package com.nice.kothttp

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

internal val DEFAULT_CLIENT = OkHttpClient.Builder()
    .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
    .build()

interface OkCall<T> {

    val isExecuted: Boolean

    val isCanceled: Boolean

    fun tag(): Any?

    fun <T> tag(type: Class<out T>): T?

    fun make(): T

    fun cancel()

}