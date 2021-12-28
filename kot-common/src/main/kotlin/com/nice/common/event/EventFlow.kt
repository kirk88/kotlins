@file:Suppress("UNUSED")

package com.nice.common.event

import android.util.ArrayMap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.nice.common.helper.orZero
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

@PublishedApi
internal class EventFlow<T> {

    private val events by lazy {
        MutableSharedFlow<T>(extraBufferCapacity = Int.MAX_VALUE)
    }

    private val mutex = Mutex()

    private val repliedCounts = ArrayMap<EventCollector<T>, Int>()
    private var replayEvents: LinkedList<T>? = null

    suspend fun emit(value: T) {
        events.emit(value)
    }

    suspend fun emitSticky(value: T) {
        enqueueReply(value)
        events.emit(value)
    }

    suspend fun collect(collector: EventCollector<T>) {
        events.collect(collector::onCollect)
    }

    suspend fun collectSticky(collector: EventCollector<T>) {
        events.flowWithReplay(collector).collect(collector::onCollect)
    }

    suspend fun collectStickyWithLifecycle(
        lifecycle: Lifecycle,
        minActiveState: Lifecycle.State,
        collector: EventCollector<T>
    ) {
        lifecycle.repeatOnLifecycle(minActiveState) {
            events.flowWithReplay(collector).collect(collector::onCollect)
        }
    }

    private suspend fun SharedFlow<T>.flowWithReplay(collector: EventCollector<T>) = onSubscription {
        val replayEvents = replayEvents ?: return@onSubscription
        val repliedCount = getRepliedCount(collector)
        try {
            replayEvents.drop(repliedCount).forEach { emit(it) }
        } finally {
            updateRepliedCount(collector)
        }
    }

    private suspend fun getRepliedCount(collector: EventCollector<T>): Int = mutex.withLock {
        repliedCounts[collector].orZero()
    }

    private suspend fun updateRepliedCount(collector: EventCollector<T>) = mutex.withLock {
        repliedCounts[collector] = replayEvents?.size.orZero()
    }

    private suspend fun enqueueReply(value: T) = mutex.withLock {
        val cache = replayEvents ?: LinkedList<T>().also { replayEvents = it }
        cache.add(value)
    }

    suspend fun clearStickyCache() = mutex.withLock {
        replayEvents?.clear()
        replayEvents = null
    }

}

fun interface EventCollector<T> {
    suspend fun onCollect(value: T)
}

suspend operator fun <T> EventCollector<T>.invoke(value: T) = onCollect(value)