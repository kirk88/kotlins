@file:Suppress("unused")

package com.nice.common.event

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

object EventBus {

    private val mutableEvents = MutableSharedFlow<Event>()
    val events = mutableEvents.asSharedFlow()

    private val mutableStickyEvents = MutableSharedFlow<Event>(replay = 1)
    val stickyEvents = mutableStickyEvents.asSharedFlow()

    fun <T : Event> LifecycleOwner.produceEvent(event: T): Job = lifecycleScope.launch {
        mutableEvents.emit(event)
    }

    fun <T : Event> LifecycleOwner.produceStickyEvent(event: T): Job = lifecycleScope.launch {
        mutableStickyEvents.emit(event)
    }

    fun <T : Event> tryProduceEvent(event: T): Boolean = mutableEvents.tryEmit(event)

    fun <T : Event> tryProduceStickyEvent(event: T): Boolean = mutableStickyEvents.tryEmit(event)

    fun <T : Event> LifecycleOwner.produceEventWhenCreated(event: T): Job = lifecycleScope.launchWhenCreated {
        mutableEvents.emit(event)
    }

    fun <T : Event> LifecycleOwner.produceStickyEventWhenCreated(event: T): Job = lifecycleScope.launchWhenCreated {
        mutableStickyEvents.emit(event)
    }

    fun <T : Event> LifecycleOwner.produceEventWhenStarted(event: T): Job = lifecycleScope.launchWhenStarted {
        mutableEvents.emit(event)
    }

    fun <T : Event> LifecycleOwner.produceStickyEventWhenStarted(event: T): Job = lifecycleScope.launchWhenStarted {
        mutableStickyEvents.emit(event)
    }

    fun <T : Event> LifecycleOwner.produceEventWhenResumed(event: T): Job = lifecycleScope.launchWhenResumed {
        mutableEvents.emit(event)
    }

    fun <T : Event> LifecycleOwner.produceStickyEventWhenResumed(event: T): Job = lifecycleScope.launchWhenResumed {
        mutableStickyEvents.emit(event)
    }

    inline fun LifecycleOwner.subscribeEvent(
        crossinline predicate: suspend (Event) -> Boolean,
        crossinline action: suspend (Event) -> Unit
    ): Job = events
        .filter { predicate.invoke(it) }
        .onEach { action.invoke(it) }
        .cancellable()
        .launchIn(lifecycleScope)

    inline fun <reified T : Event> LifecycleOwner.subscribeEvent(
        crossinline action: suspend (T) -> Unit
    ): Job = events
        .filterIsInstance<T>()
        .onEach { action.invoke(it) }
        .cancellable()
        .launchIn(lifecycleScope)


    inline fun LifecycleOwner.subscribeStickyEvent(
        crossinline predicate: suspend (Event) -> Boolean,
        crossinline action: suspend (Event) -> Unit
    ): Job = stickyEvents
        .filter { predicate.invoke(it) }
        .onEach { action.invoke(it) }
        .cancellable()
        .launchIn(lifecycleScope)

    inline fun <reified T : Event> LifecycleOwner.subscribeStickyEvent(
        crossinline action: suspend (T) -> Unit
    ): Job = stickyEvents
        .filterIsInstance<T>()
        .onEach { action.invoke(it) }
        .cancellable()
        .launchIn(lifecycleScope)

}

open class NamedEvent(open val name: String, open val value: Any) : Event

operator fun NamedEvent.component1(): String = name
operator fun NamedEvent.component2(): Any = value