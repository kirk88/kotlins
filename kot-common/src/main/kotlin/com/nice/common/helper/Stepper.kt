@file:Suppress("UNUSED")

package com.nice.common.helper

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal interface SuspendedTask {

    suspend fun run()

}

class Stepper {

    private val channel: Channel<SuspendedTask> = Channel(Channel.UNLIMITED)

    fun add(
            context: CoroutineContext = EmptyCoroutineContext,
            block: suspend CoroutineScope.() -> Unit
    ) {
        channel.trySend(DelayedTask(context, block))
    }

    suspend fun start() {
        for (task in channel) {
            task.run()
        }
    }

    fun cancel() {
        channel.cancel()
    }

    fun close() {
        channel.close()
    }

    private class DelayedTask(
            private val context: CoroutineContext,
            private val block: suspend CoroutineScope.() -> Unit
    ) : SuspendedTask {

        override suspend fun run() = withContext(Dispatchers.Main.immediate + context) {
            try {
                block()
            } catch (error: Throwable) {
                val errorHandler = coroutineContext[CoroutineExceptionHandler] ?: throw error
                errorHandler.handleException(coroutineContext, error)
            }
        }

    }

}

fun Stepper.launchIn(scope: CoroutineScope): Job = scope.launch {
    start()
}

fun step(block: Stepper.() -> Unit): Stepper = Stepper().apply(block)