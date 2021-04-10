package com.nice.kotlins.event.livebus

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

interface Observable<T> {

    fun post(value: T?)

    fun postDelay(value: T?, delay: Long)

    fun postOrderly(value: T?)

    fun observe(owner: LifecycleOwner, observer: Observer<T>)

    fun observeSticky(owner: LifecycleOwner, observer: Observer<T>)

    fun observeActive(owner: LifecycleOwner, observer: Observer<T>)

    fun observeActiveSticky(owner: LifecycleOwner, observer: Observer<T>)

    fun observeForever(observer: Observer<T>)

    fun observeStickyForever(observer: Observer<T>)

    fun removeObserver(observer: Observer<T>)
}