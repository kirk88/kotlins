package com.easy.kotlins.http.extension

import okhttp3.Request
import okhttp3.Response

/**
 * Create by LiZhanPing on 2020/8/26
 */
interface OkExtension {

    fun shouldInterceptRequest(builder: Request.Builder): Request

    fun onResponse(response: Response): Boolean

    fun onError(error: Throwable): Boolean
}