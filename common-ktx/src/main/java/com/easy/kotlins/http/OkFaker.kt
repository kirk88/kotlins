@file:Suppress("unused")

package com.easy.kotlins.http

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.easy.kotlins.helper.forEach
import com.easy.kotlins.helper.toJSON
import com.easy.kotlins.helper.toJSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import kotlin.collections.set
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Create by LiZhanPing on 2020/8/22
 */

class OkFaker<T>(method: OkRequestMethod) : OkManager<T, OkRestRequest<T>>(OkRestRequest(method)) {

    fun formParameters(operation: RequestPairs<Any?>.() -> Unit) {
        RequestPairs<Any?>().apply(operation).forEach {
            request.addFormParameter(it.key, it.value.toString())
        }
    }

    fun formParameters(formParameters: Map<String, Any?>) {
        formParameters.forEach {
            request.addFormParameter(it.key, it.value.toString())
        }
    }

    fun formParameters(vararg formParameters: Pair<String, Any?>) {
        formParameters.forEach {
            request.addFormParameter(it.first, it.second.toString())
        }
    }

    fun encodedFormParameters(operation: RequestPairs<Any?>.() -> Unit) {
        RequestPairs<Any?>().apply(operation).forEach {
            request.addEncodedFormParameter(it.key, it.value.toString())
        }
    }

    fun encodedFormParameters(encodedFormParameters: Map<String, Any?>) {
        encodedFormParameters.forEach {
            request.addEncodedFormParameter(it.key, it.value.toString())
        }
    }

    fun encodedFormParameters(vararg encodedFormParameters: Pair<String, Any?>) {
        encodedFormParameters.forEach {
            request.addEncodedFormParameter(it.first, it.second.toString())
        }
    }

    fun parts(vararg parts: BodyPart) {
        parts.forEach {
            request.addPart(it.headers, it.body)
        }
    }

    fun formDataParts(operation: RequestPairs<Any?>.() -> Unit) {
        RequestPairs<Any?>().apply(operation).forEach {
            request.addFormDataPart(it.key, it.value.toString())
        }
    }

    fun formDataParts(formDataParts: Map<String, Any?>) {
        formDataParts.forEach {
            it.value.let { value ->
                when (value) {
                    is BodyFromDataPart -> request.addFormDataPart(
                        it.key,
                        value.filename,
                        value.body
                    )
                    is FileFormDataPart -> request.addFormDataPart(
                        it.key,
                        value.contentType,
                        value.file
                    )
                    else -> request.addFormDataPart(it.key, value.toString())
                }
            }
        }
    }

    fun formDataParts(vararg formDataParts: Pair<String, Any?>) {
        formDataParts.forEach {
            it.second.let { value ->
                when (value) {
                    is BodyFromDataPart -> request.addFormDataPart(
                        it.first,
                        value.filename,
                        value.body
                    )
                    is FileFormDataPart -> request.addFormDataPart(
                        it.first,
                        value.contentType,
                        value.file
                    )
                    else -> request.addFormDataPart(it.first, value.toString())
                }
            }
        }
    }

    fun parts(operation: ArrayList<RequestBody>.() -> Unit) {
        arrayListOf<RequestBody>().apply(operation).forEach {
            request.addPart(it)
        }
    }

    fun multiParts(operation: ArrayList<MultipartBody.Part>.() -> Unit) {
        arrayListOf<MultipartBody.Part>().apply(operation).forEach {
            request.addPart(it)
        }
    }

    fun body(body: RequestBody) {
        request.body(body)
    }

    fun body(body: () -> RequestBody) {
        request.body(body())
    }

    companion object {

        fun <T> get(block: OkFaker<T>.() -> Unit): OkFaker<T> =
            OkFaker<T>(OkRequestMethod.GET).apply(block)

        fun <T> post(block: OkFaker<T>.() -> Unit): OkFaker<T> =
            OkFaker<T>(OkRequestMethod.POST).apply(block)

        fun <T> delete(block: OkFaker<T>.() -> Unit): OkFaker<T> =
            OkFaker<T>(OkRequestMethod.DELETE).apply(block)

        fun <T> put(block: OkFaker<T>.() -> Unit): OkFaker<T> =
            OkFaker<T>(OkRequestMethod.PUT).apply(block)

        fun <T> head(block: OkFaker<T>.() -> Unit): OkFaker<T> =
            OkFaker<T>(OkRequestMethod.HEAD).apply(block)

        fun <T> patch(block: OkFaker<T>.() -> Unit): OkFaker<T> =
            OkFaker<T>(OkRequestMethod.PATCH).apply(block)
    }

}

data class BodyPart(val body: RequestBody, val headers: Headers? = null)
data class BodyFromDataPart(val body: RequestBody, val filename: String? = null)
data class FileFormDataPart(val file: File, val contentType: MediaType? = null)

class RequestPairs<T> : Iterable<Map.Entry<String, T>> {

    private val pairs: MutableMap<String, T> = mutableMapOf()

    infix fun String.to(value: T) {
        pairs[this] = value
    }

    fun put(key: String, value: T) {
        pairs[key] = value
    }

    fun putAll(pairsFrom: Map<String, T>) {
        pairs.putAll(pairsFrom)
    }

    fun remove(key: String): T? {
        return pairs.remove(key)
    }

    override fun toString(): String {
        return pairs.toJSON()
    }

    override fun iterator(): Iterator<Map.Entry<String, T>> {
        return pairs.iterator()
    }
}

inline fun requestPairsOf(crossinline operation: RequestPairs<Any?>.() -> Unit): RequestPairs<Any?> {
    return RequestPairs<Any?>().apply(operation)
}

fun requestPairsOf(
    vararg pairs: Pair<String, Any?>,
    operation: (RequestPairs<Any?>.() -> Unit)? = null
): RequestPairs<Any?> {
    return RequestPairs<Any?>().apply { putAll(pairs.toMap()) }.also {
        operation?.invoke(it)
    }
}

fun requestPairsOf(
    copyFrom: Any,
    serializeNulls: Boolean = false,
    operation: (RequestPairs<Any?>.() -> Unit)? = null
): RequestPairs<Any?> {
    return RequestPairs<Any?>().apply {
        val source: String =
            if (copyFrom is RequestPairs<*>) copyFrom.toString() else copyFrom.toJSON()
        source.toJSONObject().forEach { key, element ->
            when {
                element.isJSONNull && serializeNulls -> put(key, element.asString())
                else -> put(key, element.asString())
            }
        }
    }.also { operation?.invoke(it) }
}

fun <T : Any> OkFaker<T>.asFlow(): Flow<T> = flow {
    emit(execute())
}.flowOn(Dispatchers.IO)

fun <T : Any> OkFaker<T>.asLiveData(
    context: CoroutineContext = EmptyCoroutineContext,
    timeoutInMillis: Long = 5000L
): LiveData<T> = liveData(context, timeoutInMillis) {
    emit(execute())
}