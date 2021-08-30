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

    private val messages = MutableSharedFlow<Message>(extraBufferCapacity = Int.MAX_VALUE)

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

    fun observe(owner: LifecycleOwner, observer: MessageObserver) {
        owner.launchWhenStateAtLeast(Lifecycle.State.CREATED) {
            messages.onEach {
                observer.onMessageChanged(it)
            }.cancellable().collect()
        }
    }

    fun observe(observer: LifecycleMessageObserver) {
        observer.launchWhenStateAtLeast(Lifecycle.State.CREATED) {
            messages.onEach {
                observer.onMessageChanged(it)
            }.cancellable().collect()
        }
    }

}

fun interface MessageObserver {
    fun onMessageChanged(message: Message)
}

interface LifecycleMessageObserver : MessageObserver, LifecycleOwner

private fun <T> LifecycleOwner.launchWhenStateAtLeast(
    minState: Lifecycle.State,
    block: suspend CoroutineScope.() -> T
) {
    lifecycleScope.launch {
        lifecycle.whenStateAtLeast(minState, block)
    }
}