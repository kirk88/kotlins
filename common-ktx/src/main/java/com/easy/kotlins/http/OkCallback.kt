package com.easy.kotlins.http

interface OkCallback<T> {

    fun onStart()

    fun onSuccess(result: T)

    fun onFailure(error: Exception)

    fun onCompletion()

    fun onCancel()

}