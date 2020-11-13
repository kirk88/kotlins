package com.easy.kotlins.http.core


/**
 * Create by LiZhanPing on 2020/4/29
 */
sealed class OkResult<T> {

    data class Success<T>(val data: T) : OkResult<T>()
    data class Error(val exception: Exception) : OkResult<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
        }
    }
}