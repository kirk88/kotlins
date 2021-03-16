package com.easy.kotlins.http

import okhttp3.Request

abstract class OkDownloadMapper<T, R> : OkMapper<T, R> {

    internal val requestInterceptor: OkRequestInterceptor = OkRequestInterceptor {
        shouldInterceptRequest(it)
    }


    open fun shouldInterceptRequest(request: Request): Request {
        return request
    }

}