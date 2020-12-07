package com.easy.kotlins.helper

import com.easy.kotlins.event.*
import com.easy.kotlins.http.OkFaker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import okhttp3.Response
import java.io.Serializable
import java.util.concurrent.atomic.AtomicInteger

/**
 * Create by LiZhanPing on 2020/8/25
 */

inline fun <T> pagedList(page: Int, crossinline dataList: () -> List<T>?): PagedList<T> =
    PagedList.create(dataList(), page)

fun <T> pagedList(page: Int, dataList: List<T>?): PagedList<T> = PagedList.create(dataList, page)

inline fun <T> pagedList(pager: Pager, crossinline dataList: () -> List<T>?): PagedList<T> =
    dataList().let {
        PagedList.create(it, it.isNullOrEmpty().opt({
            pager.get()
        }, {
            pager.getAndPlus()
        }))
    }

fun <T> pagedList(pager: Pager, dataList: List<T>?): PagedList<T> = dataList.let {
    PagedList.create(it, it.isNullOrEmpty().opt({
        pager.get()
    }, {
        pager.getAndPlus()
    }))
}

fun PagedList<*>.isEmpty() = this.list.isNullOrEmpty()

fun PagedList<*>?.isNullOrEmpty() = this?.list?.isNullOrEmpty()

fun PagedList<*>.isNotEmpty() = !this.list.isNullOrEmpty()

open class PagedList<T> internal constructor(
    val page: Int,
    val list: List<T>?
) : Serializable {

    companion object {

        fun <T> create(list: List<T>?, page: Int): PagedList<T> = PagedList(page, list)

    }

}

class Pager {
    private val count = AtomicInteger(1)

    fun plusAndGet(): Int {
        return count.incrementAndGet()
    }

    fun minusAndGet(): Int {
        return count.decrementAndGet()
    }

    fun getAndPlus(): Int {
        return count.getAndIncrement()
    }

    fun getAndMinus(): Int {
        return count.getAndDecrement()
    }

    fun set(page: Int) {
        count.set(page)
    }

    fun get(): Int {
        return count.get()
    }
}

enum class LoadMode {
    LOAD, REFRESH, LOADMORE
}

class Loader private constructor(val mode: LoadMode, val scope: CoroutineScope, val delayed: Long) {
    companion object {
        fun with(mode: LoadMode, scope: CoroutineScope = MainScope(), delayed: Long = 50) =
            Loader(mode, scope, delayed)
    }
}

fun OkFaker.requestPlugin(url: String, vararg params: Pair<String, Any?>) {

    url(url)

    formParameters(params.toMap())

}

fun OkFaker.requestPlugin(url: String, params: Map<String, Any?>) {

    url(url)

    formParameters(params)

}

fun OkFaker.responsePlugin(
    precondition: (Response) -> Boolean = { it.isSuccessful },
    transform: ((String) -> Any)? = null
) {
    mapRawResponse {
        if (precondition(it)) transform?.invoke(it.body()!!.string())
        else error("Invalid response")
    }
}

fun <T> OkFaker.loadPlugin(
    loader: Loader,
    onEvent: (Event) -> Unit,
    onApply: (List<T>) -> Unit,
    onError: ((Throwable) -> Unit)? = null
) {
    onStart {
        if (loader.mode == LoadMode.LOAD) onEvent(loadingShow())
    }

    onSuccess<List<T>> {
        step(loader.scope) {
            add {
                onEvent(
                    when (loader.mode) {
                        LoadMode.LOAD -> it.isEmpty().opt(emptyShow(), contentShow())
                        LoadMode.REFRESH -> refreshCompleted()
                        LoadMode.LOADMORE -> loadMoreCompleted(it.isNotEmpty())
                    }
                )
            }

            add(loader.delayed) {
                onApply(it)
            }
        }
    }

    onError {
        step(loader.scope) {
            add {
                onEvent(
                    when (loader.mode) {
                        LoadMode.LOAD -> errorShow(it.message)
                        LoadMode.REFRESH -> refreshFailed()
                        LoadMode.LOADMORE -> loadMoreFailed()
                    }
                )
            }

            add(loader.delayed) {
                onError?.invoke(it)
            }
        }
    }
}

fun <T> OkFaker.loadPlugin(
    message: String? = null,
    onEvent: (Event) -> Unit,
    onApply: (T) -> Unit,
    onError: ((Throwable) -> Unit)? = null
) {
    onStart {
        onEvent(progressShow(message))
    }

    onSuccess<T> {
        onApply(it)
    }

    onError {
        onError?.invoke(it)
    }

    onComplete {
        onEvent(progressDismiss())
    }
}

fun OkFaker.loadPlugin(
    message: String? = null,
    onEvent: (Event) -> Unit,
    onApply: () -> Unit,
    onError: ((Throwable) -> Unit)? = null
) {
    onStart {
        onEvent(progressShow(message))
    }

    onSimpleSuccess {
        onApply()
    }

    onError {
        onError?.invoke(it)
    }

    onComplete {
        onEvent(progressDismiss())
    }
}