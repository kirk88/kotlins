package com.easy.kotlins.event.livebus

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

internal class SimpleLiveEvent : SimpleObservable {

    private val delegate =
        LiveEventDelegate<Nothing>()

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
        delegate.observe(owner, ObserverWrapper(observer))
    }

    override fun observeSticky(owner: LifecycleOwner, observer: SimpleObserver) {
        delegate.observeSticky(owner, ObserverWrapper(observer))
    }

    override fun observeForever(observer: SimpleObserver) {
        delegate.observeForever(ObserverWrapper(observer))
    }

    override fun observeStickyForever(observer: SimpleObserver) {
        delegate.observeStickyForever(ObserverWrapper(observer))
    }

    override fun removeObserver(observer: SimpleObserver) {
        delegate.removeObserver(observer as ObserverWrapper)
    }

    private class ObserverWrapper(private val observer: SimpleObserver) : Observer<Nothing>,
        SimpleObserver {

        override fun onChanged(t: Nothing?) {
            onChanged()
        }

        override fun onChanged() {
            observer.onChanged()
        }
    }
}