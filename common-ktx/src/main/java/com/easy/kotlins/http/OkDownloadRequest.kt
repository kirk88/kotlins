@file:Suppress("unused")

package com.easy.kotlins.http

import com.easy.kotlins.http.extension.OkDownloadExtension
import okhttp3.Request
import okhttp3.Response
import java.io.File

class OkDownloadRequest : OkRequest<File>() {

    private var extension: OkDownloadExtension? = null

    fun extension(extension: OkDownloadExtension) {
        this.extension = extension
    }

    override fun createRealRequest(): Request {
        val request = requestBuilder.url(urlBuilder.build()).get().build()
        return extension?.shouldInterceptRequest(request) ?: request
    }

    override fun onResponse(response: Response): Boolean {
        val downloadExtension = extension ?: return false

        val file: File = downloadExtension.onResponse(response) { bytes, totalBytes ->
            callOnProgress(bytes, totalBytes)
        }

        callOnSuccess(file)
        return true
    }

    override fun mapResponse(response: Response, responseMapper: OkMapper<Response, File>?): File {
        responseMapper ?: throw NullPointerException("ResponseMapper is null")
        return responseMapper.map(response)
    }

    override fun mapError(exception: Exception, errorMapper: OkMapper<Exception, File>?): File {
        errorMapper ?: throw NullPointerException("ErrorMapper is null")
        return errorMapper.map(exception)
    }

}