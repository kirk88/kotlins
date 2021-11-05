@file:Suppress("UNUSED")

package com.nice.common.event

import android.util.ArrayMap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.nice.common.helper.orZero
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onSubscription
import java.util.*

@PublishedApi
internal class EventFlow<T> {

    private val eventsLazy = lazy {
        MutableSharedFlow<T>(extraBufferCapacity = Int.MAX_VALUE)
    }
    private val events by eventsLazy

    private var replayEvents: LinkedList<T>? = null
    private val repliedCounts = ArrayMap<FlowReceiver<T>, Int>()

    suspend fun emit(value: T) {
        events.emit(value)
    }

    suspend fun emitSticky(value: T) {
        enqueueReply(value)
        events.emit(value)
    }

    suspend fun collect(receiver: FlowReceiver<T>) {
        events.collect(receiver::onReceive)
    }

    suspend fun collectSticky(receiver: FlowReceiver<T>) {
        events.flowWithReplay(receiver).collect(receiver::onReceive)
    }

    suspend fun collectStickyWithLifecycle(
        lifecycle: Lifecycle,
        minActiveState: Lifecycle.State,
        receiver: FlowReceiver<T>
    ) {
        lifecycle.repeatOnLifecycle(minActiveState) {
            events.flowWithReplay(receiver).collect(receiver::onReceive)
        }
    }

    private suspend fun SharedFlow<T>.flowWithReplay(receiver: FlowReceiver<T>) = onSubscription {
        val replayEvents = replayEvents ?: return@onSubscription
        val repliedCount = getRepliedCount(receiver)
        try {
            replayEvents.drop(repliedCount).forEach { emit(it) }
        } finally {
            updateRepliedCount(receiver)
        }
    }

    private fun getRepliedCount(receiver: FlowReceiver<T>): Int {
        val repliedCount: Int
        synchronized(this) {
            repliedCount = repliedCounts[receiver].orZero()
        }
        return repliedCount
    }

    private fun updateRepliedCount(receiver: FlowReceiver<T>) {
        synchronized(this) {
            if (replayEvents != null) {
                repliedCounts[receiver] = replayEvents!!.size
            }
        }
    }

    private fun enqueueReply(value: T) {
        synchronized(this) {
            val cache = replayEvents ?: LinkedList<T>().also { replayEvents = it }
            cache.add(value)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun clearStickyCache() {
        synchronized(this) {
            replayEvents?.clear()
            replayEvents = null
        }
    }

}

fun interface FlowReceiver<T> {
    suspend fun onReceive(value: T)
}

suspend operator fun <T> FlowReceiver<T>.invoke(value: T) = onReceive(value)