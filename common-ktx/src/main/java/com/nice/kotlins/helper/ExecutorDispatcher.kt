@file:Suppress("unused")

package com.nice.kotlins.helper

import android.os.Build
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.Closeable
import java.lang.reflect.InvocationTargetException
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
        runCatching(block).onSuccess {
            if (!con.isCancelled) con.resumeWith(Result.success(it))
        }.onFailure {
            if (!con.isCompleted) con.resumeWith(Result.failure(it))
        }
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
                        handler = createAsync(Looper.getMainLooper())
                    }
                }
            }
            handler!!.post(command)
        }

        companion object {

            private fun createAsync(looper: Looper): Handler {
                if (Build.VERSION.SDK_INT >= 28) {
                    return Handler.createAsync(looper)
                }
                try {
                    return Handler::class.java.getDeclaredConstructor(
                            Looper::class.java, Handler.Callback::class.java,
                            Boolean::class.javaPrimitiveType
                    ).newInstance(looper, null, true)
                } catch (_: IllegalAccessException) {
                } catch (_: InstantiationException) {
                } catch (_: NoSuchMethodException) {
                } catch (_: InvocationTargetException) {
                }
                return Handler(looper)
            }

        }

    }

}

internal object UnconfinedExecutorDispatcher : ExecutorDispatcher {

    override fun dispatch(command: Runnable) {
        command.run()
    }

}