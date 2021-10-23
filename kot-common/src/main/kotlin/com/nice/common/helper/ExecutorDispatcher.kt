@file:Suppress("UNUSED")

package com.nice.common.helper

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.Closeable
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * appropriate blocking method call
 *
 * For example, the following code:
 *
 * suspend fun get(): T = execute<T> {
 *      //run blocking
 *      return T
 * }
 */
suspend fun <T> suspendBlocking(
    dispatcher: ExecutorDispatcher = ExecutorDispatchers.Unconfined,
    block: () -> T
): T = suspendCancellableCoroutine { con ->
    dispatcher.dispatch {
        con.resumeWith(runCatching(block))
    }
}

interface ExecutorDispatcher {

    fun dispatch(command: Runnable)

}

interface SimpleExecutorDispatcher : ExecutorDispatcher, Closeable {
    val executor: Executor

    override fun dispatch(command: Runnable) {
        executor.execute(command)
    }

    override fun close() {
        (executor as? ExecutorService)?.shutdown()
    }
}

interface HandlerExecutorDispatcher : SimpleExecutorDispatcher {

    val immediate: ExecutorDispatcher

}

object ExecutorDispatchers {

    val Default: ExecutorDispatcher = DefaultExecutorDispatcher

    val Main: HandlerExecutorDispatcher = MainThreadExecutorDispatcher

    val Unconfined: ExecutorDispatcher = UnconfinedExecutorDispatcher

    val IO: ExecutorDispatcher = IOExecutorDispatcher

}

internal object DefaultExecutorDispatcher : SimpleExecutorDispatcher {

    override val executor: Executor = Executors.newSingleThreadExecutor {
        Thread(it, "ExecutorDispatchers.Default")
    }

}

internal object IOExecutorDispatcher : SimpleExecutorDispatcher {

    override val executor: Executor = Executors.newCachedThreadPool(WorkThreadFactory())

    class WorkThreadFactory : ThreadFactory {

        private val counter: AtomicInteger = AtomicInteger(1)

        override fun newThread(runnable: Runnable): Thread {
            return Thread(runnable, "ExecutorDispatchers.IO-${counter.getAndIncrement()}")
        }

    }

}

internal object MainThreadExecutorDispatcher : HandlerExecutorDispatcher {

    override val executor: Executor = HandlerExecutor()

    override val immediate: ExecutorDispatcher = object : ExecutorDispatcher {
        override fun dispatch(command: Runnable) {
            if (isMainThread) {
                command.run()
            } else {
                executor.execute(command)
            }
        }
    }

    class HandlerExecutor : Executor {

        private val lock = Any()

        @Volatile
        private var handler: Handler? = null

        override fun execute(command: Runnable) {
            if (handler == null) {
                synchronized(lock) {
                    if (handler == null) {
                        handler = Looper.getMainLooper().asHandler(true)
                    }
                }
            }
            handler!!.post(command)
        }

    }

}

internal object UnconfinedExecutorDispatcher : ExecutorDispatcher {

    override fun dispatch(command: Runnable) {
        command.run()
    }

}