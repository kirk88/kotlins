package com.nice.okfaker

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.Response
import okio.ByteString
import java.io.InputStream

typealias OkResponseMapper<T> = (Response) -> T
typealias OkErrorMapper<T> = (Throwable) -> T

@PublishedApi
internal val GSON: Gson = GsonBuilder()
    .serializeNulls()
    .setLenient()
    .create()

@PublishedApi
internal inline fun <reified T> typeMapper(): OkResponseMapper<T> = {
    val typeToken = object : TypeToken<T>() {}
    when (typeToken.rawType) {
        Response::class.java -> it as T
        String::class.java -> it.body!!.string() as T
        ByteArray::class.java -> it.body!!.bytes() as T
        ByteString::class.java -> it.body!!.byteString() as T
        InputStream::class.java -> it.body!!.byteStream() as T
        else -> GSON.fromJson(it.body!!.string(), typeToken.type)
    }
}