package com.easy.kotlins.http


/**
 * Create by LiZhanPing on 2020/4/29
 */
internal sealed class OkResult<T> {

    data class Success<T>(val data: T) : OkResult<T>()
    data class Error(val exception: Exception) : OkResult<Nothing>()

}