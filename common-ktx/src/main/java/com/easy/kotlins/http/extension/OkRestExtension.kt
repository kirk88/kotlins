package com.easy.kotlins.http.extension

import okhttp3.Response

interface OkRestExtension : OkExtension {

    fun onResponse(response: Response): Boolean

    fun onError(error: Throwable): Boolean
}