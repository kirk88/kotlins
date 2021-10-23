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

    fun <T : Any> LifecycleOwner.emitEvent(
        event: T,
        delay: Long = 0L
    ): Job = lifecycleScope.launch {
        if (delay > 0) delay(delay)
        get<T>(event.javaClass.name).emitEvent(event)
    }

    fun <T : Any> LifecycleOwner.emitStickyEvent(
        name: String,
        event: T, delay: Long = 0L
    ): Job = lifecycleScope.launch {
        if (delay > 0) delay(delay)
        get<T>(name).emitStickyEvent(event)
    }

    fun <T : Any> LifecycleOwner.emitStickyEvent(
        event: T,
        delay: Long = 0L
    ): Job = lifecycleScope.launch {
        if (delay > 0) delay(delay)
        get<T>(event.javaClass.name).emitStickyEvent(event)
    }

    fun <T : Any> ViewModel.emitEvent(
        name: String,
        event: T, delay: Long = 0L
    ): Job = viewModelScope.launch {
        if (delay > 0) delay(delay)
        get<T>(name).emitEvent(event)
    }

    fun <T : Any> ViewModel.emitEvent(
        event: T,
        delay: Long = 0L
    ): Job = viewModelScope.launch {
        if (delay > 0) delay(delay)
        get<T>(event.javaClass.name).emitEvent(event)
    }

    fun <T : Any> ViewModel.emitStickyEvent(
        name: String,
        event: T, delay: Long = 0L
    ): Job = viewModelScope.launch {
        if (delay > 0) delay(delay)
        get<T>(name).emitStickyEvent(event)
    }

    fun <T : Any> ViewModel.emitStickyEvent(
        event: T,
        delay: Long = 0L
    ): Job = viewModelScope.launch {
        if (delay > 0) delay(delay)
        get<T>(event.javaClass.name).emitStickyEvent(event)
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun <T : Any> emitEventGlobal(
        name: String,
        event: T, delay: Long = 0L
    ): Job = GlobalScope.launch {
        if (delay > 0) delay(delay)
        get<T>(name).emitEvent(event)
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
    fun <T : Any> emitStickyEventGlobal(
        name: String,
        event: T, delay: Long = 0L
    ): Job = GlobalScope.launch {
        if (delay > 0) delay(delay)
        get<T>(name).emitStickyEvent(event)
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun <T : Any> emitStickyEventGlobal(
        event: T,
        delay: Long = 0L
    ): Job = GlobalScope.launch {
        if (delay > 0) delay(delay)
        get<T>(event.javaClass.name).emitStickyEvent(event)
    }

    fun <T : Any> LifecycleOwner.emitEventWhenCreated(
        name: String,
        event: T, delay: Long = 0L
    ): Job = lifecycleScope.launchWhenCreated {
        if (delay > 0) delay(delay)
        get<T>(name).emitEvent(event)
    }

    fun <T : Any> LifecycleOwner.emitEventWhenCreated(
        event: T,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenCreated {
        if (delay > 0) delay(delay)
        get<T>(event.javaClass.name).emitEvent(event)
    }

    fun <T : Any> LifecycleOwner.emitStickyEventWhenCreated(
        name: String,
        event: T, delay: Long = 0L
    ): Job = lifecycleScope.launchWhenCreated {
        if (delay > 0) delay(delay)
        get<T>(name).emitStickyEvent(event)
    }

    fun <T : Any> LifecycleOwner.emitStickyEventWhenCreated(
        event: T,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenCreated {
        if (delay > 0) delay(delay)
        get<T>(event.javaClass.name).emitStickyEvent(event)
    }

    fun <T : Any> LifecycleOwner.emitEventWhenStarted(
        name: String,
        event: T, delay: Long = 0L
    ): Job = lifecycleScope.launchWhenStarted {
        if (delay > 0) delay(delay)
        get<T>(name).emitEvent(event)
    }

    fun <T : Any> LifecycleOwner.emitEventWhenStarted(
        event: T,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenStarted {
        if (delay > 0) delay(delay)
        get<T>(event.javaClass.name).emitEvent(event)
    }

    fun <T : Any> LifecycleOwner.emitStickyEventWhenStarted(
        name: String,
        event: T, delay: Long = 0L
    ): Job = lifecycleScope.launchWhenStarted {
        if (delay > 0) delay(delay)
        get<T>(name).emitStickyEvent(event)
    }

    fun <T : Any> LifecycleOwner.emitStickyEventWhenStarted(
        event: T,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenStarted {
        if (delay > 0) delay(delay)
        get<T>(event.javaClass.name).emitStickyEvent(event)
    }

    fun <T : Any> LifecycleOwner.emitEventWhenResumed(
        name: String,
        event: T, delay: Long = 0L
    ): Job = lifecycleScope.launchWhenResumed {
        if (delay > 0) delay(delay)
        get<T>(name).emitEvent(event)
    }

    fun <T : Any> LifecycleOwner.emitEventWhenResumed(
        event: T,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenResumed {
        if (delay > 0) delay(delay)
        get<T>(event.javaClass.name).emitEvent(event)
    }

    fun <T : Any> LifecycleOwner.emitStickyEventWhenResumed(
        name: String,
        event: T, delay: Long = 0L
    ): Job = lifecycleScope.launchWhenResumed {
        if (delay > 0) delay(delay)
        get<T>(name).emitStickyEvent(event)
    }

    fun <T : Any> LifecycleOwner.emitStickyEventWhenResumed(
        event: T,
        delay: Long = 0L
    ): Job = lifecycleScope.launchWhenResumed {
        if (delay > 0) delay(delay)
        get<T>(event.javaClass.name).emitStickyEvent(event)
    }

    inline fun <T : Any> LifecycleOwner.collectEvent(
        name: String,
        crossinline action: suspend (T) -> Unit
    ): Job = lifecycleScope.launch {
        get<T>(name).collectEvent(action)
    }

    inline fun <reified T : Any> LifecycleOwner.collectEvent(
        crossinline action: suspend (T) -> Unit
    ): Job = lifecycleScope.launch {
        get<T>(T::class.java.name).collectEvent(action)
    }

    inline fun <T : Any> LifecycleOwner.collectStickEvent(
        name: String,
        crossinline action: suspend (T) -> Unit
    ): Job = lifecycleScope.launch {
        get<T>(name).collectStickyEvent(action)
    }

    inline fun <reified T : Any> LifecycleOwner.collectStickEvent(
        crossinline action: suspend (T) -> Unit
    ): Job = lifecycleScope.launch {
        get<T>(T::class.java.name).collectStickyEvent(action)
    }

    inline fun <T : Any> ViewModel.collectEvent(
        name: String,
        crossinline action: suspend (T) -> Unit
    ): Job = viewModelScope.launch {
        get<T>(name).collectEvent(action)
    }

    inline fun <reified T : Any> ViewModel.collectEvent(
        crossinline action: suspend (T) -> Unit
    ): Job = viewModelScope.launch {
        get<T>(T::class.java.name).collectEvent(action)
    }

    inline fun <T : Any> ViewModel.collectStickyEvent(
        name: String,
        crossinline action: suspend (T) -> Unit
    ): Job = viewModelScope.launch {
        get<T>(name).collectStickyEvent(action)
    }

    inline fun <reified T : Any> ViewModel.collectStickyEvent(
        crossinline action: suspend (T) -> Unit
    ): Job = viewModelScope.launch {
        get<T>(T::class.java.name).collectStickyEvent(action)
    }

    @OptIn(DelicateCoroutinesApi::class)
    inline fun <T : Any> collectEventForever(
        name: String,
        crossinline action: suspend (T) -> Unit
    ): Job = GlobalScope.launch {
        get<T>(name).collectEvent(action)
    }

    @OptIn(DelicateCoroutinesApi::class)
    inline fun <reified T : Any> collectEventForever(
        crossinline action: suspend (T) -> Unit
    ): Job = GlobalScope.launch {
        get<T>(T::class.java.name).collectEvent(action)
    }

    @OptIn(DelicateCoroutinesApi::class)
    inline fun <T : Any> collectStickyEventForever(
        name: String,
        crossinline action: suspend (T) -> Unit
    ): Job = GlobalScope.launch {
        get<T>(name).collectStickyEvent(action)
    }

    @OptIn(DelicateCoroutinesApi::class)
    inline fun <reified T : Any> collectStickyEventForever(
        crossinline action: suspend (T) -> Unit
    ): Job = GlobalScope.launch {
        get<T>(T::class.java.name).collectStickyEvent(action)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun clearStickyEvent(name: String) = get<Any>(name).clearStickyEvent()

    inline fun <reified T : Any> clearStickyEvent() = get<T>(T::class.java.name).clearStickyEvent()

}