@file:Suppress("UNUSED", "UNCHECKED_CAST")

package com.nice.common.event

import androidx.lifecycle.*
import kotlinx.coroutines.*

@PublishedApi
internal object FlowEventBus {

    val flows = mutableMapOf<String, EventFlow<*>>()

    fun <T> eventFlow(name: String): EventFlow<T?> =
        flows.getOrPut(name) { EventFlow<T?>() } as EventFlow<T?>

    inline fun <reified T> eventFlow(): EventFlow<T?> =
        flows.getOrPut(T::class.java.name) { EventFlow<T?>() } as EventFlow<T?>

}

fun <T> LifecycleOwner.emitEvent(
    name: String,
    event: T?,
    delay: Long = 0L
): Job = lifecycleScope.launch {
    delay(delay)
    FlowEventBus.eventFlow<T>(name).emit(event)
}

inline fun <T> LifecycleOwner.emitEvent(
    name: String,
    crossinline event: suspend () -> T?
): Job = lifecycleScope.launch {
    FlowEventBus.eventFlow<T>(name).emit(event())
}

inline fun <reified T> LifecycleOwner.emitEvent(
    event: T?,
    delay: Long = 0L
): Job = lifecycleScope.launch {
    delay(delay)
    FlowEventBus.eventFlow<T>().emit(event)
}

inline fun <reified T> LifecycleOwner.emitEvent(
    crossinline event: suspend () -> T?
): Job = lifecycleScope.launch {
    FlowEventBus.eventFlow<T>().emit(event())
}

fun <T> LifecycleOwner.emitStickyEvent(
    name: String,
    event: T?,
    delay: Long = 0L
): Job = lifecycleScope.launch {
    delay(delay)
    FlowEventBus.eventFlow<T>(name).emitSticky(event)
}

inline fun <T> LifecycleOwner.emitStickyEvent(
    name: String,
    crossinline event: suspend () -> T?
): Job = lifecycleScope.launch {
    FlowEventBus.eventFlow<T>(name).emitSticky(event())
}

inline fun <reified T> LifecycleOwner.emitStickyEvent(
    event: T?,
    delay: Long = 0L
): Job = lifecycleScope.launch {
    delay(delay)
    FlowEventBus.eventFlow<T>().emitSticky(event)
}

inline fun <reified T> LifecycleOwner.emitStickyEvent(
    crossinline event: suspend () -> T?
): Job = lifecycleScope.launch {
    FlowEventBus.eventFlow<T>().emitSticky(event())
}

fun <T> ViewModel.emitEvent(
    name: String,
    event: T?,
    delay: Long = 0L
): Job = viewModelScope.launch {
    delay(delay)
    FlowEventBus.eventFlow<T>(name).emit(event)
}

inline fun <T> ViewModel.emitEvent(
    name: String,
    crossinline event: suspend () -> T?
): Job = viewModelScope.launch {
    FlowEventBus.eventFlow<T>(name).emit(event())
}

inline fun <reified T> ViewModel.emitEvent(
    event: T?,
    delay: Long = 0L
): Job = viewModelScope.launch {
    delay(delay)
    FlowEventBus.eventFlow<T>().emit(event)
}

inline fun <reified T> ViewModel.emitEvent(
    crossinline event: suspend () -> T?
): Job = viewModelScope.launch {
    FlowEventBus.eventFlow<T>().emit(event())
}

fun <T> ViewModel.emitStickyEvent(
    name: String,
    event: T?,
    delay: Long = 0L
): Job = viewModelScope.launch {
    delay(delay)
    FlowEventBus.eventFlow<T>(name).emitSticky(event)
}

inline fun <T> ViewModel.emitStickyEvent(
    name: String,
    crossinline event: suspend () -> T?
): Job = viewModelScope.launch {
    FlowEventBus.eventFlow<T>(name).emitSticky(event())
}

inline fun <reified T> ViewModel.emitStickyEvent(
    event: T?,
    delay: Long = 0L
): Job = viewModelScope.launch {
    delay(delay)
    FlowEventBus.eventFlow<T>().emitSticky(event)
}

inline fun <reified T> ViewModel.emitStickyEvent(
    crossinline event: suspend () -> T?
): Job = viewModelScope.launch {
    FlowEventBus.eventFlow<T>().emitSticky(event())
}

