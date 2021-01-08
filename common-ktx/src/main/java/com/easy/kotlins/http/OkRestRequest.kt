@file:Suppress("unused")

package com.easy.kotlins.http

import com.easy.kotlins.http.extension.OkExtension
import com.easy.kotlins.http.extension.OkRestExtension
import okhttp3.*
import java.io.File

/**
 * Create by LiZhanPing on 2020/4/27
 */
class OkRestRequest<T>(private val method: OkRequestMethod) : OkRequest<T>() {

    private var requestBody: RequestBody? = null
        set(value) {
            field = value
            formBuilder = null
            multiBuilder = null
        }

    private var formBuilder: FormBody.Builder? = null
        get() = if (field == null) {
            FormBody.Builder().also {
                field = it
                multiBuilder = null
                requestBody = null
            }
        } else field

    private var multiBuilder: MultipartBody.Builder? = null
        get() = if (field == null) {
            MultipartBody.Builder().also {
                field = it
                formBuilder = null
                requestBody = null
            }
        } else field

    private var extension: OkExtension? = null

    fun body(mediaType: MediaType?, body: String) {
        requestBody = RequestBody.create(mediaType, body)
    }

    fun body(mediaType: MediaType?, file: File) {
        requestBody = RequestBody.create(mediaType, file)
    }

    fun body(body: RequestBody) {
        requestBody = body
    }

    fun addFormParameter(key: String, value: String) {
        formBuilder?.add(key, value)
    }

    fun addEncodedFormParameter(key: String, value: String) {
        formBuilder?.addEncoded(key, value)
    }

    fun addPart(part: MultipartBody.Part) {
        multiBuilder?.addPart(part)
    }

    fun addPart(body: RequestBody) {
        multiBuilder?.addPart(body)
    }

    fun addFormDataPart(name: String, value: String) {
        multiBuilder?.addFormDataPart(name, value)
    }

    fun addFormDataPart(name: String, filename: String?, body: RequestBody) {
        multiBuilder?.addFormDataPart(name, filename, body)
    }

    fun addFormDataPart(name: String, type: MediaType?, file: File) {
        multiBuilder?.addFormDataPart(
            name,
            file.name,
            RequestBody.create(type, file)
        )
    }

    fun extension(extension: OkExtension) {
        this.extension = extension
    }

    override fun createRealRequest(): Request {
        val body = OkRequestBody(
            requestBody ?: multiBuilder?.build() ?: formBuilder?.build() ?: FormBody.Builder()
                .build()
        ) { bytes, totalBytes ->
            callOnProgress(bytes, totalBytes)
        }
        val request = requestBuilder.url(urlBuilder.build()).let {
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

    override fun onFailure(exception: Exception): Boolean {
        if ((extension as? OkRestExtension)?.onError(exception) == true) return true
        return false
    }

    override fun onResponse(response: Response): Boolean {
        if ((extension as? OkRestExtension)?.onResponse(response) == true) return true
        return false
    }

}