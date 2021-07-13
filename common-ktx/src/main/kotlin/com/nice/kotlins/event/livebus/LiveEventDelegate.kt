package com.nice.kotlins.event.livebus

import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.nice.kotlins.event.MutableLiveEvent
import com.nice.kotlins.helper.isMainThread

internal class LiveEventDelegate<T> {

    private val liveData = MutableLiveEvent<T>()

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

    fun observeActive(owner: LifecycleOwner, observer: Observer<T>) {
        if (isMainThread) {
            observeActiveInternal(owner, observer)
        } else {
            HANDLER.post { observeActiveInternal(owner, observer) }
        }
    }

    fun observeActiveSticky(
            owner: LifecycleOwner,
            observer: Observer<T>
    ) {
        if (isMainThread) {
            observeActiveStickyInternal(owner, observer)
        } else {
            HANDLER.post { observeActiveStickyInternal(owner, observer) }
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
        liveData.setValue(value)
    }

    @MainThread
    private fun observeInternal(owner: LifecycleOwner, observer: Observer<T>) {
        liveData.observe(owner, observer)
    }

    @MainThread
    private fun observeStickyInternal(owner: LifecycleOwner, observer: Observer<T>) {
        liveData.observeSticky(owner, observer)
    }

    @MainThread
    private fun observeActiveInternal(owner: LifecycleOwner, observer: Observer<T>) {
        liveData.observeActive(owner, observer)
    }

    @MainThread
    private fun observeActiveStickyInternal(owner: LifecycleOwner, observer: Observer<T>) {
        liveData.observeActiveSticky(owner, observer)
    }

    @MainThread
    private fun observeForeverInternal(observer: Observer<T>) {
        liveData.observeForever(observer)
    }

    @MainThread
    private fun observeStickyForeverInternal(observer: Observer<T>) {
        liveData.observeForever(observer)
    }

    @MainThread
    private fun removeObserverInternal(observer: Observer<T>) {
        liveData.removeObserver(observer)
    }

    companion object {
        private val HANDLER = Handler(Looper.getMainLooper())
    }

}