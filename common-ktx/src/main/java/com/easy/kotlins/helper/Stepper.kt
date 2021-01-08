package com.easy.kotlins.helper

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


typealias Task = suspend CoroutineScope.() -> Unit

interface SuspendedTask {

    suspend fun run()

}

class Stepper(context: CoroutineContext = EmptyCoroutineContext) {

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main.immediate + context)

    private val channel: Channel<SuspendedTask> = Channel(Channel.UNLIMITED)
    private var runningJob: Job? = null

    fun add(
        delayed: Long = 0,
        context: CoroutineContext = EmptyCoroutineContext,
        task: Task
    ) {
        channel.offer(DelayedTask(delayed, context, task))
    }

    fun start(): Stepper {
        runningJob = scope.launch {
            for (task in channel) {
                task.run()
            }
        }
        return this
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
            } catch (exception: Throwable) {
                val errorHandler = coroutineContext[CoroutineExceptionHandler] ?: throw exception
                errorHandler.handleException(coroutineContext, exception)
            }
        }

    }

}

fun step(context: CoroutineContext = EmptyCoroutineContext, action: Stepper.() -> Unit): Stepper =
    Stepper(context).apply(action).start()

fun step(vararg tasks: Task, context: CoroutineContext = EmptyCoroutineContext): Stepper =
    Stepper(context).apply {
        tasks.forEach { add(task = it) }
    }.start()
