package com.nice.kotlins.http

import com.nice.kotlins.helper.parseJson
import okhttp3.Response
import java.lang.reflect.Type

internal class OkTransformer<T> {

    private var responseMapper: OkMapper<Response, T>? = null
    private var errorMapper: OkMapper<Throwable, T>? = null

    fun mapResponse(mapper: OkMapper<Response, T>) {
        responseMapper = mapper
    }

    fun mapResponse(clazz: Class<T>) {
        responseMapper = OkMapperImpl(clazz)
    }

    fun mapResponse(type: Type) {
        responseMapper = OkMapperImpl(type)
    }

    fun mapError(mapper: OkMapper<Throwable, T>) {
        errorMapper = mapper
    }

    fun transformResponse(response: Response): T {
        return try {
            val mapper = requireNotNull(responseMapper) {
                "Response Mapper must not be null"
            }
            mapper.map(response)
        } catch (error: Throwable) {
            transformError(error)
        }
    }

    fun transformError(error: Throwable): T {
        return errorMapper?.map(error) ?: throw error
    }

    private class OkMapperImpl<T>(private val type: Type) : OkMapper<Response, T> {

        @Suppress("UNCHECKED_CAST")
        override fun map(value: Response): T = when (type) {
            Response::class.java -> value as T
            String::class.java -> value.body!!.string() as T
            else -> parseJson(value.body!!.string(), type)
        }

    }
}