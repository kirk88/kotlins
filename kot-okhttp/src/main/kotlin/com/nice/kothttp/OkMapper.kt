package com.nice.kothttp

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Response
import okio.ByteString
import java.io.InputStream
import kotlin.coroutines.resume

fun interface OkResponseMapper<T> {
    suspend fun map(response: Response): T
}

fun interface OkErrorMapper<T> {
    suspend fun map(error: Throwable): T
}

suspend operator fun <T> OkResponseMapper<T>.invoke(response: Response) = map(response)

suspend operator fun <T> OkErrorMapper<T>.invoke(error: Throwable) = map(error)

@PublishedApi
internal val GSON: Gson = GsonBuilder()
    .serializeNulls()
    .setLenient()
    .create()

@PublishedApi
internal inline fun <reified T> typeMapper(): OkResponseMapper<T> = object : OkResponseMapper<T> {

    override suspend fun map(response: Response): T {
        if (T::class == Response::class) {
            return response as T
        }
        val body = requireNotNull(response.body) { "ResponseBody is null" }
        val token = object : TypeToken<T>() {}
        return suspendCancellableCoroutine { con ->
            val result = when (token.rawType) {
                String::class.java -> body.string() as T
                ByteArray::class.java -> body.bytes() as T
                ByteString::class.java -> body.byteString() as T
                InputStream::class.java -> body.byteStream() as T
                else -> GSON.fromJson(body.string(), token.type)
            }
            con.resume(result)
        }
    }

}