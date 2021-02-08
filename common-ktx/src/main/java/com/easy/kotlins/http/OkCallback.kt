package com.easy.kotlins.http

interface OkCallback<T> {

    fun onStart()

    fun onSuccess(result: T)

    fun onError(error: Exception)

    fun onComplete()

    fun onCancel()

}