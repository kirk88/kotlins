package com.nice.common.http

internal interface OkCallback<T> {

    fun onStart()

    fun onSuccess(result: T)

    fun onError(error: Throwable)

    fun onCompletion()

    fun onCancel()

}