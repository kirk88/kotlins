@file:Suppress("unused")

package com.nice.common.event

import android.annotation.SuppressLint
import androidx.annotation.CallSuper
import androidx.annotation.MainThread
import androidx.arch.core.internal.SafeIterableMap
import androidx.lifecycle.Observer

@SuppressLint("RestrictedApi")
class MediatorLiveEvent<T> : MutableLiveEvent<T>() {
    private val sources = SafeIterableMap<LiveEvent<*>, Source<*>>()

    @MainThread
    fun <S> addSource(source: LiveEvent<S>, onChanged: Observer<in S>) {
        val e = Source(source, onChanged)
        val existing = sources.putIfAbsent(source, e)
        require(!(existing != null && existing.observer !== onChanged)) { "This source was already added with the different observer" }
        if (existing != null) {
            return
        }
        if (hasActiveObservers()) {
            e.plug()
        }
    }

    @MainThread
    fun <S> removeSource(toRemote: LiveEvent<S>) {
        val source = sources.remove(toRemote)
        source?.unplug()
    }

    @CallSuper
    override fun onActive() {
        for ((_, value) in sources) {
            value.plug()
        }
    }

    @CallSuper
    override fun onInactive() {
        for ((_, value) in sources) {
            value.unplug()
        }
    }

    private class Source<V>(
        private val liveEvent: LiveEvent<V>,
        val observer: Observer<in V>
    ) : Observer<V> {

        private var version = START_VERSION

        fun plug() {
            liveEvent.observeForever(this)
        }

        fun unplug() {
            liveEvent.removeObserver(this)
        }

        override fun onChanged(v: V?) {
            if (version != liveEvent.getVersion()) {
                version = liveEvent.getVersion()
                observer.onChanged(v)
            }
        }

    }
}