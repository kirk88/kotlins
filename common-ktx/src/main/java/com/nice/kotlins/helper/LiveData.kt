@file:Suppress("unused")

package com.nice.kotlins.helper

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

inline fun <T> LiveData<T>.observeNotNull(
    owner: LifecycleOwner,
    crossinline observer: (value: T) -> Unit,
) {
    observe(owner) {
        if (it != null) {
            observer(it)
        }
    }
}

operator fun <T> MutableLiveData<T>.plusAssign(value: T?) {
    if (isMainThread) {
        this.value = value
    } else {
        this.postValue(value)
    }
}