@file:Suppress("unused")

package com.easy.kotlins.http

import com.easy.kotlins.http.extension.OkExtension
import com.easy.kotlins.http.extension.OkCommonExtension
import okhttp3.*
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

/**
 * Create by LiZhanPing on 2020/4/27
 */
class OkRestRequest<T>(private val method: OkRequestMethod) : OkRequest<T>() {

    private var formBuilderApplied = false
    private var formBuilder: FormBody.Builder? = null
        get() = field ?: FormBody.Builder().also { field = it }
        set(value) {
            field = value
            formBuilderApplied = true
            multipartBuilderApplied = false
        }

    private var multipartBuilderApplied = false
    private var multipartBuilder: MultipartBody.Builder? = null
        get() = field ?: MultipartBody.Builder().also { field = it }
        set(value) {
            field = value
            formBuilderApplied = false
            multipartBuilderApplied = true
        }

    private var requestBody: RequestBody? = null
        set(value) {
            field = value
            formBuilderApplied = false
            multipartBuilderApplied = false
        }

    private var extension: OkExtension? = null

    fun addFormParameter(key: String, value: String) {
        formBuilder?.add(key, value)
    }

    fun addEncodedFormParameter(key: String, value: String) {
        formBuilder?.addEncoded(key, value)
    }

    fun addFormDataPart(name: String, value: String) {
        multipartBuilder?.addFormDataPart(name, value)
    }

    fun addFormDataPart(name: String, filename: String?, body: RequestBody) {
        multipartBuilder?.addFormDataPart(name, filename, body)
    }

    fun addFormDataPart(name: String, contentType: MediaType?, file: File) {
        multipartBuilder?.addFormDataPart(
            name,
            file.name,
            file.asRequestBody(contentType)
        )
    }

    fun addPart(part: MultipartBody.Part) {
        multipartBuilder?.addPart(part)
    }

    fun addPart(body: RequestBody) {
        multipartBuilder?.addPart(body)
    }

    fun addPart(headers: Headers?, body: RequestBody) {
        multipartBuilder?.addPart(headers, body)
    }

    fun body(contentType: MediaType?, body: String) {
        requestBody = body.toRequestBody(contentType)
    }

    fun body(contentType: MediaType?, file: File) {
        requestBody = file.asRequestBody(contentType)
    }

    fun body(body: RequestBody) {
        requestBody = body
    }

    fun extension(extension: OkExtension) {
        this.extension = extension
    }

    override fun createRealRequest(): Request {
        val body: RequestBody by lazy {
            val formBody = if (formBuilderApplied) formBuilder?.build() else null
            val multipartBody = if (multipartBuilderApplied) multipartBuilder?.build() else null
            OkRequestBody(
                requestBody ?: formBody ?: multipartBody ?: FormBody.Builder().build()
            ) { bytes, totalBytes ->
                dispatchOnProgress(bytes, totalBytes)
            }
        }
        val request = requestBuilder.url(urlBuilder.build()).also {
            when (method) {
                OkRequestMethod.GET -> it.get()
                OkRequestMethod.POST -> it.post(body)
                OkRequestMethod.DELETE -> it.delete(body)
                OkRequestMethod.PUT -> it.put(body)
                OkRequestMethod.HEAD -> it.head()
                OkRequestMethod.PATCH -> it.patch(body)
            }
        }.build()
        return extension?.shouldInterceptRequest(request) ?: request
    }

    override fun onFailure(error: Exception): Boolean {
        if ((extension as? OkCommonExtension)?.onError(error) == true) return true
        return false
    }

    override fun onResponse(response: Response): Boolean {
        if ((extension as? OkCommonExtension)?.onResponse(response) == true) return true
        return false
    }

}