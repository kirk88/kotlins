package com.easy.kotlins.http.core.extension

import com.easy.kotlins.http.core.OkRequest
import okhttp3.Request
import okhttp3.Response

/**
 * Create by LiZhanPing on 2020/8/26
 */
abstract class OkExtension {
    private var _request: OkRequest? = null

    val request: OkRequest
        get() {
            return _request ?: throw NullPointerException("you must install extension to OkFaker")
        }

    fun install(faker: OkRequest?) {
        _request = faker
    }

    abstract fun onRequest(builder: Request.Builder)

    abstract fun onResponse(response: Response): Boolean

    abstract fun onError(error: Throwable): Boolean
}