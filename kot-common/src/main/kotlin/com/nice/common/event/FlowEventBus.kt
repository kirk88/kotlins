@file:Suppress("UNUSED", "UNCHECKED_CAST")

package com.nice.common.event

import androidx.lifecycle.*
import kotlinx.coroutines.*

object FlowEventBus {

    @PublishedApi
    internal val flows = mutableMapOf<String, EventFlow<*>>()

    @PublishedApi
    internal fun <T> eventFlow(name: String): EventFlow<T?> =
        flows.getOrPut(name) { EventFlow<T?>() } as EventFlow<T?>

    @PublishedApi
    internal inline fun <reified T> eventFlow(): EventFlow<T?> =
        flows.getOrPut(T::class.java.name) { EventFlow<T?>() } as EventFlow<T?>

    fun <T> LifecycleOwner.emitEvent(
        name: String,
        event: T?,
        delay: Long = 0L
    ): Job = lifecycleScope.launch {
        delay(delay)
        eventFlow<T>(name).emit(event)
    }

    inline fun <T> LifecycleOwner.emitEvent(
        name: String,
        crossinline event: suspend () -> T?
    ): Job = lifecycleScope.launch {
        eventFlow<T>(name).emit(event())
    }

    inline fun <reified T> LifecycleOwner.emitEvent(
        event: T?,
        delay: Long = 0L
    ): Job = lifecycleScope.launch {
        delay(delay)
        eventFlow<T>().emit(event)
    }

    inline fun <reified T> LifecycleOwner.emitEvent(
        crossinline event: suspend () -> T?
    ): Job = lifecycleScope.launch {
        eventFlow<T>().emit(event())
    }

    fun <T> LifecycleOwner.emitStickyEvent(
        name: String,
        event: T?,
        delay: Long = 0L
    ): Job = lifecycleScope.launch {
        delay(delay)
        eventFlow<T>(name).emitSticky(event)
    }

    inline fun <T> LifecycleOwner.emitStickyEvent(
        name: String,
        crossinline event: suspend () -> T?
    ): Job = lifecycleScope.launch {
        eventFlow<T>(name).emitSticky(event())
    }

    inline fun <reified T> LifecycleOwner.emitStickyEvent(
        event: T?,
        delay: Long = 0L
    ): Job = lifecycleScope.launch {
        delay(delay)
        eventFlow<T>().emitSticky(event)
    }

    inline fun <reified T> LifecycleOwner.emitStickyEvent(
        crossinline event: suspend () -> T?
    ): Job = lifecycleScope.launch {
        eventFlow<T>().emitSticky(event())
    }

    fun <T> ViewModel.emitEvent(
        name: String,
        event: T?,
        delay: Long = 0L
    ): Job = viewModelScope.launch {
        delay(delay)
        eventFlow<T>(name).emit(event)
    }

    inline fun <T> ViewModel.emitEvent(
        name: String,
        crossinline event: suspend () -> T?
    ): Job = viewModelScope.launch {
        eventFlow<T>(name).emit(event())
    }

    inline fun <reified T> ViewModel.emitEvent(
        event: T?,
        delay: Long = 0L
    ): Job = viewModelScope.launch {
        delay(delay)
        eventFlow<T>().emit(event)
    }

    inline fun <reified T> ViewModel.emitEvent(
        crossinline event: suspend () -> T?
    ): Job = viewModelScope.launch {
        eventFlow<T>().emit(event())
    }

    fun <T> ViewModel.emitStickyEvent(
        name: String,
        event: T?,
        delay: Long = 0L
    ): Job = viewModelScope.launch {
        delay(delay)
        eventFlow<T>(name).emitSticky(event)
    }

    inline fun <T> ViewModel.emitStickyEvent(
        name: String,
        crossinline event: suspend () -> T?
    ): Job = viewModelScope.launch {
        eventFlow<T>(name).emitSticky(event())
    }

