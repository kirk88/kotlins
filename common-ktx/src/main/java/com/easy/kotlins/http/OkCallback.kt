package com.easy.kotlins.http

interface OkCallback<T> {

    fun onStart()

    fun onSuccess(result: T)

    fun onFailure(exception: Throwable)

    fun onCompletion()

    fun onCancel()

}