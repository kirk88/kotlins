@file:Suppress("unused")

package com.nice.kotlins.helper

import androidx.lifecycle.LifecycleOwner
import com.nice.kotlins.event.LiveEvent
import com.nice.kotlins.event.MutableLiveEvent


inline fun <T> LiveEvent<T>.observeNotNull(
    owner: LifecycleOwner,
    crossinline observer: (value: T) -> Unit,
) {
    observe(owner) {
        if (it != null) {
            observer(it)
        }
    }
}

operator fun <T> MutableLiveEvent<T>.plusAssign(value: T) {
    postValue(value)
}