@file:Suppress("unused")

package com.nice.common.event

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

object FlowEventBus {

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

    fun <T : Event> ViewModel.produceEvent(event: T): Job = viewModelScope.launch {
        mutableEvents.emit(event)
    }

    fun <T : Event> ViewModel.produceStickyEvent(event: T): Job = viewModelScope.launch {
        mutableStickyEvents.emit(event)
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun <T : Event> produceEventGlobal(event: T): Job = GlobalScope.launch {
        mutableEvents.emit(event)
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun <T : Event> produceStickyEventGlobal(event: T): Job = GlobalScope.launch {
        mutableStickyEvents.emit(event)
    }

    fun <T : Event> LifecycleOwner.produceEventWhenCreated(event: T): Job =
        lifecycleScope.launchWhenCreated {
            mutableEvents.emit(event)
        }

    fun <T : Event> LifecycleOwner.produceStickyEventWhenCreated(event: T): Job =
        lifecycleScope.launchWhenCreated {
            mutableStickyEvents.emit(event)
        }

    fun <T : Event> LifecycleOwner.produceEventWhenStarted(event: T): Job =
        lifecycleScope.launchWhenStarted {
            mutableEvents.emit(event)
        }

    fun <T : Event> LifecycleOwner.produceStickyEventWhenStarted(event: T): Job =
        lifecycleScope.launchWhenStarted {
            mutableStickyEvents.emit(event)
        }

    fun <T : Event> LifecycleOwner.produceEventWhenResumed(event: T): Job =
        lifecycleScope.launchWhenResumed {
            mutableEvents.emit(event)
        }

    fun <T : Event> LifecycleOwner.produceStickyEventWhenResumed(event: T): Job =
        lifecycleScope.launchWhenResumed {
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

    inline fun LifecycleOwner.subscribeEvent(
        name: String,
        crossinline action: suspend (NamedEvent) -> Unit
    ): Job = events
        .filterIsInstance<NamedEvent>()
        .filter { it.name == name }
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

    inline fun LifecycleOwner.subscribeStickyEvent(
        name: String,
        crossinline action: suspend (NamedEvent) -> Unit
    ): Job = stickyEvents
        .filterIsInstance<NamedEvent>()
        .filter { it.name == name }
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

    inline fun ViewModel.subscribeEvent(
        crossinline predicate: suspend (Event) -> Boolean,
        crossinline action: suspend (Event) -> Unit
    ): Job = events
        .filter { predicate.invoke(it) }
        .onEach { action.invoke(it) }
        .cancellable()
        .launchIn(viewModelScope)

    inline fun ViewModel.subscribeEvent(
        name: String,
        crossinline action: suspend (NamedEvent) -> Unit
    ): Job = events
        .filterIsInstance<NamedEvent>()
        .filter { it.name == name }
        .onEach { action.invoke(it) }
        .cancellable()
        .launchIn(viewModelScope)

    inline fun <reified T : Event> ViewModel.subscribeEvent(
        crossinline action: suspend (T) -> Unit
    ): Job = events
        .filterIsInstance<T>()
        .onEach { action.invoke(it) }
        .cancellable()
        .launchIn(viewModelScope)

    inline fun ViewModel.subscribeStickyEvent(
        crossinline predicate: suspend (Event) -> Boolean,
        crossinline action: suspend (Event) -> Unit
    ): Job = stickyEvents
        .filter { predicate.invoke(it) }
        .onEach { action.invoke(it) }
        .cancellable()
        .launchIn(viewModelScope)

    inline fun ViewModel.subscribeStickyEvent(
        name: String,
        crossinline action: suspend (NamedEvent) -> Unit
    ): Job = stickyEvents
        .filterIsInstance<NamedEvent>()
        .filter { it.name == name }
        .onEach { action.invoke(it) }
        .cancellable()
        .launchIn(viewModelScope)

    inline fun <reified T : Event> ViewModel.subscribeStickyEvent(
        crossinline action: suspend (T) -> Unit
    ): Job = stickyEvents
        .filterIsInstance<T>()
        .onEach { action.invoke(it) }
        .cancellable()
        .launchIn(viewModelScope)

    @OptIn(DelicateCoroutinesApi::class)
    inline fun subscribeEventForever(
        crossinline predicate: suspend (Event) -> Boolean,
        crossinline action: suspend (Event) -> Unit
    ): Job = events
        .filter { predicate.invoke(it) }
        .onEach { action.invoke(it) }
        .cancellable()
        .launchIn(GlobalScope)

    @OptIn(DelicateCoroutinesApi::class)
    inline fun subscribeEventForever(
        name: String,
        crossinline action: suspend (NamedEvent) -> Unit
    ): Job = events
        .filterIsInstance<NamedEvent>()
        .filter { it.name == name }
        .onEach { action.invoke(it) }
        .cancellable()
        .launchIn(GlobalScope)

    @OptIn(DelicateCoroutinesApi::class)
    inline fun <reified T : Event> subscribeEventForever(
        crossinline action: suspend (T) -> Unit
    ): Job = events
        .filterIsInstance<T>()
        .onEach { action.invoke(it) }
        .cancellable()
        .launchIn(GlobalScope)

    @OptIn(DelicateCoroutinesApi::class)
    inline fun subscribeStickyEventForever(
        crossinline predicate: suspend (Event) -> Boolean,
        crossinline action: suspend (Event) -> Unit
    ): Job = stickyEvents
        .filter { predicate.invoke(it) }
        .onEach { action.invoke(it) }
        .cancellable()
        .launchIn(GlobalScope)

    @OptIn(DelicateCoroutinesApi::class)
    inline fun subscribeStickyEventForever(
        name: String,
        crossinline action: suspend (NamedEvent) -> Unit
    ): Job = stickyEvents
        .filterIsInstance<NamedEvent>()
        .filter { it.name == name }
        .onEach { action.invoke(it) }
        .cancellable()
        .launchIn(GlobalScope)

    @OptIn(DelicateCoroutinesApi::class)
    inline fun <reified T : Event> subscribeStickyEventForever(
        crossinline action: suspend (T) -> Unit
    ): Job = stickyEvents
        .filterIsInstance<T>()
        .onEach { action.invoke(it) }
        .cancellable()
        .launchIn(GlobalScope)


}