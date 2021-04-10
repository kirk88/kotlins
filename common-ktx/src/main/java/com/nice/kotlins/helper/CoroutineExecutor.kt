@file:Suppress("unused")

package com.nice.kotlins.helper

import android.os.Build
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.Executor
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
suspend fun <T> suspendExecutor(
    executor: CoroutineExecutor = CoroutineExecutors.Unconfined,
    block: () -> T
): T = suspendCancellableCoroutine { con ->
    executor.execute {
        runCatching(block).onSuccess {
            if (!con.isCancelled) con.resumeWith(Result.success(it))
        }.onFailure {
            if (!con.isCompleted) con.resumeWith(Result.failure(it))
        }
    }
}


interface CoroutineExecutor {

    fun execute(command: Runnable)

}

interface MainCoroutineExecutor : CoroutineExecutor {

    val immediate: MainCoroutineExecutor

}

object CoroutineExecutors {

    @JvmField
    val Default: CoroutineExecutor = DefaultExecutor

    @JvmStatic
    val Main: MainCoroutineExecutor = MainExecutor

    @JvmStatic
    val Unconfined: CoroutineExecutor = UnconfinedExecutor

    @JvmStatic
    val IO: CoroutineExecutor = IOExecutor

}

internal object IOExecutor : CoroutineExecutor {

    private val delegate: Executor = Executors.newCachedThreadPool(WorkThreadFactory())

    override fun execute(command: Runnable) {
        delegate.execute(command)
    }

    class WorkThreadFactory : ThreadFactory {

        private val counter: AtomicInteger = AtomicInteger(1)

        override fun newThread(runnable: Runnable): Thread {
            return Thread(runnable, "CoroutineExecutors.IO-${counter.getAndIncrement()}")
        }

    }

}

internal object DefaultExecutor : CoroutineExecutor {

    private val delegate: Executor = Executors.newSingleThreadExecutor {
        Thread(it, "CoroutineExecutors.Default")
    }

    override fun execute(command: Runnable) {
        delegate.execute(command)
    }

}

internal object MainExecutor : MainCoroutineExecutor {

    private val delegate: CoroutineExecutor = MainThreadExecutor()

    override val immediate: MainCoroutineExecutor = object : MainCoroutineExecutor {
        override val immediate: MainCoroutineExecutor get() = this

        override fun execute(command: Runnable) {
            if (Looper.getMainLooper() === Looper.myLooper()) {
                command.run()
            } else {
                delegate.execute(command)
            }
        }
    }

    override fun execute(command: Runnable) {
        delegate.execute(command)
    }

    class MainThreadExecutor : CoroutineExecutor {

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
                    return Handler(looper)
                }
                return Handler(looper)
            }

        }

    }

}

internal object UnconfinedExecutor : CoroutineExecutor {

    override fun execute(command: Runnable) {
        command.run()
    }

}