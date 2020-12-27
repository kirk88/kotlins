package com.easy.kotlins.helper

import androidx.lifecycle.MutableLiveData
import com.easy.kotlins.adapter.CommonRecyclerAdapter
import com.easy.kotlins.event.*
import com.easy.kotlins.http.BodyFromDataPart
import com.easy.kotlins.http.FileFormDataPart
import com.easy.kotlins.http.OkFaker
import kotlinx.coroutines.Dispatchers
import okhttp3.Response
import java.io.Serializable
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext

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

fun PagedList<*>?.isNullOrEmpty() = this?.list?.isNullOrEmpty()

fun PagedList<*>.isNotEmpty() = !this.list.isNullOrEmpty()

open class PagedList<T> internal constructor(
    val page: Int,
    val list: List<T>?
) : Serializable, Collection<T> {

    override val size: Int
        get() = list?.size ?: 0

    override fun contains(element: T): Boolean {
        return list?.contains(element) ?: false
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        return list?.containsAll(elements) ?: false
    }

    override fun isEmpty(): Boolean {
        return list?.isEmpty() ?: true
    }

    override fun iterator(): Iterator<T> {
        return (list ?: emptyList()).iterator()
    }

    fun dispatchTo(adapter: CommonRecyclerAdapter<T>) {
        if (page == 1) adapter.setItems(list)
        else list?.let { adapter.addItems(it) }
    }

    companion object {

        fun <T> create(list: List<T>?, page: Int): PagedList<T> = PagedList(page, list)

    }

}

open class Pager {
    private val page = AtomicInteger(1)

    fun plusAndGet(): Int {
        return page.incrementAndGet()
    }

    fun minusAndGet(): Int {
        return page.decrementAndGet()
    }

    fun getAndPlus(): Int {
        return page.getAndIncrement()
    }

    fun getAndMinus(): Int {
        return page.getAndDecrement()
    }

    fun get(): Int {
        return page.get()
    }

    protected open fun set(page: Int) {
        this.page.set(page)
    }

    protected open fun reset() {
        page.set(1)
    }
}

class MutablePager : Pager() {

    public override fun set(page: Int) {
        super.set(page)
    }

    public override fun reset() {
        super.reset()
    }

}

enum class LoadMode {
    START, REFRESH, LOADMORE
}

class Loader(
    context: CoroutineContext = Dispatchers.Main.immediate,
    delayed: Long = 0
) {

    private var _mode: LoadMode = LoadMode.START
    private var _context: CoroutineContext = context
    private var _delayed: Long = delayed

    private val _pager: MutablePager by lazy { MutablePager() }
    private var _pageSize: Int = 10

    val mode: LoadMode
        get() = _mode
    val context: CoroutineContext
        get() = _context
    val delayed: Long
        get() = _delayed
    val pager: Pager
        get() = _pager
    val page: Int
        get() = _pager.also { if (mode != LoadMode.LOADMORE) it.reset() }.get()
    val pageSize: Int
        get() = _pageSize

    fun with(mode: LoadMode): Loader {
        this._mode = mode
        return this
    }

    fun on(context: CoroutineContext): Loader {
        _context = context
        return this
    }

    fun delayed(delayed: Long): Loader {
        _delayed = delayed
        return this
    }

    fun page(page: Int): Loader {
        _pager.set(page)
        return this
    }

    fun pageSize(pageSize: Int): Loader {
        _pageSize = pageSize
        return this
    }
}

fun OkFaker.requestPlugin(url: String, vararg params: Pair<String, Any?>) {

    url(url)

    if (params.any { it.second is BodyFromDataPart || it.second is FileFormDataPart }) {
        formDataParts(*params)
    } else {
        formParameters(*params)
    }

}

fun OkFaker.requestPlugin(url: String, params: Map<String, Any?>) {

    url(url)

    if (params.any { it.value is BodyFromDataPart || it.value is FileFormDataPart }) {
        formDataParts(params)
    } else {
        formParameters(params)
    }

}

fun OkFaker.requestPlugin(loader: Loader, url: String, vararg params: Pair<String, Any?>) {

    url(url)

    formParameters(mutableMapOf(*params).apply {
        put("page", loader.page)
        put("pagesize", loader.pageSize)
    })

}

fun OkFaker.requestPlugin(loader: Loader, url: String, params: Map<String, Any?>) {

    url(url)

    formParameters(params.toMutableMap().apply {
        put("page", loader.page)
        put("pagesize", loader.pageSize)
    })
}

fun <T> OkFaker.responsePlugin(
    precondition: (Response) -> Boolean = { it.isSuccessful },
    errorMapper: ((Throwable) -> T)? = null,
    resultMapper: (String) -> T
) {
    mapResponse {
        if (precondition(it)) resultMapper(it.body()!!.string())
        else error("Invalid response")
    }

    if (errorMapper != null) {
        mapError(errorMapper)
    }
}

fun <T : Any> OkFaker.loadPlugin(
    loader: Loader,
    onEvent: ((Event) -> Unit)? = null,
    onError: ((Throwable) -> Unit)? = null,
    onApply: (T) -> Unit
) {
    onStart {
        if (loader.mode == LoadMode.START) onEvent?.invoke(loadingShow())
    }

    onSuccess<T> {
        step(loader.context) {
            add {
                if (it is Collection<*>) {
                    onEvent?.invoke(
                        when (loader.mode) {
                            LoadMode.START -> it.isEmpty().opt(emptyShow(), contentShow())
                            LoadMode.REFRESH -> refreshCompleted()
                            LoadMode.LOADMORE -> loadMoreCompleted(it.isNotEmpty())
                        }
                    )
                } else {
                    onEvent?.invoke(contentShow())
                }
            }

            add(loader.delayed) {
                onApply(it)
            }
        }
    }

    onError {
        step(loader.context) {
            add {
                onEvent?.invoke(
                    when (loader.mode) {
                        LoadMode.START -> errorShow(it.message)
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

fun <T : Any> OkFaker.loadPlugin(
    loader: Loader,
    source: MutableLiveData<T>,
    onError: ((Throwable) -> Unit)? = null,
    onEvent: ((Event) -> Unit)? = null
) {
    loadPlugin<T>(loader, onEvent, onError) {
        source.value = it
    }
}

fun <T : Any> OkFaker.loadPlugin(
    message: String? = null,
    onEvent: ((Event) -> Unit)? = null,
    onError: ((Throwable) -> Unit)? = null,
    onApply: (T) -> Unit
) {
    onStart {
        onEvent?.invoke(progressShow(message))
    }

    onSuccess<T> {
        onApply(it)
    }

    onError {
        onError?.invoke(it)
    }

    onComplete {
        onEvent?.invoke(progressDismiss())
    }
}

fun OkFaker.loadPlugin(
    message: String? = null,
    onEvent: ((Event) -> Unit)? = null,
    onError: ((Throwable) -> Unit)? = null,
    onApply: () -> Unit
) {
    onStart {
        onEvent?.invoke(progressShow(message))
    }

    onSimpleSuccess {
        onApply()
    }

    onError {
        onError?.invoke(it)
    }

    onComplete {
        onEvent?.invoke(progressDismiss())
    }
}