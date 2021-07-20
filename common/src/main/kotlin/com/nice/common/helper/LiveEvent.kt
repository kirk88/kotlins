@file:Suppress("unused")

package com.nice.common.helper

import com.nice.common.event.MutableLiveEvent

operator fun <T> MutableLiveEvent<T>.plusAssign(value: T) {
    postValue(value)
}