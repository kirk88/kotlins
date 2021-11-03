@file:Suppress("UNUSED")

package com.nice.common.event

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect

private const val EVENT_BUFFER_CAPACITY = Int.MAX_VALUE / 2

@PublishedApi
internal class EventFlow<T> {

    private val eventsLazy = lazy {
        MutableSharedFlow<T>(extraBufferCapacity = EVENT_BUFFER_CAPACITY)
    }

    private val stickyEventsLazy = lazy {
        MutableSharedFlow<T>(replay = EVENT_BUFFER_CAPACITY)
    }

    private val events by eventsLazy
    val sharedEvents: SharedFlow<T> by lazy { events.asSharedFlow() }

    private val stickyEvents by stickyEventsLazy
    val sharedStickyEvents: SharedFlow<T> by lazy { stickyEvents.asSharedFlow() }

    suspend fun emit(event: T) {
        events.emit(event)
        if (stickyEventsLazy.isInitialized()) {
            stickyEvents.emit(event)
        }
    }

    suspend fun emitSticky(event: T) {
        stickyEvents.emit(event)
        if (eventsLazy.isInitialized()) {
            events.emit(event)
        }
    }

    suspend inline fun collect(crossinline action: suspend (T) -> Unit) {
        sharedEvents.collect(action)
    }

    suspend inline fun collectWithLifecycle(
        lifecycle: Lifecycle,
        minActiveState: Lifecycle.State,
        crossinline action: suspend (T) -> Unit
    ) {
        sharedEvents.flowWithLifecycle(lifecycle, minActiveState).collect(action)
    }

    suspend inline fun collectSticky(crossinline action: suspend (T) -> Unit) {
        sharedStickyEvents.collect(action)
    }

    suspend inline fun collectStickyWithLifecycle(
        lifecycle: Lifecycle,
        minActiveState: Lifecycle.State,
        crossinline action: suspend (T) -> Unit
    ) {
        sharedStickyEvents.flowWithLifecycle(lifecycle, minActiveState).collect(action)
    }

}