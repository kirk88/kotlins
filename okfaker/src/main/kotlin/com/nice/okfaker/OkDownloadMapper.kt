package com.nice.okfaker

import okhttp3.Request
import okhttp3.Response

abstract class OkDownloadMapper<R> : OkMapper<Response, R> {

    internal val requestInterceptor: OkRequestInterceptor = OkRequestInterceptor {
        shouldInterceptRequest(it)
    }
    internal val responseInterceptor: OkResponseInterceptor = OkResponseInterceptor {
        shouldInterceptResponse(it)
    }

    open fun shouldInterceptRequest(request: Request): Request {
        return request
    }

    open fun shouldInterceptResponse(response: Response): Response {
        return response
    }

}