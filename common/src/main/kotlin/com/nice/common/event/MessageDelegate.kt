package com.nice.common.event

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.*
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
        messages.onEach {
            observer.onMessageChanged(it)
        }.cancellable().launchIn(owner.lifecycleScope)
    }

    fun observe(observer: LifecycleMessageObserver) {
        messages.onEach {
            observer.onMessageChanged(it)
        }.cancellable().launchIn(observer.lifecycleScope)
    }

}

fun interface MessageObserver {
    fun onMessageChanged(message: Message)
}

interface LifecycleMessageObserver : MessageObserver, LifecycleOwner