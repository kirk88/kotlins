package com.nice.okfaker

import okhttp3.Request
import okhttp3.Response

typealias OkRequestInterceptor = (Request) -> Request

typealias OkResponseInterceptor = (response: Response) -> Response