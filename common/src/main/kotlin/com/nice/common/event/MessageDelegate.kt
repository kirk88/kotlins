package com.nice.common.event

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStateAtLeast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KProperty

class MessageDelegate {

    private val messages = MutableSharedFlow<Message>()

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Message {
        return runBlocking { messages.last() }
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Message) {
        if (value is Message.Batch) {
            value.messages.forEach { messages.tryEmit(it) }
        } else {
            messages.tryEmit(value)
        }
    }

    fun addObserver(owner: LifecycleOwner, observer: EventObserver) {
        owner.launchWhenStateAtLeast(Lifecycle.State.CREATED) {
            messages.onEach {
                observer.onEventChanged(it)
            }.cancellable().collect()
        }
    }

    fun addObserver(observer: EventLifecycleObserver) {
        observer.launchWhenStateAtLeast(Lifecycle.State.CREATED) {
            messages.onEach {
                observer.onEventChanged(it)
            }.cancellable().collect()
        }
    }

}

fun interface EventObserver {
    fun onEventChanged(message: Message)
}

interface EventLifecycleObserver : EventObserver, LifecycleOwner

private fun <T> LifecycleOwner.launchWhenStateAtLeast(
    minState: Lifecycle.State,
    block: suspend CoroutineScope.() -> T
) {
    lifecycleScope.launch {
        lifecycle.whenStateAtLeast(minState, block)
    }
}