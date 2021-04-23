package com.nice.kotlins.http.cookie

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class DefaultCookieJar(private val cookieStore: CookieStore) : CookieJar {

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieStore[url] = cookies
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieStore[url]
    }

}