@OptIn(DelicateCoroutinesApi::class)
fun <T> emitEventGlobal(
    name: String,
    event: T?,
    delay: Long = 0L
): Job = GlobalScope.launch {
    delay(delay)
    FlowEventBus.eventFlow<T>(name).emit(event)
}

@OptIn(DelicateCoroutinesApi::class)
inline fun <T> emitEventGlobal(
    name: String,
    crossinline event: suspend () -> T?
): Job = GlobalScope.launch {
    FlowEventBus.eventFlow<T>(name).emit(event())
}

@OptIn(DelicateCoroutinesApi::class)
inline fun <reified T> emitEventGlobal(
    event: T?,
    delay: Long = 0L
): Job = GlobalScope.launch {
    delay(delay)
    FlowEventBus.eventFlow<T>().emit(event)
}

@OptIn(DelicateCoroutinesApi::class)
inline fun <reified T> emitEventGlobal(
    crossinline event: suspend () -> T?
): Job = GlobalScope.launch {
    FlowEventBus.eventFlow<T>().emit(event())
}

@OptIn(DelicateCoroutinesApi::class)
fun <T> emitStickyEventGlobal(
    name: String,
    event: T?,
    delay: Long = 0L
): Job = GlobalScope.launch {
    delay(delay)
    FlowEventBus.eventFlow<T>(name).emitSticky(event)
}

@OptIn(DelicateCoroutinesApi::class)
inline fun <T> emitStickyEventGlobal(
    name: String,
    crossinline event: suspend () -> T?
): Job = GlobalScope.launch {
    FlowEventBus.eventFlow<T>(name).emitSticky(event())
}

@OptIn(DelicateCoroutinesApi::class)
inline fun <reified T> emitStickyEventGlobal(
    event: T?,
    delay: Long = 0L
): Job = GlobalScope.launch {
    delay(delay)
    FlowEventBus.eventFlow<T>().emitSticky(event)
}

@OptIn(DelicateCoroutinesApi::class)
inline fun <reified T> emitStickyEventGlobal(
    crossinline event: suspend () -> T?
): Job = GlobalScope.launch {
    FlowEventBus.eventFlow<T>().emitSticky(event())
}

suspend fun <T> emitEvent(
    name: String,
    event: T?
) = FlowEventBus.eventFlow<T>(name).emit(event)

suspend inline fun <reified T> emitEvent(
    event: T?
) = FlowEventBus.eventFlow<T>().emit(event)

suspend fun <T> emitStickyEvent(
    name: String,
    event: T?
) = FlowEventBus.eventFlow<T>(name).emitSticky(event)

suspend inline fun <reified T> emitStickyEvent(
    event: T?
) = FlowEventBus.eventFlow<T>().emitSticky(event)

fun <T> LifecycleOwner.emitEventWhenCreated(
    name: String,
    event: T?,
    delay: Long = 0L
): Job = lifecycleScope.launchWhenCreated {
    delay(delay)
    FlowEventBus.eventFlow<T>(name).emit(event)
}

inline fun <T> LifecycleOwner.emitEventWhenCreated(
    name: String,
    crossinline event: suspend () -> T?
): Job = lifecycleScope.launchWhenCreated {
    FlowEventBus.eventFlow<T>(name).emit(event())
}

inline fun <reified T> LifecycleOwner.emitEventWhenCreated(
    event: T?,
    delay: Long = 0L
): Job = lifecycleScope.launchWhenCreated {
    delay(delay)
    FlowEventBus.eventFlow<T>().emit(event)
}

inline fun <reified T> LifecycleOwner.emitEventWhenCreated(
    crossinline event: suspend () -> T?
): Job = lifecycleScope.launchWhenCreated {
    FlowEventBus.eventFlow<T>().emit(event())
}

fun <T> LifecycleOwner.emitStickyEventWhenCreated(
    name: String,
    event: T?,
    delay: Long = 0L
): Job = lifecycleScope.launchWhenCreated {
    delay(delay)
    FlowEventBus.eventFlow<T>(name).emitSticky(event)
}

inline fun <T> LifecycleOwner.emitStickyEventWhenCreated(
    name: String,
    crossinline event: suspend () -> T?
): Job = lifecycleScope.launchWhenCreated {
    FlowEventBus.eventFlow<T>(name).emitSticky(event())
}

inline fun <reified T> LifecycleOwner.emitStickyEventWhenCreated(
    event: T?,
    delay: Long = 0L
): Job = lifecycleScope.launchWhenCreated {
    delay(delay)
    FlowEventBus.eventFlow<T>().emitSticky(event)
}

