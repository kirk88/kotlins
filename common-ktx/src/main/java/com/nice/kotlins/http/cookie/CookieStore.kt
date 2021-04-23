package com.nice.kotlins.http.cookie

import okhttp3.Cookie
import okhttp3.HttpUrl

interface CookieStore {

    operator fun set(httpUrl: HttpUrl, cookie: Cookie)

    operator fun set(httpUrl: HttpUrl, cookies: List<Cookie>)

    operator fun get(httpUrl: HttpUrl): List<Cookie>

    fun getCookies(): List<Cookie>

    fun remove(httpUrl: HttpUrl, cookie: Cookie): Boolean

    fun removeAll(): Boolean

}