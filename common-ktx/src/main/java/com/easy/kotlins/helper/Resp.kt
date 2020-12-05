package com.easy.kotlins.helper

import com.easy.kotlins.event.*
import com.easy.kotlins.http.OkFaker
import com.google.gson.JsonElement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
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

class Loader private constructor(val mode: LoadMode, val scope: CoroutineScope = MainScope()) {
    companion object {
        fun with(mode: LoadMode, scope: CoroutineScope) = Loader(mode, scope)
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
    codeKey: String = "status",
    codeSuccess: Int = 1,
    dataKey: String = "data",
    transform: ((JsonElement) -> Any)? = null
) {
    mapResponse {
//        if (it.code(codeKey) == codeSuccess) {
//            it[dataKey].let { element -> transform?.invoke(element) ?: element }
//        } else mineError { it.message() }
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
            add(20) {
                onEvent(
                    when (loader.mode) {
                        LoadMode.LOAD -> it.isEmpty().opt(emptyShow(), contentShow())
                        LoadMode.REFRESH -> refreshCompleted()
                        LoadMode.LOADMORE -> loadMoreCompleted(it.isNotEmpty())
                    }
                )
            }

            add(40) {
                onApply(it)
            }
        }
    }

    onError {
        step(loader.scope) {
            add(20) {
                onEvent(
                    when (loader.mode) {
                        LoadMode.LOAD -> errorShow(it.message)
                        LoadMode.REFRESH -> refreshFailed()
                        LoadMode.LOADMORE -> loadMoreFailed()
                    }
                )
            }

            add(40) {
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