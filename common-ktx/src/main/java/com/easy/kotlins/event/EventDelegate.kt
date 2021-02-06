package com.easy.kotlins.event

import androidx.lifecycle.LifecycleOwner
import kotlin.reflect.KProperty

class EventDelegate {

    private val liveEvent = MutableLiveEventData<Event>()

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

    fun addEventObserver(owner: LifecycleOwner, observer: EventObserver) {
        liveEvent.observeActive(owner) { if (it != null) observer.onEventChanged(it) }
    }

    fun addEventObserver(observer: EventLifecycleObserver) {
        liveEvent.observeActive(observer) { if (it != null) observer.onEventChanged(it) }
    }

    companion object {
        private val NOT_SET = Event()
    }

}

fun interface EventObserver {
    fun onEventChanged(event: Event)
}

interface EventLifecycleObserver : EventObserver, LifecycleOwner