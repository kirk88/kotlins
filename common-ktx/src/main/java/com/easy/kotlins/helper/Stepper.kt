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
    private var runningContext: CoroutineContext? = null

    val coroutineContext: CoroutineContext
        get() = runningContext ?: EmptyCoroutineContext

    suspend fun send(
        delayed: Long = 0,
        context: CoroutineContext = EmptyCoroutineContext,
        task: Task
    ) {
        channel.send(DelayedTask(delayed, context, task))
    }

    fun add(
        delayed: Long = 0,
        context: CoroutineContext = EmptyCoroutineContext,
        task: Task
    ) {
        channel.offer(DelayedTask(delayed, context, task))
    }

    suspend fun start(): Stepper {
        for (task in channel) {
            task.run()
        }
        return this
    }

    fun start(context: CoroutineContext): Stepper {
        val scope = CoroutineScope(Dispatchers.Main.immediate + context + SupervisorJob()).also {
            runningContext = it.coroutineContext
        }

        runningJob = scope.launch {
            start()
        }
        return this
    }

    suspend fun receive(): SuspendedTask {
        return channel.receive()
    }

    fun poll(): SuspendedTask? {
        return channel.poll()
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

        override suspend fun run() = withContext(context) {
            delay(delayed)

            try {
                task()
            } catch (exception: Throwable) {
                val errorHandler = context[CoroutineExceptionHandler] ?: throw exception
                errorHandler.handleException(coroutineContext, exception)
            }
        }

    }

}

fun step(context: CoroutineContext = EmptyCoroutineContext, init: Stepper.() -> Unit): Stepper =
    Stepper().apply {
        init()
        start(context)
    }

fun stepOf(vararg tasks: Pair<Long, Task>): Stepper = Stepper().apply {
    tasks.forEach {
        add(it.first, task = it.second)
    }
}