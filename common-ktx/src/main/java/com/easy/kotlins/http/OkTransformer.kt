package com.easy.kotlins.http

import okhttp3.Response

internal class OkTransformer<T> {

    private var responseMapper: OkMapper<Response, T>? = null
    private var errorMapper: OkMapper<Exception, T>? = null

    fun mapResponse(mapper: OkMapper<Response, T>) = this.apply {
        responseMapper = mapper
    }

    fun mapError(mapper: OkMapper<Exception, T>) = this.apply {
        errorMapper = mapper
    }

    fun transformResponse(response: Response): T {
        val mapper = responseMapper ?: DEFAULT_RESPONSE_MAPPER
        return try {
            @Suppress("UNCHECKED_CAST")
            mapper.map(response) as T
        } catch (e: Exception) {
            transformError(e)
        }
    }

    fun transformError(error: Exception): T {
        return errorMapper?.map(error) ?: throw error
    }

    companion object {
        private val DEFAULT_RESPONSE_MAPPER = OkMapper<Response, Any> { it }
    }

}