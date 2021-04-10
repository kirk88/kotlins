package com.nice.kotlins.http

internal interface OkCallback<T> {

    fun onStart()

    fun onSuccess(result: T)

    fun onFailure(error: Throwable)

    fun onCompletion()

    fun onCancel()

}