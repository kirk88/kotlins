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


inline fun <reified T> liveBus(key: String? = null): Observable<T> = LiveBus.get(key, T::class.java)

fun simpleLiveBus(key: String): SimpleObservable = LiveBus.get(key)


