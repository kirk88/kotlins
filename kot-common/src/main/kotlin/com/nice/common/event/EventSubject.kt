package com.nice.common.event

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect

class EventSubject {

    private val eventFlow = MutableSharedFlow<Any>(0, extraBufferCapacity = Int.MAX_VALUE)
    private val stickyEventFlow by lazy {
        MutableSharedFlow<Any>(1, extraBufferCapacity = Int.MAX_VALUE)
    }

    val flow: SharedFlow<Any> get() = eventFlow.asSharedFlow()
    val stickyFlow: SharedFlow<Any> get() = stickyEventFlow.asSharedFlow()

    suspend fun produceEvent(event: Any) = eventFlow.emit(event)

    suspend fun produceStickyEvent(event: Any) = stickyEventFlow.emit(event)

    suspend fun subscribeEvent(action: suspend (Any) -> Unit) {
        eventFlow.collect(action)
    }

    suspend fun subscribeStickyEvent(action: suspend (Any) -> Unit) {
        stickyEventFlow.collect(action)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun clearStickyEvent() {
        stickyEventFlow.resetReplayCache()
    }

}