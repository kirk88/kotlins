package com.easy.kotlins.http.core.extension

import com.easy.kotlins.http.core.OkRequest

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
}