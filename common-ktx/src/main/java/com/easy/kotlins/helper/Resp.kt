@file:Suppress("unused")

package com.easy.kotlins.helper

import androidx.lifecycle.MutableLiveData
import com.easy.kotlins.adapter.CommonRecyclerAdapter
import com.easy.kotlins.event.*
import com.easy.kotlins.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.Response
import java.io.Serializable
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext

/**
 * Create by LiZhanPing on 2020/8/25
 */

inline fun <T> pagedList(page: Int, crossinline dataList: () -> List<T>?): PagedList<T> =
    PagedList.create(page, dataList())

fun <T> pagedList(page: Int, dataList: List<T>?): PagedList<T> = PagedList.create(page, dataList)

inline fun <T> pagedList(pager: Pager, crossinline dataList: () -> List<T>?): PagedList<T> =
    dataList().let {
        PagedList.create(it.isNullOrEmpty().opt({
            pager.get()
        }, {
            pager.getAndPlus()
        }), it)
    }

fun <T> pagedList(pager: Pager, dataList: List<T>?): PagedList<T> = dataList.let {
    PagedList.create(it.isNullOrEmpty().opt({
        pager.get()
    }, {
        pager.getAndPlus()
    }), it)
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

        fun <T> create(page: Int, list: List<T>?): PagedList<T> = PagedList(page, list)

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

class LoadConfig(
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
        get() = _pager.get()
    val pageSize: Int
        get() = _pageSize

    fun with(mode: LoadMode): LoadConfig {
        this._mode = mode
        if (mode != LoadMode.LOADMORE) {
            this._pager.reset()
        }
        return this
    }

    fun on(context: CoroutineContext): LoadConfig {
        _context = context
        return this
    }

    fun delayed(delayed: Long): LoadConfig {
        _delayed = delayed
        return this
    }

    fun page(page: Int): LoadConfig {
        _pager.set(page)
        return this
    }

    fun pageSize(pageSize: Int): LoadConfig {
        _pageSize = pageSize
        return this
    }
}

operator fun LoadConfig.plus(mode: LoadMode): LoadConfig = with(mode)
operator fun LoadConfig.plus(context: CoroutineContext): LoadConfig = on(context)

fun OkFaker.Builder<*>.request(url: String, vararg params: Pair<String, Any?>) {

    url(url)

    if (params.any { it.second is BodyFormDataPart || it.second is FileFormDataPart }) {
        formDataParts(*params)
    } else {
        formParameters(*params)
    }

}

fun OkFaker.Builder<*>.request(url: String, params: Map<String, Any?>) {

    url(url)

    if (params.any { it.value is BodyFormDataPart || it.value is FileFormDataPart }) {
        formDataParts(params)
    } else {
        formParameters(params)
    }

}

fun OkFaker.Builder<*>.request(url: String, operation: RequestPairs<String, Any?>.() -> Unit) {

    url(url)

    requestPairsOf(operation).let { pairs ->
        if (pairs.any { it.value is BodyFormDataPart || it.value is FileFormDataPart }) {
            formDataParts(pairs.toMap())
        } else {
            formParameters(pairs.toMap())
        }
    }

}

fun OkFaker.Builder<*>.request(
    config: LoadConfig,
    url: String,
    vararg params: Pair<String, Any?>
) {

    url(url)

    formParameters(mutableMapOf(*params).apply {
        put("page", config.page)
        put("pageSize", config.pageSize)
    })

}

fun OkFaker.Builder<*>.request(
    config: LoadConfig,
    url: String,
    params: Map<String, Any?>
) {

    url(url)

    formParameters(params.toMutableMap().apply {
        put("page", config.page)
        put("pageSize", config.pageSize)
    })

}

fun OkFaker.Builder<*>.request(
    config: LoadConfig,
    url: String,
    operation: RequestPairs<String, Any?>.() -> Unit
) {

    url(url)

    formParameters(requestPairsOf(operation).apply {
        put("page", config.page)
        put("pageSize", config.pageSize)
    }.toMap())

}

fun <T> OkFaker.Builder<T>.response(
    precondition: (Response) -> Boolean = { it.isSuccessful },
    errorMapper: OkMapper<Exception, T>? = null,
    resultMapper: OkMapper<String, T>
) {
    mapResponse {
        if (precondition(it)) resultMapper.map(it.body!!.string())
        else error("Invalid response")
    }

    if (errorMapper != null) {
        mapError(errorMapper)
    }
}

fun <T> OkFaker.Builder<T>.load(
    config: LoadConfig,
    onEvent: ((Event) -> Unit)? = null,
    onError: ((Exception) -> Unit)? = null,
    onApply: (T) -> Unit
) {

    val scope = CoroutineScope(config.context)

    onStart {
        if (config.mode == LoadMode.START) onEvent?.invoke(loadingShow())
    }

    onSuccess {
        step {
            add(config.delayed) {
                onEvent?.invoke(
                    if (it is Collection<*>) {
                        when (config.mode) {
                            LoadMode.START -> it.isNotEmpty().opt(contentShow(), emptyShow())
                            LoadMode.REFRESH -> it.isNotEmpty()
                                .opt(refreshSuccess(), emptyShow())
                            LoadMode.LOADMORE -> loadMoreSuccess(it.isNotEmpty())
                        }
                    } else {
                        contentShow()
                    }
                )
            }

            add {
                onApply(it)
            }
        }.launchIn(scope)
    }

    onError {
        step {
            add(config.delayed) {
                onEvent?.invoke(
                    when (config.mode) {
                        LoadMode.START -> errorShow(it.message)
                        LoadMode.REFRESH -> refreshFailure()
                        LoadMode.LOADMORE -> refreshFailure()
                    }
                )
            }

            add {
                onError?.invoke(it)
            }
        }.launchIn(scope)
    }
}

inline fun <reified T> OkFaker.Builder<T>.load(
    config: LoadConfig,
    source: MutableLiveData<T>,
    noinline onError: ((Throwable) -> Unit)? = null,
    noinline onEvent: ((Event) -> Unit)? = null
) {
    load(config, onEvent, onError) {
        if (T::class == PagedList::class && it is List<*>) {
            source.value = pagedList(config.pager, it) as T
        } else {
            source.value = it
        }
    }
}

fun <T> OkFaker.Builder<T>.load(
    message: String? = null,
    onEvent: ((Event) -> Unit)? = null,
    onError: ((Throwable) -> Unit)? = null,
    onApply: (T) -> Unit
) {
    onStart {
        onEvent?.invoke(progressShow(message))
    }

    onSuccess {
        onApply(it)
    }

    onError {
        onError?.invoke(it)
    }

    onComplete {
        onEvent?.invoke(progressDismiss())
    }
}