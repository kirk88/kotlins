@file:Suppress("unused")

package com.nice.kotlins.helper

import com.nice.kotlins.event.MutableLiveEvent

operator fun <T> MutableLiveEvent<T>.plusAssign(value: T) {
    postValue(value)
}