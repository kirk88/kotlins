package com.easy.kotlins.http.core

/**
 * Create by LiZhanPing on 2020/4/29
 */
interface OkCallback<T> {
    fun onStart()
    fun onSuccess(result: T)
    fun onError(error: OkException)
    fun onComplete()
}