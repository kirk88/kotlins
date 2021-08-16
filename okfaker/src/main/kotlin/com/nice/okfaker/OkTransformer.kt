package com.nice.okfaker

import okhttp3.Response

internal class OkTransformer<T> {

    private var responseMapper: OkResponseMapper<T>? = null
    private var errorMapper: OkErrorMapper<T>? = null

    fun mapResponse(mapper: OkResponseMapper<T>) {
        responseMapper = mapper
    }

    fun mapError(mapper: OkErrorMapper<T>) {
        errorMapper = mapper
    }

    suspend fun transformResponse(response: Response): T {
        return try {
            requireNotNull(responseMapper) {
                "OkResponseMapper must not be null"
            }.invoke(response)
        } catch (error: Throwable) {
            transformError(error)
        }
    }

    suspend fun transformError(error: Throwable): T {
        return errorMapper?.invoke(error) ?: throw error
    }

}