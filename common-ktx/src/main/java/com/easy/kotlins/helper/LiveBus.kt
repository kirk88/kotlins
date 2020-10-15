package com.easy.kotlins.helper

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.easy.kotlins.event.SingleLiveEvent


/**
 * Create by LiZhanPing on 2020/9/11
 */
object LiveBus {

    val BUS: Map<TypedKey<*>, LiveDataObservable<*>> by lazy { mutableMapOf() }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> getSender(key: String): LiveDataSender<T> {
        val typedKey = TypedKey(key, T::class.java)
        val value: LiveDataSender<T>
        synchronized(BUS) {
            val existing = BUS[typedKey]
            value = if (existing != null) {
                existing as LiveDataSender<T>
            } else {
                LiveDataSender<T>().also { (BUS as MutableMap)[typedKey] = it }
            }
        }
        return value
    }

    @Suppress("UNCHECKED_CAST")
    fun getCaller(key: String): LiveDataCaller {
        val typedKey = TypedKey(key, Nothing::class.java)
        val value: LiveDataCaller
        synchronized(BUS) {
            val existing = BUS[typedKey]
            value = if (existing != null) {
                existing as LiveDataCaller
            } else {
                LiveDataCaller().also { (BUS as MutableMap)[typedKey] = it }
            }
        }
        return value
    }

    inline fun <reified T> remove(key: String): LiveDataObservable<*>? {
        val typedKey = TypedKey(key, T::class.java)
        synchronized(BUS) {
            return (BUS as MutableMap).remove(typedKey)
        }
    }
}

data class TypedKey<T>(
        val key: String,
        private val clazz: Class<T>
)

open class LiveDataCaller : LiveDataObservable<Nothing>() {

    fun call() {
        LiveDataPoster.post(liveData, null)
    }

    fun observe(owner: LifecycleOwner, observer: () -> Unit): Disposable {
        return ObserverDisposable(liveData, Observer<Nothing> { observer() }.also {
            liveData.observe(owner, it)
        })
    }

    fun observeForever(observer: () -> Unit): Disposable {
        return ObserverDisposable(liveData, Observer<Nothing> { observer() }.also {
            liveData.observeForever(it)
        })
    }
}

open class LiveDataSender<T> : LiveDataObservable<T>() {

    fun post(value: T) {
        LiveDataPoster.post(liveData, value)
    }

    fun observe(owner: LifecycleOwner, observer: (value: T?) -> Unit): Disposable {
        return ObserverDisposable(liveData, Observer<T> { observer(it) }.also {
            liveData.observe(owner, it)
        })
    }

    fun observeForever(observer: (value: T?) -> Unit): Disposable {
        return ObserverDisposable(liveData, Observer<T> { observer(it) }.also {
            liveData.observeForever(it)
        })
    }

}

class LiveEventSender<T> : LiveDataSender<T>(){

    override val liveData: MutableLiveData<T> = SingleLiveEvent()

}


abstract class LiveDataObservable<T> {

    protected open val liveData: MutableLiveData<T> = MutableLiveData()

    fun removeObservers(owner: LifecycleOwner) {
        liveData.removeObservers(owner)
    }
}

interface Disposable {

    fun dispose()

}

class ObserverDisposable<T>(
    private val liveData: LiveData<T>,
    private val observer: Observer<T>
) : Disposable {
    override fun dispose() {
        liveData.removeObserver(observer)
    }
}