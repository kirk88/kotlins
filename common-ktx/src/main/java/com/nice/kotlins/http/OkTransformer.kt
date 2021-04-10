package com.nice.kotlins.http

import okhttp3.Response

internal class OkTransformer<T> {

    private var responseMapper: OkMapper<Response, T>? = null
    private var errorMapper: OkMapper<Throwable, T>? = null

    fun mapResponse(mapper: OkMapper<Response, T>) = apply {
        responseMapper = mapper
    }

    fun mapError(mapper: OkMapper<Throwable, T>) = apply {
        errorMapper = mapper
    }

    fun transformResponse(response: Response): T {
        val mapper = responseMapper ?: DEFAULT_RESPONSE_MAPPER
        return try {
            @Suppress("UNCHECKED_CAST")
            mapper.map(response) as T
        } catch (error: Throwable) {
            transformError(error)
        }
    }

    fun transformError(error: Throwable): T {
        return errorMapper?.map(error) ?: throw error
    }

    companion object {
        private val DEFAULT_RESPONSE_MAPPER = OkMapper<Response, Any> { it }
    }

}