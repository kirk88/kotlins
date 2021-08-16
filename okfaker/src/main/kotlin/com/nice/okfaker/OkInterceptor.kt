package com.nice.okfaker

import okhttp3.Request
import okhttp3.Response

typealias OkRequestInterceptor = suspend (Request) -> Request

typealias OkResponseInterceptor = suspend (response: Response) -> Response