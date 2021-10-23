@file:Suppress("UNUSED", "UNCHECKED_CAST")

package com.nice.common.event

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*

object FlowEventBus {

    private val flows = mutableMapOf<String, EventFlow<out Any>>()

    operator fun <T : Any> get(name: String): EventFlow<T> =
        flows.getOrPut(name) { EventFlow<T>() } as EventFlow<T>

    inline fun <reified T : Any> get(): EventFlow<T> = get(T::class.java.name)

    fun <T : Any> LifecycleOwner.emitEvent(
        name: String,
        event: T,
        delay: Long = 0L
    ): Job = lifecycleScope.launch {
        if (delay > 0) delay(delay)
        get<T>(name).emitEvent(event)
    }

    inline fun <T : Any> LifecycleOwner.emitEvent(
        name: String,
        crossinline event: suspend CoroutineScope.() -> T
    ): Job = lifecycleScope.launch {
        get<T>(name).emitEvent(event())
    }

    fun <T : Any> LifecycleOwner.emitEvent(
        event: T,
        delay: Long = 0L
    ): Job = lifecycleScope.launch {
        if (delay > 0) delay(delay)
        get<T>(event.javaClass.name).emitEvent(event)
    }

    inline fun <T : Any> LifecycleOwner.emitEvent(
        crossinline event: suspend CoroutineScope.() -> T
    ): Job = lifecycleScope.launch {
        event().let { get<T>(it.javaClass.name).emitEvent(it) }
    }

    fun <T : Any> LifecycleOwner.emitStickyEvent(
        name: String,
        event: T,
        delay: Long = 0L
    ): Job = lifecycleScope.launch {
        if (delay > 0) delay(delay)
        get<T>(name).emitStickyEvent(event)
    }

    inline fun <T : Any> LifecycleOwner.emitStickyEvent(
        name: String,
        crossinline event: suspend CoroutineScope.() -> T
    ): Job = lifecycleScope.launch {
        get<T>(name).emitStickyEvent(event())
    }

    fun <T : Any> LifecycleOwner.emitStickyEvent(
        event: T,
        delay: Long = 0L
    ): Job = lifecycleScope.launch {
        if (delay > 0) delay(delay)
        get<T>(event.javaClass.name).emitStickyEvent(event)
    }

    inline fun <T : Any> LifecycleOwner.emitStickyEvent(
        crossinline event: suspend CoroutineScope.() -> T
    ): Job = lifecycleScope.launch {
        event().let { get<T>(it.javaClass.name).emitStickyEvent(it) }
    }

    fun <T : Any> ViewModel.emitEvent(
        name: String,
        event: T,
        delay: Long = 0L
    ): Job = viewModelScope.launch {
        if (delay > 0) delay(delay)
        get<T>(name).emitEvent(event)
    }

    inline fun <T : Any> ViewModel.emitEvent(
        name: String,
        crossinline event: suspend CoroutineScope.() -> T
    ): Job = viewModelScope.launch {
        get<T>(name).emitEvent(event())
    }

    fun <T : Any> ViewModel.emitEvent(
        event: T,
        delay: Long = 0L
    ): Job = viewModelScope.launch {
        if (delay > 0) delay(delay)
        get<T>(event.javaClass.name).emitEvent(event)
    }

    inline fun <T : Any> ViewModel.emitEvent(
        crossinline event: suspend CoroutineScope.() -> T
    ): Job = viewModelScope.launch {
        event().let { get<T>(it.javaClass.name).emitEvent(it) }
    }

    fun <T : Any> ViewModel.emitStickyEvent(
        name: String,
        event: T,
        delay: Long = 0L
    ): Job = viewModelScope.launch {
        if (delay > 0) delay(delay)
        get<T>(name).emitStickyEvent(event)
    }

    inline fun <T : Any> ViewModel.emitStickyEvent(
        name: String,
        crossinline event: suspend CoroutineScope.() -> T
    ): Job = viewModelScope.launch {
        get<T>(name).emitStickyEvent(event())
    }

