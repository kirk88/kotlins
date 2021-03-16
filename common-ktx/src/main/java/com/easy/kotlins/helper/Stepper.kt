package com.easy.kotlins.helper

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


typealias Task = suspend CoroutineScope.() -> Unit

interface SuspendedTask {

    suspend fun run()

}

class Stepper {

    private val channel: Channel<SuspendedTask> = Channel(Channel.UNLIMITED)
    private var runningJob: Job? = null

    fun add(
        delayed: Long = 0,
        context: CoroutineContext = EmptyCoroutineContext,
        task: Task
    ) {
        channel.offer(DelayedTask(delayed, context, task))
    }

    suspend fun start() {
        for (task in channel) {
            task.run()
        }
    }

    fun launchIn(scope: CoroutineScope) {
        scope.launch {
            for (task in channel) {
                task.run()
            }
        }
    }

    fun cancel() {
        channel.cancel()
        runningJob?.cancel()
    }

    fun close() {
        channel.close()
    }

    private class DelayedTask(
        private val delayed: Long,
        private val context: CoroutineContext,
        private val task: Task
    ) : SuspendedTask {

        override suspend fun run() = withContext(Dispatchers.Main.immediate + context) {
            delay(delayed)

            try {
                task()
            } catch (exception: Exception) {
                val errorHandler = coroutineContext[CoroutineExceptionHandler] ?: throw exception
                errorHandler.handleException(coroutineContext, exception)
            }
        }

    }

}

fun step(block: Stepper.() -> Unit): Stepper = Stepper().apply(block)

fun step(vararg tasks: Task): Stepper = Stepper().apply {
    tasks.forEach { add(task = it) }
}