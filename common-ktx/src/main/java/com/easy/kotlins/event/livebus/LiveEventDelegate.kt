package com.easy.kotlins.event.livebus

import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import java.util.*

internal class LiveEventDelegate<T> {
    private val liveData = LiveEventData<T>()
    private val observerMap: MutableMap<Observer<*>, ObserverWrapper<T>> = HashMap()
  
    fun post(value: T?) {
        if (isMainThread) {
            postInternal(value)
        } else {
            HANDLER.post { postInternal(value) }
        }
    }

    fun postDelay(value: T?, delay: Long) {
        HANDLER.postDelayed({ postInternal(value) }, delay)
    }

    fun postOrderly(value: T?) {
        HANDLER.post { postInternal(value) }
    }

    fun observe(owner: LifecycleOwner, observer: Observer<T>) {
        if (isMainThread) {
            observeInternal(owner, observer)
        } else {
            HANDLER.post { observeInternal(owner, observer) }
        }
    }

    fun observeSticky(
        owner: LifecycleOwner,
        observer: Observer<T>
    ) {
        if (isMainThread) {
            observeStickyInternal(owner, observer)
        } else {
            HANDLER.post { observeStickyInternal(owner, observer) }
        }
    }

    fun observeForever(observer: Observer<T>) {
        if (isMainThread) {
            observeForeverInternal(observer)
        } else {
            HANDLER.post { observeForeverInternal(observer) }
        }
    }

    fun observeStickyForever(observer: Observer<T>) {
        if (isMainThread) {
            observeStickyForeverInternal(observer)
        } else {
            HANDLER.post { observeStickyForeverInternal(observer) }
        }
    }

    fun removeObserver(observer: Observer<T>) {
        if (isMainThread) {
            removeObserverInternal(observer)
        } else {
            HANDLER.post { removeObserverInternal(observer) }
        }
    }

    @MainThread
    private fun postInternal(value: T?) {
        liveData.value = value
    }

    @MainThread
    private fun observeInternal(owner: LifecycleOwner, observer: Observer<T>) {
        val observerWrapper = ObserverWrapper(observer)
        observerWrapper.preventNextEvent = liveData.version > LiveEventData.START_VERSION
        liveData.observe(owner, observerWrapper)
    }

    @MainThread
    private fun observeStickyInternal(owner: LifecycleOwner, observer: Observer<T>) {
        liveData.observe(owner, ObserverWrapper(observer))
    }

    @MainThread
    private fun observeForeverInternal(observer: Observer<T>) {
        val observerWrapper = ObserverWrapper(observer)
        observerWrapper.preventNextEvent = liveData.version > LiveEventData.START_VERSION
        observerMap[observer] = observerWrapper
        liveData.observeForever(observerWrapper)
    }

    @MainThread
    private fun observeStickyForeverInternal(observer: Observer<T>) {
        val observerWrapper = ObserverWrapper(observer)
        observerMap[observer] = observerWrapper
        liveData.observeForever(observerWrapper)
    }

    @MainThread
    private fun removeObserverInternal(observer: Observer<T>) {
        if (observerMap.containsKey(observer)) {
            observerMap.remove(observer)?.let { liveData.removeObserver(it) }
        }
    }

    private class ObserverWrapper<T>(private val observer: Observer<T>) :
        Observer<T> {
        var preventNextEvent = false
        override fun onChanged(t: T) {
            if (preventNextEvent) {
                preventNextEvent = false
                return
            }
            observer.onChanged(t)
        }
    }

    companion object {
        private val HANDLER = Handler(Looper.getMainLooper())
        private val isMainThread: Boolean
            get() = Looper.getMainLooper().thread === Thread.currentThread()
    }
}