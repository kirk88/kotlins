@file:Suppress("UNUSED")

package com.nice.common.event

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect

private const val EVENT_BUFFER_CAPACITY = Int.MAX_VALUE / 2

@PublishedApi
internal class EventFlow<T> {

    @PublishedApi
    internal val flow by lazy {
        MutableSharedFlow<T>(extraBufferCapacity = EVENT_BUFFER_CAPACITY)
    }

    private val stickyFlowLazy = lazy {
        MutableSharedFlow<T>(replay = EVENT_BUFFER_CAPACITY, extraBufferCapacity = EVENT_BUFFER_CAPACITY)
    }

    @PublishedApi
    internal val stickyFlow: MutableSharedFlow<T>
        get() = stickyFlowLazy.value

    suspend fun emit(event: T) {
        flow.emit(event)
        if (stickyFlowLazy.isInitialized()) {
            stickyFlow.emit(event)
        }
    }

    suspend fun emitSticky(event: T) {
        stickyFlow.emit(event)
        flow.emit(event)
    }

    suspend inline fun collect(crossinline action: suspend (T) -> Unit) {
        flow.asSharedFlow().collect(action)
    }

    suspend inline fun collectWithLifecycle(
        lifecycle: Lifecycle,
        minActiveState: Lifecycle.State,
        crossinline action: suspend (T) -> Unit
    ) {
        flow.asSharedFlow().flowWithLifecycle(lifecycle, minActiveState).collect(action)
    }

    suspend inline fun collectSticky(crossinline action: suspend (T) -> Unit) {
        stickyFlow.asSharedFlow().collect(action)
    }

    suspend inline fun collectStickyWithLifecycle(
        lifecycle: Lifecycle,
        minActiveState: Lifecycle.State,
        crossinline action: suspend (T) -> Unit
    ) {
        stickyFlow.asSharedFlow().flowWithLifecycle(lifecycle, minActiveState).collect(action)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun clearStickyCache() {
        stickyFlow.resetReplayCache()
    }

}

@PublishedApi
internal fun <T> EventFlow<T>.asSharedFlow() = flow.asSharedFlow()

@PublishedApi
internal fun <T> EventFlow<T>.asStickySharedFlow() = stickyFlow.asSharedFlow()