inline fun <reified T> LifecycleOwner.emitStickyEventWhenCreated(
    crossinline event: suspend () -> T?
): Job = lifecycleScope.launchWhenCreated {
    FlowEventBus.eventFlow<T>().emitSticky(event())
}

fun <T> LifecycleOwner.emitEventWhenStarted(
    name: String,
    event: T?,
    delay: Long = 0L
): Job = lifecycleScope.launchWhenStarted {
    delay(delay)
    FlowEventBus.eventFlow<T>(name).emit(event)
}

inline fun <T> LifecycleOwner.emitEventWhenStarted(
    name: String,
    crossinline event: suspend () -> T?
): Job = lifecycleScope.launchWhenStarted {
    FlowEventBus.eventFlow<T>(name).emit(event())
}

inline fun <reified T> LifecycleOwner.emitEventWhenStarted(
    event: T?,
    delay: Long = 0L
): Job = lifecycleScope.launchWhenStarted {
    delay(delay)
    FlowEventBus.eventFlow<T>().emit(event)
}

inline fun <reified T> LifecycleOwner.emitEventWhenStarted(
    crossinline event: suspend () -> T?
): Job = lifecycleScope.launchWhenStarted {
    FlowEventBus.eventFlow<T>().emit(event())
}

fun <T> LifecycleOwner.emitStickyEventWhenStarted(
    name: String,
    event: T?,
    delay: Long = 0L
): Job = lifecycleScope.launchWhenStarted {
    delay(delay)
    FlowEventBus.eventFlow<T>(name).emitSticky(event)
}

inline fun <T> LifecycleOwner.emitStickyEventWhenStarted(
    name: String,
    crossinline event: suspend () -> T?
): Job = lifecycleScope.launchWhenStarted {
    FlowEventBus.eventFlow<T>(name).emitSticky(event())
}

inline fun <reified T> LifecycleOwner.emitStickyEventWhenStarted(
    event: T?,
    delay: Long = 0L
): Job = lifecycleScope.launchWhenStarted {
    delay(delay)
    FlowEventBus.eventFlow<T>().emitSticky(event)
}

inline fun <reified T> LifecycleOwner.emitStickyEventWhenStarted(
    crossinline event: suspend () -> T?
): Job = lifecycleScope.launchWhenStarted {
    FlowEventBus.eventFlow<T>().emitSticky(event())
}

fun <T> LifecycleOwner.emitEventWhenResumed(
    name: String,
    event: T?,
    delay: Long = 0L
): Job = lifecycleScope.launchWhenResumed {
    delay(delay)
    FlowEventBus.eventFlow<T>(name).emit(event)
}

inline fun <T> LifecycleOwner.emitEventWhenResumed(
    name: String,
    crossinline event: suspend () -> T?
): Job = lifecycleScope.launchWhenResumed {
    FlowEventBus.eventFlow<T>(name).emit(event())
}

inline fun <reified T> LifecycleOwner.emitEventWhenResumed(
    event: T?,
    delay: Long = 0L
): Job = lifecycleScope.launchWhenResumed {
    delay(delay)
    FlowEventBus.eventFlow<T>().emit(event)
}

inline fun <reified T> LifecycleOwner.emitEventWhenResumed(
    crossinline event: suspend () -> T?
): Job = lifecycleScope.launchWhenResumed {
    FlowEventBus.eventFlow<T>().emit(event())
}

fun <T> LifecycleOwner.emitStickyEventWhenResumed(
    name: String,
    event: T?,
    delay: Long = 0L
): Job = lifecycleScope.launchWhenResumed {
    delay(delay)
    FlowEventBus.eventFlow<T>(name).emitSticky(event)
}

inline fun <T> LifecycleOwner.emitStickyEventWhenResumed(
    name: String,
    crossinline event: suspend () -> T?
): Job = lifecycleScope.launchWhenResumed {
    FlowEventBus.eventFlow<T>(name).emitSticky(event())
}

inline fun <reified T> LifecycleOwner.emitStickyEventWhenResumed(
    event: T?,
    delay: Long = 0L
): Job = lifecycleScope.launchWhenResumed {
    delay(delay)
    FlowEventBus.eventFlow<T>().emitSticky(event)
}

inline fun <reified T> LifecycleOwner.emitStickyEventWhenResumed(
    crossinline event: suspend () -> T?
): Job = lifecycleScope.launchWhenResumed {
    FlowEventBus.eventFlow<T>().emitSticky(event())
}

