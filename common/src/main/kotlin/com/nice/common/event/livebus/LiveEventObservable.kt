package com.nice.common.event.livebus

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

internal class LiveEventObservable<T> : Observable<T> {

    private val delegate = LiveEventDelegate<T>()

    override fun post(value: T?) {
        delegate.post(value)
    }

    override fun postDelay(value: T?, delay: Long) {
        delegate.postDelay(value, delay)
    }

    override fun postOrderly(value: T?) {
        delegate.postOrderly(value)
    }

    override fun observe(owner: LifecycleOwner, observer: Observer<T>) {
        delegate.observe(owner, observer)
    }

    override fun observeSticky(owner: LifecycleOwner, observer: Observer<T>) {
        delegate.observeSticky(owner, observer)
    }

    override fun observeActive(owner: LifecycleOwner, observer: Observer<T>) {
        delegate.observeActive(owner, observer)
    }

    override fun observeActiveSticky(owner: LifecycleOwner, observer: Observer<T>) {
        delegate.observeActiveSticky(owner, observer)
    }

    override fun observeForever(observer: Observer<T>) {
        delegate.observeForever(observer)
    }

    override fun observeStickyForever(observer: Observer<T>) {
        delegate.observeStickyForever(observer)
    }

    override fun removeObserver(observer: Observer<T>) {
        delegate.removeObserver(observer)
    }

}
