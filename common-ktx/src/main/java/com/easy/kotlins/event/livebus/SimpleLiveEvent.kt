package com.easy.kotlins.event.livebus

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

internal class SimpleLiveEvent : SimpleObservable {

    private val delegate = LiveEventDelegate<Nothing>()
    private val observers = mutableMapOf<SimpleObserver, Observer<Nothing>>()

    override fun call() {
        delegate.post(null)
    }

    override fun callDelay(delay: Long) {
        delegate.postDelay(null, delay)
    }

    override fun callOrderly() {
        delegate.postOrderly(null)
    }

    override fun observe(owner: LifecycleOwner, observer: SimpleObserver) {
        delegate.observe(owner, ObserverWrapper(observer).also {
            observers[observer] = it
        })
    }

    override fun observeSticky(owner: LifecycleOwner, observer: SimpleObserver) {
        delegate.observeSticky(owner, ObserverWrapper(observer).also {
            observers[observer] = it
        })
    }

    override fun observeActive(owner: LifecycleOwner, observer: SimpleObserver) {
        delegate.observeActive(owner, ObserverWrapper(observer).also {
            observers[observer] = it
        })
    }

    override fun observeActiveSticky(owner: LifecycleOwner, observer: SimpleObserver) {
        delegate.observeActive(owner, ObserverWrapper(observer).also {
            observers[observer] = it
        })
    }

    override fun observeForever(observer: SimpleObserver) {
        delegate.observeForever(ObserverWrapper(observer).also {
            observers[observer] = it
        })
    }

    override fun observeStickyForever(observer: SimpleObserver) {
        delegate.observeStickyForever(ObserverWrapper(observer).also {
            observers[observer] = it
        })
    }

    override fun removeObserver(observer: SimpleObserver) {
        observers[observer]?.let { delegate.removeObserver(it) }
    }

    private class ObserverWrapper(private val observer: SimpleObserver) : Observer<Nothing> {

        override fun onChanged(t: Nothing?) {
            observer.onChanged()
        }
    }
}