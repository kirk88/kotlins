@file:Suppress("unused")

package com.nice.kotlins.http.cookie

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import android.util.Log
import okhttp3.Cookie
import okhttp3.HttpUrl
import okhttp3.internal.and
import java.io.*
import java.net.CookieManager
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class PersistentCookieStore(context: Context) : CookieStore {
    private val cookies: HashMap<String, ConcurrentHashMap<String, Cookie>> = HashMap()
    private val cookiePrefs: SharedPreferences = context.getSharedPreferences(COOKIE_PREFS, 0)
    private var omitNonPersistentCookies = false

    /** 移除失效cookie  */
    private fun clearExpired() {
        val prefsWriter = cookiePrefs.edit()
        for (key in cookies.keys) {
            var changeFlag = false
            val cookieMap = cookies.getValue(key)
            for ((name, cookie) in cookieMap) {
                if (isCookieExpired(cookie)) {
                    // Clear cookies from local store
                    cookieMap.remove(name)

                    // Clear cookies from persistent store
                    prefsWriter.remove(COOKIE_NAME_PREFIX + name)

                    // We've cleared at least one
                    changeFlag = true
                }
            }

            // Update names in persistent store
            if (changeFlag) {
                prefsWriter.putString(key, TextUtils.join(",", cookies.keys))
            }
        }
        prefsWriter.apply()
    }

    override fun add(httpUrl: HttpUrl, cookie: Cookie) {
        if (omitNonPersistentCookies && !cookie.persistent) {
            return
        }
        val name = cookieName(cookie)
        val hostKey = hostName(httpUrl)

        // Save cookie into local store, or remove if expired
        if (!cookies.containsKey(hostKey)) {
            cookies[hostKey] = ConcurrentHashMap()
        }
        cookies.getValue(hostKey)[name] = cookie

        // Save cookie into persistent store
        val prefsWriter = cookiePrefs.edit()
        // 保存httpUrl对应的所有cookie的name
        prefsWriter.putString(hostKey, TextUtils.join(",", cookies.getValue(hostKey).keys))
        // 保存cookie
        prefsWriter.putString(COOKIE_NAME_PREFIX + name, encodeCookie(SerializableCookie(cookie)))
        prefsWriter.apply()
    }

    override fun add(httpUrl: HttpUrl, cookies: List<Cookie>) {
        for (cookie in cookies) {
            if (isCookieExpired(cookie)) {
                continue
            }
            this.add(httpUrl, cookie)
        }
    }

    override fun get(httpUrl: HttpUrl): List<Cookie> {
        return getCookies(hostName(httpUrl))
    }

    override fun getCookies(): List<Cookie> {
        val result = ArrayList<Cookie>()
        for (hostKey in cookies.keys) {
            result.addAll(getCookies(hostKey))
        }
        return result
    }

    /** 获取cookie集合  */
    private fun getCookies(hostKey: String): List<Cookie> {
        val result = ArrayList<Cookie>()
        if (cookies.containsKey(hostKey)) {
            val cookies: Collection<Cookie> = cookies.getValue(hostKey).values
            for (cookie in cookies) {
                if (isCookieExpired(cookie)) {
                    this.remove(hostKey, cookie)
                } else {
                    result.add(cookie)
                }
            }
        }
        return result
    }

    override fun remove(httpUrl: HttpUrl, cookie: Cookie): Boolean {
        CookieManager()
        return this.remove(hostName(httpUrl), cookie)
    }

    /** 从缓存中移除cookie  */
    private fun remove(hostKey: String, cookie: Cookie): Boolean {
        val name = cookieName(cookie)
        if (cookies.containsKey(hostKey) && cookies.getValue(hostKey).containsKey(name)) {
            // 从内存中移除httpUrl对应的cookie
            cookies.getValue(hostKey).remove(name)
            val prefsWriter = cookiePrefs.edit()

            // 从本地缓存中移出对应cookie
            prefsWriter.remove(COOKIE_NAME_PREFIX + name)

            // 保存httpUrl对应的所有cookie的name
            prefsWriter.putString(hostKey, TextUtils.join(",", cookies.getValue(hostKey).keys))
            prefsWriter.apply()
            return true
        }
        return false
    }

    override fun removeAll(): Boolean {
        val prefsWriter = cookiePrefs.edit()
        prefsWriter.clear()
        prefsWriter.apply()
        cookies.clear()
        return true
    }

    fun setOmitNonPersistentCookies(omitNonPersistentCookies: Boolean) {
        this.omitNonPersistentCookies = omitNonPersistentCookies
    }

    /** 判断cookie是否失效   */
    private fun isCookieExpired(cookie: Cookie): Boolean {
        return cookie.expiresAt < System.currentTimeMillis()
    }

    private fun hostName(httpUrl: HttpUrl): String {
        return if (httpUrl.host.startsWith(HOST_NAME_PREFIX)) httpUrl.host else HOST_NAME_PREFIX + httpUrl.host
    }

    private fun cookieName(cookie: Cookie): String {
        return cookie.name + cookie.domain
    }

    private fun encodeCookie(cookie: SerializableCookie?): String? {
        if (cookie == null) return null
        val os = ByteArrayOutputStream()
        try {
            val outputStream = ObjectOutputStream(os)
            outputStream.writeObject(cookie)
        } catch (e: IOException) {
            Log.d(TAG, "IOException in encodeCookie", e)
            return null
        }
        return byteArrayToHexString(os.toByteArray())
    }

    private fun decodeCookie(cookieString: String): Cookie? {
        val bytes = hexStringToByteArray(cookieString)
        val byteArrayInputStream = ByteArrayInputStream(bytes)
        var cookie: Cookie? = null
        try {
            val objectInputStream = ObjectInputStream(byteArrayInputStream)
            cookie = (objectInputStream.readObject() as SerializableCookie).getCookie()
        } catch (e: IOException) {
            Log.d(TAG, "IOException in decodeCookie", e)
        } catch (e: ClassNotFoundException) {
            Log.d(TAG, "ClassNotFoundException in decodeCookie", e)
        }
        return cookie
    }

    private fun byteArrayToHexString(bytes: ByteArray): String {
        val sb = StringBuilder(bytes.size * 2)
        for (element in bytes) {
            val v: Int = element and 0xff
            if (v < 16) {
                sb.append('0')
            }
            sb.append(Integer.toHexString(v))
        }
        return sb.toString().toUpperCase(Locale.US)
    }

    private fun hexStringToByteArray(hexString: String): ByteArray {
        val len = hexString.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(
                hexString[i],
                16
            ) shl 4) + Character.digit(hexString[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    companion object {
        private const val TAG = "PersistentCookieStore"
        private const val COOKIE_PREFS = "CookiePrefsFile"
        private const val HOST_NAME_PREFIX = "host_"
        private const val COOKIE_NAME_PREFIX = "cookie_"
    }

    /** Construct a persistent cookie store.   */
    init {
        val tempCookieMap: MutableMap<Any, Any?> = HashMap(cookiePrefs.all)
        for (key in tempCookieMap.keys) {
            if (key !is String || !key.contains(HOST_NAME_PREFIX)) {
                continue
            }
            val cookieNames = tempCookieMap[key] as String? ?: continue
            if (!cookies.containsKey(key)) {
                cookies[key] = ConcurrentHashMap()
            }
            val cookieNameArr = cookieNames.split(",").toTypedArray()
            for (name in cookieNameArr) {
                val encodedCookie = cookiePrefs.getString("cookie_$name", null) ?: continue
                val decodedCookie = decodeCookie(encodedCookie)
                if (decodedCookie != null) {
                    cookies.getValue(key)[name] = decodedCookie
                }
            }
        }
        tempCookieMap.clear()
        clearExpired()
    }
}