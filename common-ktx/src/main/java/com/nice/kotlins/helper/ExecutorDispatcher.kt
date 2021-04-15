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
    dispatcher: ExecutorDispatcher = CoroutineExecutors.UNCONFINED,
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

    override fun close() {
        (executor as? ExecutorService)?.shutdown()
    }

}

interface MainExecutorDispatcher : ExecutorDispatcher {

    val immediate: MainExecutorDispatcher

}

object CoroutineExecutors {

    @JvmField
    val DEFAULT: ExecutorDispatcher = DefaultExecutorDispatcher

    @JvmStatic
    val Main: MainExecutorDispatcher = MainThreadExecutorDispatcher

    @JvmStatic
    val UNCONFINED: ExecutorDispatcher = UnconfinedExecutorDispatcher

    @JvmStatic
    val IO: ExecutorDispatcher = IOExecutorDispatcher

}

internal object IOExecutorDispatcher : SimpleExecutorDispatcher {

    override val executor: Executor = Executors.newCachedThreadPool(WorkThreadFactory())

    override fun dispatch(command: Runnable) {
        executor.execute(command)
    }

    class WorkThreadFactory : ThreadFactory {

        private val counter: AtomicInteger = AtomicInteger(1)

        override fun newThread(runnable: Runnable): Thread {
            return Thread(runnable, "CoroutineExecutors.IO-${counter.getAndIncrement()}")
        }

    }

}

internal object DefaultExecutorDispatcher : SimpleExecutorDispatcher {

    override val executor: Executor = Executors.newSingleThreadExecutor {
        Thread(it, "CoroutineExecutors.Default")
    }

    override fun dispatch(command: Runnable) {
        executor.execute(command)
    }

}

internal object MainThreadExecutorDispatcher : MainExecutorDispatcher {

    private val delegate: ExecutorDispatcher = HandlerDispatcher()

    override val immediate: MainExecutorDispatcher = object : MainExecutorDispatcher {
        override val immediate: MainExecutorDispatcher get() = this

        override fun dispatch(command: Runnable) {
            if (isMainThread) {
                command.run()
            } else {
                delegate.dispatch(command)
            }
        }

    }

    override fun dispatch(command: Runnable) {
        delegate.dispatch(command)
    }

    class HandlerDispatcher : ExecutorDispatcher {

        private val lock = Any()

        @Volatile
        private var handler: Handler? = null

        override fun dispatch(command: Runnable) {
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
                    return Handler(looper)
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