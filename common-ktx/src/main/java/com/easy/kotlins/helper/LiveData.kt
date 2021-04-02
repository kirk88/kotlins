@file:Suppress("unused")

package com.easy.kotlins.helper

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

fun <T> LiveData<T>.observeNotNull(owner: LifecycleOwner, observer: (value: T) -> Unit){
    observe(owner){
        if(it != null){
            observer(it)
        }
    }
}

var <T> MutableLiveData<T>.valueOnMain
    get() = value
    set(value) {
        postValue(value)
    }