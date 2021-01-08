package com.easy.kotlins.http

/**
 * Create by LiZhanPing on 2020/4/29
 */
interface OkCallback<T> {
    fun onStart()
    fun onProgress(bytes: Long, totalBytes: Long)
    fun onSuccess(result: T)
    fun onError(error: Exception)
    fun onCancel()
    fun onComplete()
}