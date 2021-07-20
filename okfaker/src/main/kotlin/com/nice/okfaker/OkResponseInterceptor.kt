package com.nice.okfaker

import okhttp3.Response

fun interface OkResponseInterceptor {

    fun intercept(response: Response): Response

}