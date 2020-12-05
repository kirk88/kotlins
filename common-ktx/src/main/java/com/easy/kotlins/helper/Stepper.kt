package com.easy.kotlins.helper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


fun interface Task {

    fun run()

}

interface SuspendedTask {

    suspend fun run()

}

class Stepper {

    private val channel: Channel<SuspendedTask> = Channel(Channel.UNLIMITED)

    fun add(delayed: Long = 0, context: CoroutineContext = EmptyCoroutineContext, task: Task): Stepper {
        channel.offer(DelayedTask(delayed, context, task))
        return this
    }

    suspend fun run(): Stepper {
        for (task in channel) {
            task.run()
        }
        return this
    }

    fun run(scope: CoroutineScope): Stepper {
        scope.launch {
            run()
        }
        return this
    }

    suspend fun take(): SuspendedTask {
        return channel.receive()
    }

    fun poll(): SuspendedTask? {
        return channel.poll()
    }

    fun cancel() {
        channel.cancel()
    }

    fun close() {
        channel.close()
    }

    private class DelayedTask(private val delayed: Long, private val context: CoroutineContext, private val task: Task) : SuspendedTask {

        override suspend fun run() {
            withContext(context) {
                delay(delayed)

                task.run()
            }
        }

    }

}

fun step(scope: CoroutineScope, init: Stepper.() -> Unit): Stepper = Stepper().apply {
    init()
    run(scope)
}

fun stepOf(vararg tasks: Pair<Long, Task>): Stepper = Stepper().apply {
    tasks.forEach {
        add(it.first, task = it.second)
    }
}