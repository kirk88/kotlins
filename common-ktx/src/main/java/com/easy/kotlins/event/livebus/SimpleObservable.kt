package com.easy.kotlins.event.livebus

import androidx.lifecycle.LifecycleOwner

interface SimpleObservable {
    fun call()

    fun callDelay(delay: Long)

    fun callOrderly()

    fun observe(owner: LifecycleOwner, observer: SimpleObserver)

    fun observeSticky(owner: LifecycleOwner, observer: SimpleObserver)

    fun observeActive(owner: LifecycleOwner, observer: SimpleObserver)

    fun observeActiveSticky(owner: LifecycleOwner, observer: SimpleObserver)

    fun observeForever(observer: SimpleObserver)

    fun observeStickyForever(observer: SimpleObserver)

    fun removeObserver(observer: SimpleObserver)
}