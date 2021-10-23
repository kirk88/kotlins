@file:Suppress("UNUSED", "UNCHECKED_CAST")

package com.nice.common.event

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.SharedFlow

object FlowEventBus {

    @PublishedApi
    internal val flows = mutableMapOf<String, EventFlow<*>>()

    @PublishedApi
    internal fun <T> get(name: String): EventFlow<T?> =
        flows.getOrPut(name) { EventFlow<T?>() } as EventFlow<T?>

    @PublishedApi
    internal inline fun <reified T> get(): EventFlow<T?> =
        flows.getOrPut(T::class.java.name) { EventFlow<T?>() } as EventFlow<T?>

    fun <T> event(name: String): SharedFlow<T?> = get<T>(name).asSharedFlow()

    inline fun <reified T> event(): SharedFlow<T?> = get<T>().asSharedFlow()

    fun <T> stickyEvent(name: String): SharedFlow<T?> = get<T>(name).asStickySharedFlow()

    inline fun <reified T> stickyEvent(): SharedFlow<T?> = get<T>().asStickySharedFlow()

    fun <T> LifecycleOwner.emitEvent(
        name: String,
        event: T?,
        delay: Long = 0L
    ): Job = lifecycleScope.launch {
        if (delay > 0) delay(delay)
        get<T>(name).emitEvent(event)
    }

    inline fun <T> LifecycleOwner.emitEvent(
        name: String,
        crossinline event: suspend () -> T?
    ): Job = lifecycleScope.launch {
        get<T>(name).emitEvent(event())
    }

    inline fun <reified T> LifecycleOwner.emitEvent(
        event: T?,
        delay: Long = 0L
    ): Job = lifecycleScope.launch {
        if (delay > 0) delay(delay)
        get<T>().emitEvent(event)
    }

    inline fun <reified T> LifecycleOwner.emitEvent(
        crossinline event: suspend () -> T?
    ): Job = lifecycleScope.launch {
        get<T>().emitEvent(event())
    }

    fun <T> LifecycleOwner.emitStickyEvent(
        name: String,
        event: T?,
        delay: Long = 0L
    ): Job = lifecycleScope.launch {
        if (delay > 0) delay(delay)
        get<T>(name).emitStickyEvent(event)
    }

    inline fun <T> LifecycleOwner.emitStickyEvent(
        name: String,
        crossinline event: suspend () -> T?
    ): Job = lifecycleScope.launch {
        get<T>(name).emitStickyEvent(event())
    }

    inline fun <reified T> LifecycleOwner.emitStickyEvent(
        event: T?,
        delay: Long = 0L
    ): Job = lifecycleScope.launch {
        if (delay > 0) delay(delay)
        get<T>().emitStickyEvent(event)
    }

    inline fun <reified T> LifecycleOwner.emitStickyEvent(
        crossinline event: suspend () -> T?
    ): Job = lifecycleScope.launch {
        get<T>().emitStickyEvent(event())
    }

    fun <T> ViewModel.emitEvent(
        name: String,
        event: T?,
        delay: Long = 0L
    ): Job = viewModelScope.launch {
        if (delay > 0) delay(delay)
        get<T>(name).emitEvent(event)
    }

    inline fun <T> ViewModel.emitEvent(
        name: String,
        crossinline event: suspend () -> T?
    ): Job = viewModelScope.launch {
        get<T>(name).emitEvent(event())
    }

    inline fun <reified T> ViewModel.emitEvent(
        event: T?,
        delay: Long = 0L
    ): Job = viewModelScope.launch {
        if (delay > 0) delay(delay)
        get<T>().emitEvent(event)
    }

    inline fun <reified T> ViewModel.emitEvent(
        crossinline event: suspend () -> T?
    ): Job = viewModelScope.launch {
        get<T>().emitEvent(event())
    }

    fun <T> ViewModel.emitStickyEvent(
        name: String,
        event: T?,
        delay: Long = 0L
    ): Job = viewModelScope.launch {
        if (delay > 0) delay(delay)
        get<T>(name).emitStickyEvent(event)
    }

    inline fun <T> ViewModel.emitStickyEvent(
        name: String,
        crossinline event: suspend () -> T?
    ): Job = viewModelScope.launch {
        get<T>(name).emitStickyEvent(event())
    }

    inline fun <reified T> ViewModel.emitStickyEvent(
        event: T?,
        delay: Long = 0L
    ): Job = viewModelScope.launch {
        if (delay > 0) delay(delay)
        get<T>().emitStickyEvent(event)
    }