    inline fun <reified T> ViewModel.emitStickyEvent(
        event: T?,
        delay: Long = 0L
    ): Job = viewModelScope.launch {
        delay(delay)
        eventFlow<T>().emitSticky(event)
    }

    inline fun <reified T> ViewModel.emitStickyEvent(
        crossinline event: suspend () -> T?
    ): Job = viewModelScope.launch {
        eventFlow<T>().emitSticky(event())
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun <T> emitEventGlobal(
        name: String,
        event: T?,
        delay: Long = 0L
    ): Job = GlobalScope.launch {
        delay(delay)
        eventFlow<T>(name).emit(event)
    }

    @OptIn(DelicateCoroutinesApi::class)
    inline fun <T> emitEventGlobal(
        name: String,
        crossinline event: suspend () -> T?
    ): Job = GlobalScope.launch {
        eventFlow<T>(name).emit(event())
    }

    @OptIn(DelicateCoroutinesApi::class)
    inline fun <reified T> emitEventGlobal(
        event: T?,
        delay: Long = 0L
    ): Job = GlobalScope.launch {
        delay(delay)
        eventFlow<T>().emit(event)
    }

    @OptIn(DelicateCoroutinesApi::class)
    inline fun <reified T> emitEventGlobal(
        crossinline event: suspend () -> T?
    ): Job = GlobalScope.launch {
        eventFlow<T>().emit(event())
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun <T> emitStickyEventGlobal(
        name: String,
        event: T?,
        delay: Long = 0L
    ): Job = GlobalScope.launch {
        delay(delay)
        eventFlow<T>(name).emitSticky(event)
    }

    @OptIn(DelicateCoroutinesApi::class)
    inline fun <T> emitStickyEventGlobal(
        name: String,
        crossinline event: suspend () -> T?
    ): Job = GlobalScope.launch {
        eventFlow<T>(name).emitSticky(event())
    }

    @OptIn(DelicateCoroutinesApi::class)
    inline fun <reified T> emitStickyEventGlobal(
        event: T?,
        delay: Long = 0L
    ): Job = GlobalScope.launch {
        delay(delay)
        eventFlow<T>().emitSticky(event)
    }

    @OptIn(DelicateCoroutinesApi::class)
    inline fun <reified T> emitStickyEventGlobal(
        crossinline event: suspend () -> T?
    ): Job = GlobalScope.launch {
        eventFlow<T>().emitSticky(event())
    }

    fun <T> LifecycleOwner.emitEventWhenCreated(
        name: String,
        event: T?,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenCreated {
        delay(delay)
        eventFlow<T>(name).emit(event)
    }

    inline fun <T> LifecycleOwner.emitEventWhenCreated(
        name: String,
        crossinline event: suspend () -> T?
    ): Job = lifecycleScope.launchWhenCreated {
        eventFlow<T>(name).emit(event())
    }

    inline fun <reified T> LifecycleOwner.emitEventWhenCreated(
        event: T?,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenCreated {
        delay(delay)
        eventFlow<T>().emit(event)
    }

    inline fun <reified T> LifecycleOwner.emitEventWhenCreated(
        crossinline event: suspend () -> T?
    ): Job = lifecycleScope.launchWhenCreated {
        eventFlow<T>().emit(event())
    }

    fun <T> LifecycleOwner.emitStickyEventWhenCreated(
        name: String,
        event: T?,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenCreated {
        delay(delay)
        eventFlow<T>(name).emitSticky(event)
    }

    inline fun <T> LifecycleOwner.emitStickyEventWhenCreated(
        name: String,
        crossinline event: suspend () -> T?
    ): Job = lifecycleScope.launchWhenCreated {
        eventFlow<T>(name).emitSticky(event())
    }

    inline fun <reified T> LifecycleOwner.emitStickyEventWhenCreated(
        event: T?,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenCreated {
        delay(delay)
        eventFlow<T>().emitSticky(event)
    }

    inline fun <reified T> LifecycleOwner.emitStickyEventWhenCreated(
        crossinline event: suspend () -> T?
    ): Job = lifecycleScope.launchWhenCreated {
        eventFlow<T>().emitSticky(event())
    }

    fun <T> LifecycleOwner.emitEventWhenStarted(
        name: String,
        event: T?,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenStarted {
        delay(delay)
        eventFlow<T>(name).emit(event)
    }

    inline fun <T> LifecycleOwner.emitEventWhenStarted(
        name: String,
        crossinline event: suspend () -> T?
    ): Job = lifecycleScope.launchWhenStarted {
        eventFlow<T>(name).emit(event())
    }

    inline fun <reified T> LifecycleOwner.emitEventWhenStarted(
        event: T?,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenStarted {
        delay(delay)
        eventFlow<T>().emit(event)
    }

    inline fun <reified T> LifecycleOwner.emitEventWhenStarted(
        crossinline event: suspend () -> T?
    ): Job = lifecycleScope.launchWhenStarted {
        eventFlow<T>().emit(event())
    }

    fun <T> LifecycleOwner.emitStickyEventWhenStarted(
        name: String,
        event: T?,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenStarted {
        delay(delay)
        eventFlow<T>(name).emitSticky(event)
    }

    inline fun <T> LifecycleOwner.emitStickyEventWhenStarted(
        name: String,
        crossinline event: suspend () -> T?
    ): Job = lifecycleScope.launchWhenStarted {
        eventFlow<T>(name).emitSticky(event())
    }

    inline fun <reified T> LifecycleOwner.emitStickyEventWhenStarted(
        event: T?,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenStarted {
        delay(delay)
        eventFlow<T>().emitSticky(event)
    }

    inline fun <reified T> LifecycleOwner.emitStickyEventWhenStarted(
        crossinline event: suspend () -> T?
    ): Job = lifecycleScope.launchWhenStarted {
        eventFlow<T>().emitSticky(event())
    }

    fun <T> LifecycleOwner.emitEventWhenResumed(
        name: String,
        event: T?,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenResumed {
        delay(delay)
        eventFlow<T>(name).emit(event)
    }

    inline fun <T> LifecycleOwner.emitEventWhenResumed(
        name: String,
        crossinline event: suspend () -> T?
    ): Job = lifecycleScope.launchWhenResumed {
        eventFlow<T>(name).emit(event())
    }

    inline fun <reified T> LifecycleOwner.emitEventWhenResumed(
        event: T?,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenResumed {
        delay(delay)
        eventFlow<T>().emit(event)
    }

    inline fun <reified T> LifecycleOwner.emitEventWhenResumed(
        crossinline event: suspend () -> T?
    ): Job = lifecycleScope.launchWhenResumed {
        eventFlow<T>().emit(event())
    }

    fun <T> LifecycleOwner.emitStickyEventWhenResumed(
        name: String,
        event: T?,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenResumed {
        delay(delay)
        eventFlow<T>(name).emitSticky(event)
    }

    inline fun <T> LifecycleOwner.emitStickyEventWhenResumed(
        name: String,
        crossinline event: suspend () -> T?
    ): Job = lifecycleScope.launchWhenResumed {
        eventFlow<T>(name).emitSticky(event())
    }

    inline fun <reified T> LifecycleOwner.emitStickyEventWhenResumed(
        event: T?,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenResumed {
        delay(delay)
        eventFlow<T>().emitSticky(event)
    }

    inline fun <reified T> LifecycleOwner.emitStickyEventWhenResumed(
        crossinline event: suspend () -> T?
    ): Job = lifecycleScope.launchWhenResumed {
        eventFlow<T>().emitSticky(event())
    }

    inline fun <T> LifecycleOwner.collectEvent(
        name: String,
        crossinline action: suspend (T?) -> Unit
    ): Job = lifecycleScope.launch {
        eventFlow<T>(name).collect(action)
    }

    inline fun <T> LifecycleOwner.collectEventWithLifecycle(
        name: String,
        minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
        crossinline action: suspend (T?) -> Unit
    ): Job = lifecycleScope.launch {
        eventFlow<T>(name).collectWithLifecycle(lifecycle, minActiveState, action)
    }

    inline fun <reified T> LifecycleOwner.collectEvent(
        crossinline action: suspend (T?) -> Unit
    ): Job = lifecycleScope.launch {
        eventFlow<T>().collect(action)
    }

    inline fun <reified T> LifecycleOwner.collectEventWithLifecycle(
        minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
        crossinline action: suspend (T?) -> Unit
    ): Job = lifecycleScope.launch {
        eventFlow<T>().collectWithLifecycle(lifecycle, minActiveState, action)
    }

    inline fun <T> LifecycleOwner.collectStickEvent(
        name: String,
        crossinline action: suspend (T?) -> Unit
    ): Job = lifecycleScope.launch {
        eventFlow<T>(name).collectSticky(action)
    }

    inline fun <T> LifecycleOwner.collectStickEventWithLifecycle(
        name: String,
        minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
        crossinline action: suspend (T?) -> Unit
    ): Job = lifecycleScope.launch {
        eventFlow<T>(name).collectStickyWithLifecycle(lifecycle, minActiveState, action)
    }

    inline fun <reified T> LifecycleOwner.collectStickEvent(
        crossinline action: suspend (T?) -> Unit
    ): Job = lifecycleScope.launch {
        eventFlow<T>().collectSticky(action)
    }

    inline fun <reified T> LifecycleOwner.collectStickEventWithLifecycle(
        minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
        noinline action: suspend (T?) -> Unit
    ): Job = lifecycleScope.launch {
        eventFlow<T>().collectStickyWithLifecycle(lifecycle, minActiveState, action)
    }

    inline fun <T> ViewModel.collectEvent(
        name: String,
        crossinline action: suspend (T?) -> Unit
    ): Job = viewModelScope.launch {
        eventFlow<T>(name).collect(action)
    }

    inline fun <reified T> ViewModel.collectEvent(
        crossinline action: suspend (T?) -> Unit
    ): Job = viewModelScope.launch {
        eventFlow<T>().collect(action)
    }

    inline fun <T> ViewModel.collectStickyEvent(
        name: String,
        crossinline action: suspend (T?) -> Unit
    ): Job = viewModelScope.launch {
        eventFlow<T>(name).collectSticky(action)
    }

    inline fun <reified T> ViewModel.collectStickyEvent(
        crossinline action: suspend (T?) -> Unit
    ): Job = viewModelScope.launch {
        eventFlow<T>().collectSticky(action)
    }

    @OptIn(DelicateCoroutinesApi::class)
    inline fun <T> collectEventForever(
        name: String,
        crossinline action: suspend (T?) -> Unit
    ): Job = GlobalScope.launch {
        eventFlow<T>(name).collect(action)
    }

    @OptIn(DelicateCoroutinesApi::class)
    inline fun <reified T> collectEventForever(
        crossinline action: suspend (T?) -> Unit
    ): Job = GlobalScope.launch {
        eventFlow<T>().collect(action)
    }

    @OptIn(DelicateCoroutinesApi::class)
    inline fun <T> collectStickyEventForever(
        name: String,
        crossinline action: suspend (T?) -> Unit
    ): Job = GlobalScope.launch {
        eventFlow<T>(name).collectSticky(action)
    }

    @OptIn(DelicateCoroutinesApi::class)
    inline fun <reified T> collectStickyEventForever(
        crossinline action: suspend (T?) -> Unit
    ): Job = GlobalScope.launch {
        eventFlow<T>().collectSticky(action)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun clearStickyEvent(name: String) = eventFlow<Any>(name).clearStickyCache()

    inline fun <reified T> clearStickyEvent() = eventFlow<T>().clearStickyCache()

}