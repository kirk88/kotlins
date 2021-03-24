package com.easy.kotlins.helper

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
 *
 */
suspend fun <T> execute(
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


object CoroutineExecutors {

    @JvmStatic
    val Default: CoroutineExecutor = DefaultExecutor

    @JvmStatic
    val Main: CoroutineExecutor = MainExecutor

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

internal object MainExecutor : CoroutineExecutor {

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

    private fun createAsync(looper: Looper): Handler {
        if (Build.VERSION.SDK_INT >= 28) {
            return Handler.createAsync(looper)
        }
        try {
            return Handler::class.java.getDeclaredConstructor(
                Looper::class.java, Handler.Callback::class.java,
                Boolean::class.javaPrimitiveType
            ).newInstance(looper, null, true)
        } catch (ignored: IllegalAccessException) {
        } catch (ignored: InstantiationException) {
        } catch (ignored: NoSuchMethodException) {
        } catch (e: InvocationTargetException) {
            return Handler(looper)
        }
        return Handler(looper)
    }

}

internal object UnconfinedExecutor : CoroutineExecutor {

    override fun execute(command: Runnable) {
        command.run()
    }

}