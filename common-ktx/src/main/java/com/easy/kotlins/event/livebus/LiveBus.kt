package com.easy.kotlins.event.livebus

import androidx.lifecycle.*

@PublishedApi
internal object LiveBus {
    private val eventMap = mutableMapOf<String, Any>()

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String?, clazz: Class<T>): Observable<T> {
        return eventMap.getOrPut(key ?: clazz.name) {
            LiveEvent<T>()
        } as Observable<T>
    }

    fun get(key: String): SimpleObservable {
        return eventMap.getOrPut(key) {
            SimpleLiveEvent()
        } as SimpleObservable
    }
}

@PublishedApi
internal class LifecycleActiveObserverWrapper<T>(
    private val owner: LifecycleOwner,
    private val observable: Observable<T>,
    private val observer: Observer<T>
) : Observer<T>, LifecycleObserver {

    init {
        owner.lifecycle.addObserver(this)
    }

    override fun onChanged(value: T?) {
        observer.onChanged(value)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        observable.removeObserver(observer)
        owner.lifecycle.removeObserver(this)
    }
}

@PublishedApi
internal class LifecycleActiveSimpleObserverWrapper(
    private val owner: LifecycleOwner,
    private val observable: SimpleObservable,
    private val observer: SimpleObserver
) : SimpleObserver, LifecycleObserver {

    init {
        owner.lifecycle.addObserver(this)
    }

    override fun onChanged() {
        observer.onChanged()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        observable.removeObserver(observer)
        owner.lifecycle.removeObserver(this)
    }
}

inline fun <reified T> liveBus(key: String? = null): Observable<T> = LiveBus.get(key, T::class.java)

inline fun simpleLiveBus(key: String): SimpleObservable = LiveBus.get(key)

inline fun <reified T> Observer<T>.removeSelf(key: String? = null) {
    return liveBus<T>(key).removeObserver(this)
}

inline fun <T> Observable<T>.observeActive(owner: LifecycleOwner, observer: Observer<T>) {
    observeForever(LifecycleActiveObserverWrapper(owner, this, observer))
}

inline fun <T> Observable<T>.observeStickyActive(owner: LifecycleOwner, observer: Observer<T>) {
    observeStickyForever(LifecycleActiveObserverWrapper(owner, this, observer))
}

inline fun SimpleObserver.removeSelf(key: String) {
    return simpleLiveBus(key).removeObserver(this)
}

inline fun SimpleObservable.observeActive(owner: LifecycleOwner, observer: SimpleObserver) {
    observeForever(LifecycleActiveSimpleObserverWrapper(owner, this, observer))
}

inline fun SimpleObservable.observeStickyActive(owner: LifecycleOwner, observer: SimpleObserver) {
    observeStickyForever(LifecycleActiveSimpleObserverWrapper(owner, this, observer))
}
