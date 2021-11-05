@file:Suppress("UNUSED")

package com.nice.common.event

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onSubscription

typealias FlowReceiver<T> = suspend (T) -> Unit

@PublishedApi
internal class EventFlow<T> {

    private val eventsLazy = lazy {
        MutableSharedFlow<T>(extraBufferCapacity = Int.MAX_VALUE)
    }
    private val events by eventsLazy

    private val eventReplayCache = mutableListOf<T>()
    private val eventRepliedCounts = mutableMapOf<FlowReceiver<T>, Int>()

    suspend fun emit(value: T) {
        events.emit(value)
    }

    suspend fun emitSticky(value: T) {
        eventReplayCache.add(value)
        events.emit(value)
    }

    suspend fun collect(receiver: FlowReceiver<T>) {
        events.collect(receiver)
    }

    suspend fun collectSticky(receiver: FlowReceiver<T>) {
        events.onSubscription { replayIfNeed(receiver) }.collect(receiver)
    }

    suspend fun collectStickyWithLifecycle(
        lifecycle: Lifecycle,
        minActiveState: Lifecycle.State,
        receiver: FlowReceiver<T>
    ) {
        lifecycle.repeatOnLifecycle(minActiveState) {
            events.onSubscription { replayIfNeed(receiver) }.collect(receiver)
        }
    }

    private suspend fun FlowCollector<T>.replayIfNeed(receiver: FlowReceiver<T>) {
        eventReplayCache
            .drop(eventRepliedCounts[receiver] ?: 0)
            .forEach { emit(it) }
        eventRepliedCounts[receiver] = eventReplayCache.size
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun clearStickyCache() {
        eventReplayCache.clear()
    }

}