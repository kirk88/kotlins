package com.nice.kothttp

import okhttp3.*
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class HeadersBuilder internal constructor(private val builder: Request.Builder) {

    fun add(name: String, value: Any?) {
        builder.addHeader(name, value.toStringOrEmpty())
    }

    fun set(name: String, value: Any?) {
        builder.header(name, value.toStringOrEmpty())
    }

    fun remove(name: String) {
        builder.removeHeader(name)
    }

    operator fun String.plusAssign(value: Any?) = add(this, value)

    operator fun String.unaryMinus() = remove(this)

}

class QueryParametersBuilder internal constructor(private val builder: HttpUrl.Builder) {

    fun add(name: String, value: Any?) {
        builder.addQueryParameter(name, value.toStringOrEmpty())
    }

    fun addEncoded(name: String, value: Any?) {
        builder.addEncodedQueryParameter(name, value.toStringOrEmpty())
    }

    fun set(name: String, value: Any?) {
        builder.setQueryParameter(name, value.toStringOrEmpty())
    }

    fun setEncoded(name: String, value: Any?) {
        builder.setEncodedQueryParameter(name, value.toStringOrEmpty())
    }

    fun remove(name: String) {
        builder.removeAllQueryParameters(name)
    }

    fun removeEncoded(name: String) {
        builder.removeAllEncodedQueryParameters(name)
    }

    operator fun String.plusAssign(value: Any?) = add(this, value)

    operator fun String.unaryMinus() = remove(this)

}

class FormParametersBuilder internal constructor(private val builder: FormBody.Builder) {

    fun add(name: String, value: Any?) {
        builder.add(name, value.toStringOrEmpty())
    }

    fun addEncoded(name: String, value: Any?) {
        builder.addEncoded(name, value.toStringOrEmpty())
    }

    operator fun String.plusAssign(value: Any?) = add(this, value)

}

class MultipartBodyBuilder internal constructor(private val builder: MultipartBody.Builder) {

    fun add(name: String, value: Any?) {
        if (value is File) {
            builder.addFormDataPart(name, value.name, value.asRequestBody())
        } else {
            builder.addFormDataPart(name, value.toStringOrEmpty())
        }
    }

    fun add(name: String, filename: String?, body: RequestBody) {
        builder.addFormDataPart(name, filename, body)
    }

    fun add(part: MultipartBody.Part) {
        builder.addPart(part)
    }

    fun add(body: RequestBody) {
        builder.addPart(body)
    }

    operator fun String.plusAssign(value: Any?) = add(this, value)

}

private fun Any?.toStringOrEmpty() = (this ?: "").toString()