    inline fun <reified T> ViewModel.emitStickyEvent(
        crossinline event: suspend () -> T?
    ): Job = viewModelScope.launch {
        get<T>().emitStickyEvent(event())
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun <T> emitEventGlobal(
        name: String,
        event: T?,
        delay: Long = 0L
    ): Job = GlobalScope.launch {
        if (delay > 0) delay(delay)
        get<T>(name).emitEvent(event)
    }

    @OptIn(DelicateCoroutinesApi::class)
    inline fun <T> emitEventGlobal(
        name: String,
        crossinline event: suspend () -> T?
    ): Job = GlobalScope.launch {
        get<T>(name).emitEvent(event())
    }

    @OptIn(DelicateCoroutinesApi::class)
    inline fun <reified T> emitEventGlobal(
        event: T?,
        delay: Long = 0L
    ): Job = GlobalScope.launch {
        if (delay > 0) delay(delay)
        get<T>().emitEvent(event)
    }

    @OptIn(DelicateCoroutinesApi::class)
    inline fun <reified T> emitEventGlobal(
        crossinline event: suspend () -> T?
    ): Job = GlobalScope.launch {
        get<T>().emitEvent(event())
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun <T> emitStickyEventGlobal(
        name: String,
        event: T?,
        delay: Long = 0L
    ): Job = GlobalScope.launch {
        if (delay > 0) delay(delay)
        get<T>(name).emitStickyEvent(event)
    }

    @OptIn(DelicateCoroutinesApi::class)
    inline fun <T> emitStickyEventGlobal(
        name: String,
        crossinline event: suspend () -> T?
    ): Job = GlobalScope.launch {
        get<T>(name).emitStickyEvent(event())
    }

    @OptIn(DelicateCoroutinesApi::class)
    inline fun <reified T> emitStickyEventGlobal(
        event: T?,
        delay: Long = 0L
    ): Job = GlobalScope.launch {
        if (delay > 0) delay(delay)
        get<T>().emitStickyEvent(event)
    }

    @OptIn(DelicateCoroutinesApi::class)
    inline fun <reified T> emitStickyEventGlobal(
        crossinline event: suspend () -> T?
    ): Job = GlobalScope.launch {
        get<T>().emitStickyEvent(event())
    }

    fun <T> LifecycleOwner.emitEventWhenCreated(
        name: String,
        event: T?,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenCreated {
        if (delay > 0) delay(delay)
        get<T>(name).emitEvent(event)
    }

    inline fun <T> LifecycleOwner.emitEventWhenCreated(
        name: String,
        crossinline event: suspend () -> T?
    ): Job = lifecycleScope.launchWhenCreated {
        get<T>(name).emitEvent(event())
    }

    inline fun <reified T> LifecycleOwner.emitEventWhenCreated(
        event: T?,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenCreated {
        if (delay > 0) delay(delay)
        get<T>().emitEvent(event)
    }

    inline fun <reified T> LifecycleOwner.emitEventWhenCreated(
        crossinline event: suspend () -> T?
    ): Job = lifecycleScope.launchWhenCreated {
        get<T>().emitEvent(event())
    }

    fun <T> LifecycleOwner.emitStickyEventWhenCreated(
        name: String,
        event: T?,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenCreated {
        if (delay > 0) delay(delay)
        get<T>(name).emitStickyEvent(event)
    }

    inline fun <T> LifecycleOwner.emitStickyEventWhenCreated(
        name: String,
        crossinline event: suspend () -> T?
    ): Job = lifecycleScope.launchWhenCreated {
        get<T>(name).emitStickyEvent(event())
    }

    inline fun <reified T> LifecycleOwner.emitStickyEventWhenCreated(
        event: T?,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenCreated {
        if (delay > 0) delay(delay)
        get<T>().emitStickyEvent(event)
    }

    inline fun <reified T> LifecycleOwner.emitStickyEventWhenCreated(
        crossinline event: suspend () -> T?
    ): Job = lifecycleScope.launchWhenCreated {
        get<T>().emitStickyEvent(event())
    }

    fun <T> LifecycleOwner.emitEventWhenStarted(
        name: String,
        event: T?,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenStarted {
        if (delay > 0) delay(delay)
        get<T>(name).emitEvent(event)
    }

    inline fun <T> LifecycleOwner.emitEventWhenStarted(
        name: String,
        crossinline event: suspend () -> T?
    ): Job = lifecycleScope.launchWhenStarted {
        get<T>(name).emitEvent(event())
    }

    inline fun <reified T> LifecycleOwner.emitEventWhenStarted(
        event: T?,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenStarted {
        if (delay > 0) delay(delay)
        get<T>().emitEvent(event)
    }

    inline fun <reified T> LifecycleOwner.emitEventWhenStarted(
        crossinline event: suspend () -> T?
    ): Job = lifecycleScope.launchWhenStarted {
        get<T>().emitEvent(event())
    }

    fun <T> LifecycleOwner.emitStickyEventWhenStarted(
        name: String,
        event: T?,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenStarted {
        if (delay > 0) delay(delay)
        get<T>(name).emitStickyEvent(event)
    }

    inline fun <T> LifecycleOwner.emitStickyEventWhenStarted(
        name: String,
        crossinline event: suspend () -> T?
    ): Job = lifecycleScope.launchWhenStarted {
        get<T>(name).emitStickyEvent(event())
    }

    inline fun <reified T> LifecycleOwner.emitStickyEventWhenStarted(
        event: T?,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenStarted {
        if (delay > 0) delay(delay)
        get<T>().emitStickyEvent(event)
    }

    inline fun <reified T> LifecycleOwner.emitStickyEventWhenStarted(
        crossinline event: suspend () -> T?
    ): Job = lifecycleScope.launchWhenStarted {
        get<T>().emitStickyEvent(event())
    }

    fun <T> LifecycleOwner.emitEventWhenResumed(
        name: String,
        event: T?,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenResumed {
        if (delay > 0) delay(delay)
        get<T>(name).emitEvent(event)
    }

    inline fun <T> LifecycleOwner.emitEventWhenResumed(
        name: String,
        crossinline event: suspend () -> T?
    ): Job = lifecycleScope.launchWhenResumed {
        get<T>(name).emitEvent(event())
    }

    inline fun <reified T> LifecycleOwner.emitEventWhenResumed(
        event: T?,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenResumed {
        if (delay > 0) delay(delay)
        get<T>().emitEvent(event)
    }

    inline fun <reified T> LifecycleOwner.emitEventWhenResumed(
        crossinline event: suspend () -> T?
    ): Job = lifecycleScope.launchWhenResumed {
        get<T>().emitEvent(event())
    }

    fun <T> LifecycleOwner.emitStickyEventWhenResumed(
        name: String,
        event: T?,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenResumed {
        if (delay > 0) delay(delay)
        get<T>(name).emitStickyEvent(event)
    }

    inline fun <T> LifecycleOwner.emitStickyEventWhenResumed(
        name: String,
        crossinline event: suspend () -> T?
    ): Job = lifecycleScope.launchWhenResumed {
        get<T>(name).emitStickyEvent(event())
    }

    inline fun <reified T> LifecycleOwner.emitStickyEventWhenResumed(
        event: T?,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenResumed {
        if (delay > 0) delay(delay)
        get<T>().emitStickyEvent(event)
    }

    inline fun <reified T> LifecycleOwner.emitStickyEventWhenResumed(
        crossinline event: suspend () -> T?
    ): Job = lifecycleScope.launchWhenResumed {
        get<T>().emitStickyEvent(event())
    }

    inline fun <T> LifecycleOwner.collectEvent(
        name: String,
        crossinline action: suspend (T?) -> Unit
    ): Job = lifecycleScope.launch {
        get<T>(name).collectEvent(action)
    }

    inline fun <reified T> LifecycleOwner.collectEvent(
        crossinline action: suspend (T?) -> Unit
    ): Job = lifecycleScope.launch {
        get<T>().collectEvent(action)
    }

    inline fun <T> LifecycleOwner.collectStickEvent(
        name: String,
        crossinline action: suspend (T?) -> Unit
    ): Job = lifecycleScope.launch {
        get<T>(name).collectStickyEvent(action)
    }

    inline fun <reified T> LifecycleOwner.collectStickEvent(
        crossinline action: suspend (T?) -> Unit
    ): Job = lifecycleScope.launch {
        get<T>().collectStickyEvent(action)
    }

    inline fun <T> ViewModel.collectEvent(
        name: String,
        crossinline action: suspend (T?) -> Unit
    ): Job = viewModelScope.launch {
        get<T>(name).collectEvent(action)
    }

    inline fun <reified T> ViewModel.collectEvent(
        crossinline action: suspend (T?) -> Unit
    ): Job = viewModelScope.launch {
        get<T>().collectEvent(action)
    }

    inline fun <T> ViewModel.collectStickyEvent(
        name: String,
        crossinline action: suspend (T?) -> Unit
    ): Job = viewModelScope.launch {
        get<T>(name).collectStickyEvent(action)
    }

    inline fun <reified T> ViewModel.collectStickyEvent(
        crossinline action: suspend (T?) -> Unit
    ): Job = viewModelScope.launch {
        get<T>().collectStickyEvent(action)
    }

    @OptIn(DelicateCoroutinesApi::class)
    inline fun <T> collectEventForever(
        name: String,
        crossinline action: suspend (T?) -> Unit
    ): Job = GlobalScope.launch {
        get<T>(name).collectEvent(action)
    }

    @OptIn(DelicateCoroutinesApi::class)
    inline fun <reified T> collectEventForever(
        crossinline action: suspend (T?) -> Unit
    ): Job = GlobalScope.launch {
        get<T>().collectEvent(action)
    }

    @OptIn(DelicateCoroutinesApi::class)
    inline fun <T> collectStickyEventForever(
        name: String,
        crossinline action: suspend (T?) -> Unit
    ): Job = GlobalScope.launch {
        get<T>(name).collectStickyEvent(action)
    }

    @OptIn(DelicateCoroutinesApi::class)
    inline fun <reified T> collectStickyEventForever(
        crossinline action: suspend (T?) -> Unit
    ): Job = GlobalScope.launch {
        get<T>().collectStickyEvent(action)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun clearStickyEvent(name: String) = get<Any>(name).clearStickyEvent()

    inline fun <reified T> clearStickyEvent() = get<T>().clearStickyEvent()

}