@file:Suppress("UNUSED")

package com.nice.common.event

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect

@PublishedApi
internal class EventFlow<T> {

    @PublishedApi
    internal val flow by lazy {
        MutableSharedFlow<T>(extraBufferCapacity = Int.MAX_VALUE)
    }

    private val stickyFlowLazy = lazy {
        MutableSharedFlow<T>(replay = 1, extraBufferCapacity = Int.MAX_VALUE)
    }

    @PublishedApi
    internal val stickyFlow: MutableSharedFlow<T>
        get() = stickyFlowLazy.value

    suspend fun emitEvent(event: T) {
        flow.emit(event)
        if (stickyFlowLazy.isInitialized()) {
            stickyFlow.emit(event)
        }
    }

    suspend fun emitStickyEvent(event: T) {
        stickyFlow.emit(event)
        flow.emit(event)
    }

    suspend inline fun collectEvent(crossinline action: suspend (T) -> Unit) {
        flow.asSharedFlow().collect(action)
    }

    suspend inline fun collectStickyEvent(crossinline action: suspend (T) -> Unit) {
        stickyFlow.asSharedFlow().collect(action)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun clearStickyEvent() {
        stickyFlow.resetReplayCache()
    }

}

@PublishedApi
internal fun <T> EventFlow<T>.asSharedFlow() = flow.asSharedFlow()

@PublishedApi
internal fun <T> EventFlow<T>.asStickySharedFlow() = stickyFlow.asSharedFlow()