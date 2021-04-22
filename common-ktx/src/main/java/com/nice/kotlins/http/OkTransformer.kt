package com.nice.kotlins.http

import com.nice.kotlins.helper.parseJson
import okhttp3.OkHttpClient
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

    private inner class OkMapperImpl(private val type: Type) : OkMapper<Response, T> {

        @Suppress("UNCHECKED_CAST")
        override fun map(value: Response): T = when (type) {
            Response::class.java -> value as T
            String::class.java -> value.body!!.string() as T
            else -> parseJson(value.body!!.string(), type)
        }

    }
}

data class Test(
    val code: Int,
    val data: Data,
    val msg: String
) {

    data class Data(
        val id: String,
        val username: String,
        val email: String,
        val devices: String?
    )

}

fun main() {

    OkFaker.configSetter().client {
        OkHttpClient()
    }.baseUrl("http://101.132.255.180:8888")
        .apply()

    val result =
        OkFaker.get<Test>().client(OkHttpClient()).url("/user/login")
            .headers {
                "username" and "1231"
            }
            .queryParameters {
                "username" and 1
                "password" and 1
            }.execute()

    println(result)

}