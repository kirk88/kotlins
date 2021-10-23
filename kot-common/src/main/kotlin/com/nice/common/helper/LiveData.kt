@file:Suppress("UNUSED")

package com.nice.common.helper

import androidx.lifecycle.MutableLiveData

operator fun <T> MutableLiveData<T>.plusAssign(value: T?) {
    postValue(value)
}