    fun <T : Any> ViewModel.emitStickyEvent(
        event: T,
        delay: Long = 0L
    ): Job = viewModelScope.launch {
        if (delay > 0) delay(delay)
        get<T>(event.javaClass.name).emitStickyEvent(event)
    }

    inline fun <T : Any> ViewModel.emitStickyEvent(
        crossinline event: suspend CoroutineScope.() -> T
    ): Job = viewModelScope.launch {
        event().let { get<T>(it.javaClass.name).emitStickyEvent(it) }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun <T : Any> emitEventGlobal(
        name: String,
        event: T,
        delay: Long = 0L
    ): Job = GlobalScope.launch {
        if (delay > 0) delay(delay)
        get<T>(name).emitEvent(event)
    }

    @OptIn(DelicateCoroutinesApi::class)
    inline fun <T : Any> emitEventGlobal(
        name: String,
        crossinline event: suspend CoroutineScope.() -> T
    ): Job = GlobalScope.launch {
        get<T>(name).emitEvent(event())
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun <T : Any> emitEventGlobal(
        event: T,
        delay: Long = 0L
    ): Job = GlobalScope.launch {
        if (delay > 0) delay(delay)
        get<T>(event.javaClass.name).emitEvent(event)
    }

    @OptIn(DelicateCoroutinesApi::class)
    inline fun <T : Any> emitEventGlobal(
        crossinline event: suspend CoroutineScope.() -> T
    ): Job = GlobalScope.launch {
        event().let { get<T>(it.javaClass.name).emitEvent(it) }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun <T : Any> emitStickyEventGlobal(
        name: String,
        event: T,
        delay: Long = 0L
    ): Job = GlobalScope.launch {
        if (delay > 0) delay(delay)
        get<T>(name).emitStickyEvent(event)
    }

    @OptIn(DelicateCoroutinesApi::class)
    inline fun <T : Any> emitStickyEventGlobal(
        name: String,
        crossinline event: suspend CoroutineScope.() -> T
    ): Job = GlobalScope.launch {
        get<T>(name).emitStickyEvent(event())
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun <T : Any> emitStickyEventGlobal(
        event: T,
        delay: Long = 0L
    ): Job = GlobalScope.launch {
        if (delay > 0) delay(delay)
        get<T>(event.javaClass.name).emitStickyEvent(event)
    }

    @OptIn(DelicateCoroutinesApi::class)
    inline fun <T : Any> emitStickyEventGlobal(
        crossinline event: suspend CoroutineScope.() -> T
    ): Job = GlobalScope.launch {
        event().let { get<T>(it.javaClass.name).emitStickyEvent(it) }
    }

    fun <T : Any> LifecycleOwner.emitEventWhenCreated(
        name: String,
        event: T,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenCreated {
        if (delay > 0) delay(delay)
        get<T>(name).emitEvent(event)
    }

    inline fun <T : Any> LifecycleOwner.emitEventWhenCreated(
        name: String,
        crossinline event: suspend CoroutineScope.() -> T
    ): Job = lifecycleScope.launchWhenCreated {
        get<T>(name).emitEvent(event())
    }

    fun <T : Any> LifecycleOwner.emitEventWhenCreated(
        event: T,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenCreated {
        if (delay > 0) delay(delay)
        get<T>(event.javaClass.name).emitEvent(event)
    }

    inline fun <T : Any> LifecycleOwner.emitEventWhenCreated(
        crossinline event: suspend CoroutineScope.() -> T
    ): Job = lifecycleScope.launchWhenCreated {
        event().let { get<T>(it.javaClass.name).emitEvent(it) }
    }

    fun <T : Any> LifecycleOwner.emitStickyEventWhenCreated(
        name: String,
        event: T,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenCreated {
        if (delay > 0) delay(delay)
        get<T>(name).emitStickyEvent(event)
    }

    inline fun <T : Any> LifecycleOwner.emitStickyEventWhenCreated(
        name: String,
        crossinline event: suspend CoroutineScope.() -> T
    ): Job = lifecycleScope.launchWhenCreated {
        get<T>(name).emitStickyEvent(event())
    }

    fun <T : Any> LifecycleOwner.emitStickyEventWhenCreated(
        event: T,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenCreated {
        if (delay > 0) delay(delay)
        get<T>(event.javaClass.name).emitStickyEvent(event)
    }

    inline fun <T : Any> LifecycleOwner.emitStickyEventWhenCreated(
        crossinline event: suspend CoroutineScope.() -> T
    ): Job = lifecycleScope.launchWhenCreated {
        event().let { get<T>(it.javaClass.name).emitStickyEvent(it) }
    }

    fun <T : Any> LifecycleOwner.emitEventWhenStarted(
        name: String,
        event: T,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenStarted {
        if (delay > 0) delay(delay)
        get<T>(name).emitEvent(event)
    }

    inline fun <T : Any> LifecycleOwner.emitEventWhenStarted(
        name: String,
        crossinline event: suspend CoroutineScope.() -> T
    ): Job = lifecycleScope.launchWhenStarted {
        get<T>(name).emitEvent(event())
    }

    fun <T : Any> LifecycleOwner.emitEventWhenStarted(
        event: T,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenStarted {
        if (delay > 0) delay(delay)
        get<T>(event.javaClass.name).emitEvent(event)
    }

    inline fun <T : Any> LifecycleOwner.emitEventWhenStarted(
        crossinline event: suspend CoroutineScope.() -> T
    ): Job = lifecycleScope.launchWhenStarted {
        event().let { get<T>(it.javaClass.name).emitEvent(it) }
    }

    fun <T : Any> LifecycleOwner.emitStickyEventWhenStarted(
        name: String,
        event: T,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenStarted {
        if (delay > 0) delay(delay)
        get<T>(name).emitStickyEvent(event)
    }

    inline fun <T : Any> LifecycleOwner.emitStickyEventWhenStarted(
        name: String,
        crossinline event: suspend CoroutineScope.() -> T
    ): Job = lifecycleScope.launchWhenStarted {
        get<T>(name).emitStickyEvent(event())
    }

    fun <T : Any> LifecycleOwner.emitStickyEventWhenStarted(
        event: T,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenStarted {
        if (delay > 0) delay(delay)
        get<T>(event.javaClass.name).emitStickyEvent(event)
    }

    inline fun <T : Any> LifecycleOwner.emitStickyEventWhenStarted(
        crossinline event: suspend CoroutineScope.() -> T
    ): Job = lifecycleScope.launchWhenStarted {
        event().let { get<T>(it.javaClass.name).emitStickyEvent(it) }
    }

    fun <T : Any> LifecycleOwner.emitEventWhenResumed(
        name: String,
        event: T,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenResumed {
        if (delay > 0) delay(delay)
        get<T>(name).emitEvent(event)
    }

    inline fun <T : Any> LifecycleOwner.emitEventWhenResumed(
        name: String,
        crossinline event: suspend CoroutineScope.() -> T
    ): Job = lifecycleScope.launchWhenResumed {
        get<T>(name).emitEvent(event())
    }

    fun <T : Any> LifecycleOwner.emitEventWhenResumed(
        event: T,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenResumed {
        if (delay > 0) delay(delay)
        get<T>(event.javaClass.name).emitEvent(event)
    }

    inline fun <T : Any> LifecycleOwner.emitEventWhenResumed(
        crossinline event: suspend CoroutineScope.() -> T
    ): Job = lifecycleScope.launchWhenResumed {
        event().let { get<T>(it.javaClass.name).emitEvent(it) }
    }

    fun <T : Any> LifecycleOwner.emitStickyEventWhenResumed(
        name: String,
        event: T,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenResumed {
        if (delay > 0) delay(delay)
        get<T>(name).emitStickyEvent(event)
    }

    inline fun <T : Any> LifecycleOwner.emitStickyEventWhenResumed(
        name: String,
        crossinline event: suspend CoroutineScope.() -> T
    ): Job = lifecycleScope.launchWhenResumed {
        get<T>(name).emitStickyEvent(event())
    }

    fun <T : Any> LifecycleOwner.emitStickyEventWhenResumed(
        event: T,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenResumed {
        if (delay > 0) delay(delay)
        get<T>(event.javaClass.name).emitStickyEvent(event)
    }

    inline fun <T : Any> LifecycleOwner.emitStickyEventWhenResumed(
        crossinline event: suspend CoroutineScope.() -> T
    ): Job = lifecycleScope.launchWhenResumed {
        event().let { get<T>(it.javaClass.name).emitStickyEvent(it) }
    }

    fun <T : Any> LifecycleOwner.collectEvent(
        name: String,
        action: suspend CoroutineScope.(T) -> Unit
    ): Job = lifecycleScope.launch {
        get<T>(name).collectEvent {
            action(it)
        }
    }

    inline fun <reified T : Any> LifecycleOwner.collectEvent(
        noinline action: suspend CoroutineScope.(T) -> Unit
    ): Job = lifecycleScope.launch {
        get<T>(T::class.java.name).collectEvent {
            action(it)
        }
    }

    fun <T : Any> LifecycleOwner.collectStickEvent(
        name: String,
        action: suspend CoroutineScope.(T) -> Unit
    ): Job = lifecycleScope.launch {
        get<T>(name).collectStickyEvent {
            action(it)
        }
    }

    inline fun <reified T : Any> LifecycleOwner.collectStickEvent(
        noinline action: suspend CoroutineScope.(T) -> Unit
    ): Job = lifecycleScope.launch {
        get<T>(T::class.java.name).collectStickyEvent {
            action(it)
        }
    }

    fun <T : Any> ViewModel.collectEvent(
        name: String,
        action: suspend CoroutineScope.(T) -> Unit
    ): Job = viewModelScope.launch {
        get<T>(name).collectEvent {
            action(it)
        }
    }

    inline fun <reified T : Any> ViewModel.collectEvent(
        noinline action: suspend CoroutineScope.(T) -> Unit
    ): Job = viewModelScope.launch {
        get<T>(T::class.java.name).collectEvent {
            action(it)
        }
    }

    fun <T : Any> ViewModel.collectStickyEvent(
        name: String,
        action: suspend CoroutineScope.(T) -> Unit
    ): Job = viewModelScope.launch {
        get<T>(name).collectStickyEvent {
            action(it)
        }
    }

    inline fun <reified T : Any> ViewModel.collectStickyEvent(
        noinline action: suspend CoroutineScope.(T) -> Unit
    ): Job = viewModelScope.launch {
        get<T>(T::class.java.name).collectStickyEvent {
            action(it)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun <T : Any> collectEventForever(
        name: String,
        action: suspend CoroutineScope.(T) -> Unit
    ): Job = GlobalScope.launch {
        get<T>(name).collectEvent {
            action(it)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    inline fun <reified T : Any> collectEventForever(
        noinline action: suspend CoroutineScope.(T) -> Unit
    ): Job = GlobalScope.launch {
        get<T>(T::class.java.name).collectEvent {
            action(it)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun <T : Any> collectStickyEventForever(
        name: String,
        action: suspend CoroutineScope.(T) -> Unit
    ): Job = GlobalScope.launch {
        get<T>(name).collectStickyEvent {
            action(it)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    inline fun <reified T : Any> collectStickyEventForever(
        noinline action: suspend CoroutineScope.(T) -> Unit
    ): Job = GlobalScope.launch {
        get<T>(T::class.java.name).collectStickyEvent {
            action(it)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun clearStickyEvent(name: String) = get<Any>(name).clearStickyEvent()

    inline fun <reified T : Any> clearStickyEvent() = get<T>(T::class.java.name).clearStickyEvent()

}