fun <T> LifecycleOwner.collectEvent(
    name: String,
    receiver: FlowReceiver<T?>
): Job = lifecycleScope.launch {
    FlowEventBus.eventFlow<T>(name).collect(receiver)
}

inline fun <reified T> LifecycleOwner.collectEvent(
    receiver: FlowReceiver<T?>
): Job = lifecycleScope.launch {
    FlowEventBus.eventFlow<T>().collect(receiver)
}

fun <T> LifecycleOwner.collectStickEvent(
    name: String,
    receiver: FlowReceiver<T?>
): Job = lifecycleScope.launch {
    FlowEventBus.eventFlow<T>(name).collectSticky(receiver)
}

fun <T> LifecycleOwner.collectStickEventWithLifecycle(
    name: String,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    receiver: FlowReceiver<T?>
): Job = lifecycleScope.launch {
    FlowEventBus.eventFlow<T>(name).collectStickyWithLifecycle(lifecycle, minActiveState, receiver)
}

inline fun <reified T> LifecycleOwner.collectStickEvent(
    receiver: FlowReceiver<T?>
): Job = lifecycleScope.launch {
    FlowEventBus.eventFlow<T>().collectSticky(receiver)
}

inline fun <reified T> LifecycleOwner.collectStickEventWithLifecycle(
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    receiver: FlowReceiver<T?>
): Job = lifecycleScope.launch {
    FlowEventBus.eventFlow<T>().collectStickyWithLifecycle(lifecycle, minActiveState, receiver)
}

fun <T> ViewModel.collectEvent(
    name: String,
    receiver: FlowReceiver<T?>
): Job = viewModelScope.launch {
    FlowEventBus.eventFlow<T>(name).collect(receiver)
}

inline fun <reified T> ViewModel.collectEvent(
    receiver: FlowReceiver<T?>
): Job = viewModelScope.launch {
    FlowEventBus.eventFlow<T>().collect(receiver)
}

fun <T> ViewModel.collectStickyEvent(
    name: String,
    receiver: FlowReceiver<T?>
): Job = viewModelScope.launch {
    FlowEventBus.eventFlow<T>(name).collectSticky(receiver)
}

inline fun <reified T> ViewModel.collectStickyEvent(
    receiver: FlowReceiver<T?>
): Job = viewModelScope.launch {
    FlowEventBus.eventFlow<T>().collectSticky(receiver)
}

@OptIn(DelicateCoroutinesApi::class)
fun <T> collectEventForever(
    name: String,
    receiver: FlowReceiver<T?>
): Job = GlobalScope.launch {
    FlowEventBus.eventFlow<T>(name).collect(receiver)
}

@OptIn(DelicateCoroutinesApi::class)
inline fun <reified T> collectEventForever(
    receiver: FlowReceiver<T?>
): Job = GlobalScope.launch {
    FlowEventBus.eventFlow<T>().collect(receiver)
}

@OptIn(DelicateCoroutinesApi::class)
fun <T> collectStickyEventForever(
    name: String,
    receiver: FlowReceiver<T?>
): Job = GlobalScope.launch {
    FlowEventBus.eventFlow<T>(name).collectSticky(receiver)
}

@OptIn(DelicateCoroutinesApi::class)
inline fun <reified T> collectStickyEventForever(
    receiver: FlowReceiver<T?>
): Job = GlobalScope.launch {
    FlowEventBus.eventFlow<T>().collectSticky(receiver)
}

suspend fun <T> collectEvent(
    name: String,
    receiver: FlowReceiver<T?>
) = FlowEventBus.eventFlow<T>(name).collect(receiver)

suspend inline fun <reified T> collectEvent(
    receiver: FlowReceiver<T?>
) = FlowEventBus.eventFlow<T>().collect(receiver)

suspend fun <T> collectStickyEvent(
    name: String,
    receiver: FlowReceiver<T?>
) = FlowEventBus.eventFlow<T>(name).collectSticky(receiver)

suspend inline fun <reified T> collectStickyEvent(
    receiver: FlowReceiver<T?>
) = FlowEventBus.eventFlow<T>().collectSticky(receiver)

fun clearStickyEvent(name: String) = FlowEventBus.eventFlow<Any>(name).clearStickyCache()

inline fun <reified T> clearStickyEvent() = FlowEventBus.eventFlow<T>().clearStickyCache()