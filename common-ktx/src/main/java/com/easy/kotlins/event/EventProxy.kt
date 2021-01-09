package com.easy.kotlins.event

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KProperty

class EventProxy {

    private val liveEvent = SingleLiveEvent()

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Event {
        return liveEvent.getValue() ?: NOT_SET
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Event) {
        if (value is EventCollection) {
            value.events.forEach { liveEvent.postValue(it) }
        } else {
            liveEvent.postValue(value)
        }
    }

    fun observe(owner: LifecycleOwner, observer: (Event) -> Unit) {
        liveEvent.observe(owner) { if (it != null) observer(it) }
    }

    fun observe(owner: EventObservableOwner) {
        liveEvent.observe(owner) { if (it != null) owner.onEventChanged(it) }
    }

    companion object {
        private val NOT_SET = Event()
    }

}

internal class SingleLiveEvent : MutableLiveEventData<Event>() {
    private val pending: AtomicBoolean = AtomicBoolean(false)

    override fun observe(owner: LifecycleOwner, observer: Observer<in Event>) {
        super.observe(owner) {
            if (it !is SingleEvent) {
                observer.onChanged(it)
            } else if (pending.compareAndSet(true, false)) {
                observer.onChanged(it)
            }
        }
    }

    override fun observeForever(observer: Observer<in Event>) {
        super.observeForever {
            if (it !is SingleEvent) {
                observer.onChanged(it)
            } else if (pending.compareAndSet(true, false)) {
                observer.onChanged(it)
            }
        }
    }

    @MainThread
    override fun setValue(value: Event?) {
        pending.set(true)
        super.setValue(value)
    }
}

interface EventObservableOwner : LifecycleOwner {

    fun onEventChanged(event: Event)

}