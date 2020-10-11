package com.easy.kotlins.helper

import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean


/**
 * Create by LiZhanPing on 2020/9/11
 */
object LiveBus {

    val BUS: Map<KeyWrapper<*>, LiveEventWrapper<*>> by lazy { mutableMapOf() }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> getSender(key: String): LiveEventNotifier<T> {
        val keyWrapper = KeyWrapper(key, T::class.java)
        val value: LiveEventNotifier<T>
        synchronized(BUS) {
            val existing = BUS[keyWrapper]
            value = if (existing != null) {
                existing as LiveEventNotifier<T>
            } else {
                LiveEventNotifier<T>().also { (BUS as MutableMap)[keyWrapper] = it }
            }
        }
        return value
    }

    @Suppress("UNCHECKED_CAST")
    fun getCaller(key: String): LiveEventCaller {
        val keyWrapper = KeyWrapper(key, Nothing::class.java)
        val value: LiveEventCaller
        synchronized(BUS) {
            val existing = BUS[keyWrapper]
            value = if (existing != null) {
                existing as LiveEventCaller
            } else {
                LiveEventCaller().also { (BUS as MutableMap)[keyWrapper] = it }
            }
        }
        return value
    }

    inline fun <reified T> remove(key: String): LiveEventWrapper<*>? {
        val keyWrapper = KeyWrapper(key, T::class.java)
        synchronized(BUS) {
            return (BUS as MutableMap).remove(keyWrapper)
        }
    }
}

data class KeyWrapper<T>(
        val key: String,
        private val clazz: Class<T>
)

class LiveEventCaller : LiveEventWrapper<Nothing>() {

    fun call() {
        LiveDataPoster.post(liveEvent, null)
    }

    fun observe(owner: LifecycleOwner, observer: () -> Unit) {
        liveEvent.observeCall(owner, observer)
    }

    fun observeForever(observer: () -> Unit) {
        liveEvent.observeCallForever(observer)
    }
}

class LiveEventNotifier<T> : LiveEventWrapper<T>() {

    fun post(value: T) {
        LiveDataPoster.post(liveEvent, value)
    }

    fun observe(owner: LifecycleOwner, observer: (value: T?) -> Unit) {
        liveEvent.observe(owner, observer)
    }

    fun observeForever(observer: (value: T?) -> Unit) {
        liveEvent.observeForever(observer)
    }

}

abstract class LiveEventWrapper<T> {

    protected val liveEvent: SingleLiveEvent<T> = SingleLiveEvent()

}

class SingleLiveEvent<T> : MutableLiveData<T>() {
    private val pending: AtomicBoolean = AtomicBoolean(false)

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner) {
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(it)
            }
        }
    }

    override fun observeForever(observer: Observer<in T>) {
        super.observeForever {
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(it)
            }
        }
    }
    inline fun observeCall(owner: LifecycleOwner, crossinline observer: () -> Unit) {
        observe(owner, Observer {
            observer()
        })
    }

    inline fun observeCallForever(crossinline observer: () -> Unit) {
        observeForever(Observer {
            observer()
        })
    }

    @MainThread
    override fun setValue(t: T?) {
        pending.set(true)
        super.setValue(t)
    }

    /**
     * Used for cases where T is Void, to make calls cleaner.
     */
    @MainThread
    fun call() {
        value = null
    }

    @MainThread
    fun postCall() {
        postValue(null)
    }
}

/**
 * data are not lost
 */
object LiveDataPoster {
    private val handler: Handler by lazy { Handler(Looper.getMainLooper()) }

    fun <T> post(liveData: MutableLiveData<T>, data: T?) {
        if (Looper.myLooper() === Looper.getMainLooper()) {
            liveData.setValue(data)
        } else {
            setValue(liveData, data)
        }
    }

    private fun <T> setValue(liveData: MutableLiveData<T>, data: T?) {
        handler.post(SetValueRunnable.create(liveData, data))
    }

    private class SetValueRunnable<T> private constructor(private val liveData: MutableLiveData<T>, private val data: T?) : Runnable {
        override fun run() {
            liveData.value = data
        }

        companion object {
            fun <T> create(liveData: MutableLiveData<T>, data: T?): SetValueRunnable<T> {
                return SetValueRunnable(liveData, data)
            }
        }
    }
}