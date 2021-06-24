@file:Suppress("unused")

package com.nice.kotlins.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.nice.kotlins.R
import com.nice.kotlins.helper.layoutInflater
import com.nice.kotlins.helper.textResource
import com.nice.kotlins.widget.LoadState.*
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException

class LoadableRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr), LoadableView {

    internal var refreshState = STATE_IDLE
    internal var loadMoreState = STATE_IDLE

    private var isRefreshEnabled = true
    private var isLoadMoreEnabled = true

    private var refreshListener: OnRefreshListener? = null
    private var loadMoreListener: OnLoadMoreListener? = null

    private val refreshView = SwipeRefreshLayout(context)
    private val recyclerView = LoadMoreRecyclerView(context) {
        if (!refreshView.isRefreshing) {
            post(loadMoreRunnable)
        }
    }

    private val refreshRunnable = Runnable {
        refreshState = STATE_RUNNING
        refreshListener?.onRefresh()
    }

    private val loadMoreRunnable = Runnable {
        loadMoreState = STATE_RUNNING
        loadMoreListener?.onLoadMore()
    }

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.LoadableRecyclerView)
        val padding =
            ta.getDimensionPixelSize(R.styleable.LoadableRecyclerView_android_padding, 0)
        val paddingTop =
            ta.getDimensionPixelSize(R.styleable.LoadableRecyclerView_android_paddingTop, padding)
        val paddingBottom =
            ta.getDimensionPixelSize(
                R.styleable.LoadableRecyclerView_android_paddingBottom,
                padding
            )
        val paddingLeft =
            ta.getDimensionPixelSize(R.styleable.LoadableRecyclerView_android_paddingLeft, padding)
        val paddingRight =
            ta.getDimensionPixelSize(R.styleable.LoadableRecyclerView_android_paddingRight, padding)
        val paddingStart = ta.getDimensionPixelSize(
            R.styleable.LoadableRecyclerView_android_paddingStart,
            paddingLeft
        )
        val paddingEnd = ta.getDimensionPixelSize(
            R.styleable.LoadableRecyclerView_android_paddingEnd,
            paddingRight
        )
        recyclerView.setPaddingRelative(paddingStart, paddingTop, paddingEnd, paddingBottom)

        if (ta.hasValue(R.styleable.LoadableRecyclerView_android_overScrollMode)) {
            val overScrollMode =
                ta.getInt(R.styleable.LoadableRecyclerView_android_overScrollMode, 0)
            recyclerView.overScrollMode = overScrollMode
        }

        setRefreshEnabled(ta.getBoolean(R.styleable.LoadableRecyclerView_refreshEnabled, true))
        setLoadMoreEnabled(ta.getBoolean(R.styleable.LoadableRecyclerView_loadMoreEnabled, true))

        val layoutManagerName = ta.getString(R.styleable.LoadableRecyclerView_layoutManager)
        ta.recycle()

        createLayoutManager(context, layoutManagerName, attrs, defStyleAttr)

        refreshView.addView(recyclerView, -1, -1)
        addView(refreshView, -1, -1)

        refreshView.setOnRefreshListener {
            recyclerView.isEnabled = false
            post(refreshRunnable)
        }
    }

    fun setAdapter(adapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>?) {
        recyclerView.adapter = adapter
    }

    fun getAdapter(): RecyclerView.Adapter<out RecyclerView.ViewHolder>? = recyclerView.adapter

    fun setLayoutManager(layoutManager: RecyclerView.LayoutManager?) {
        recyclerView.layoutManager = layoutManager
    }

    fun getLayoutManager(): RecyclerView.LayoutManager? = recyclerView.layoutManager

    fun addItemDecoration(decor: RecyclerView.ItemDecoration) =
        recyclerView.addItemDecoration(decor)

    fun removeItemDecoration(decor: RecyclerView.ItemDecoration) =
        recyclerView.removeItemDecoration(decor)

    fun invalidateItemDecorations() = recyclerView.invalidateItemDecorations()

    fun setItemAnimator(animator: RecyclerView.ItemAnimator?) {
        recyclerView.itemAnimator = animator
    }

    fun getItemAnimator(): RecyclerView.ItemAnimator? = recyclerView.itemAnimator

    fun setItemViewCacheSize(size: Int) = recyclerView.setItemViewCacheSize(size)

    fun scrollToPosition(position: Int) = recyclerView.scrollToPosition(position)

    fun smoothScrollToPosition(position: Int) = recyclerView.smoothScrollToPosition(position)

    fun setHasFixedSize(hasFixedSize: Boolean) = recyclerView.setHasFixedSize(hasFixedSize)

    fun addOnScrollListener(listener: RecyclerView.OnScrollListener) =
        recyclerView.addOnScrollListener(listener)

    fun removeOnScrollListener(listener: RecyclerView.OnScrollListener) =
        recyclerView.removeOnScrollListener(listener)

    fun setColorSchemeColors(@ColorInt vararg colors: Int) =
        refreshView.setColorSchemeColors(*colors)

    fun setColorSchemeResources(@ColorRes vararg colorResIds: Int) =
        refreshView.setColorSchemeResources(*colorResIds)

    fun setProgressBackgroundColorSchemeColor(@ColorInt color: Int) =
        refreshView.setProgressBackgroundColorSchemeColor(color)

    fun setProgressBackgroundColorSchemeResource(@ColorRes colorResId: Int) =
        refreshView.setProgressBackgroundColorSchemeResource(colorResId)

    override fun setRefreshState(state: LoadState) {
        if (refreshState != state) {
            refreshState = state
            refreshView.isRefreshing = state == STATE_RUNNING

            if (state != STATE_RUNNING) {
                recyclerView.isEnabled = isLoadMoreEnabled
            }
        }
    }

    fun setRefreshEnabled(enabled: Boolean) {
        if (isRefreshEnabled != enabled) {
            isRefreshEnabled = enabled
            refreshView.isEnabled = enabled
        }
    }

    fun setOnRefreshListener(listener: OnRefreshListener?) {
        refreshListener = listener
    }

    override fun setLoadMoreState(state: LoadState) {
        if (loadMoreState != state) {
            loadMoreState = state
            recyclerView.loadMoreState = state
        }
    }

    fun setLoadMoreEnabled(enabled: Boolean) {
        if (isLoadMoreEnabled != enabled) {
            isLoadMoreEnabled = enabled
            recyclerView.isEnabled = enabled
        }
    }

    fun setOnLoadMoreListener(listener: OnLoadMoreListener?) {
        loadMoreListener = listener
    }

    fun setOnRefreshLoadMoreListener(listener: OnRefreshLoadMoreListener?) {
        refreshListener = listener
        loadMoreListener = listener
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        setRefreshEnabled(enabled)
        setLoadMoreEnabled(enabled)
    }

    private fun createLayoutManager(
        context: Context,
        layoutManagerName: String?,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) {
        var className: String? = layoutManagerName
        if (className != null) {
            className = className.trim { it <= ' ' }
            if (className.isNotEmpty()) {
                className = getFullClassName(context, className)
                try {
                    val classLoader: ClassLoader = if (isInEditMode) {
                        this.javaClass.classLoader!!
                    } else {
                        context.classLoader
                    }
                    val layoutManagerClass = Class.forName(className, false, classLoader)
                        .asSubclass(RecyclerView.LayoutManager::class.java)
                    val constructor: Constructor<out RecyclerView.LayoutManager?> = try {
                        layoutManagerClass
                            .getConstructor(*LAYOUT_MANAGER_CONSTRUCTOR_SIGNATURE)
                    } catch (e: NoSuchMethodException) {
                        try {
                            layoutManagerClass.getConstructor()
                        } catch (e1: NoSuchMethodException) {
                            e1.initCause(e)
                            throw IllegalStateException(
                                attrs?.positionDescription
                                        + ": Error creating LayoutManager " + className, e1
                            )
                        }
                    }
                    constructor.isAccessible = true
                    recyclerView.layoutManager =
                        constructor.newInstance(context, attrs, defStyleAttr, 0)
                } catch (e: ClassNotFoundException) {
                    throw IllegalStateException(
                        attrs?.positionDescription
                                + ": Unable to find LayoutManager " + className, e
                    )
                } catch (e: InvocationTargetException) {
                    throw IllegalStateException(
                        (attrs?.positionDescription
                                + ": Could not instantiate the LayoutManager: " + className), e
                    )
                } catch (e: InstantiationException) {
                    throw IllegalStateException(
                        (attrs?.positionDescription
                                + ": Could not instantiate the LayoutManager: " + className), e
                    )
                } catch (e: IllegalAccessException) {
                    throw IllegalStateException(
                        (attrs?.positionDescription
                                + ": Cannot access non-public constructor " + className), e
                    )
                } catch (e: ClassCastException) {
                    throw IllegalStateException(
                        (attrs?.positionDescription
                                + ": Class is not a LayoutManager " + className), e
                    )
                }
            }
        }
    }

    private fun getFullClassName(context: Context, className: String): String {
        if (className[0] == '.') {
            return context.packageName + className
        }
        return if (className.contains(".")) {
            className
        } else RecyclerView::class.java.getPackage()?.name + '.' + className
    }

    fun interface OnLoadMoreListener {

        fun onLoadMore()

    }

    fun interface OnRefreshListener {

        fun onRefresh()

    }

    interface OnRefreshLoadMoreListener : OnRefreshListener, OnLoadMoreListener

    private class LoadMoreRecyclerView(
        context: Context,
        val onLoadMore: () -> Unit
    ) : RecyclerView(context) {

        var loadMoreState: LoadState
            get() {
                val loadMoreAdapter = adapter as? LoadMoreAdapter ?: return STATE_IDLE
                return loadMoreAdapter.loadMoreState
            }
            set(value) {
                val loadMoreAdapter = adapter as? LoadMoreAdapter ?: return
                loadMoreAdapter.loadMoreState = value
            }

        init {
            addOnScrollListener(object : RecyclerView.OnScrollListener() {

                var lastVisiblePosition: Int = 0
                var lastCompleteVisiblePosition: Int = 0

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    val adapter = recyclerView.adapter ?: return
                    if (isEnabled && loadMoreState == STATE_IDLE && newState == SCROLL_STATE_IDLE
                        && lastCompleteVisiblePosition > recyclerView.childCount - 1
                        && lastVisiblePosition == adapter.itemCount - 1
                    ) {
                        loadMoreState = STATE_RUNNING
                        onLoadMore()
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val layout = recyclerView.layoutManager as? LinearLayoutManager ?: return
                    lastVisiblePosition = layout.findLastVisibleItemPosition()
                    lastCompleteVisiblePosition = layout.findLastCompletelyVisibleItemPosition()
                }

            })
        }

        override fun setAdapter(adapter: Adapter<ViewHolder>?) {
            super.setAdapter(adapter?.let {
                LoadMoreAdapter(it) {
                    if (loadMoreState == STATE_FAILED) {
                        loadMoreState = STATE_RUNNING
                        onLoadMore()
                    }
                }
            })
        }

        override fun setEnabled(enabled: Boolean) {
            super.setEnabled(enabled)

            val loadMoreAdapter = adapter as? LoadMoreAdapter ?: return
            loadMoreAdapter.isLoadMoreEnabled = enabled
        }

    }

    private class LoadMoreAdapter(
        val innerAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
        val retryListener: () -> Unit
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var isLoadMoreEnabled: Boolean = true
            set(value) {
                if (field != value) {
                    field = value

                    if (itemCount > 0) {
                        if (value) {
                            notifyItemInserted(itemCount - 1)
                        } else {
                            notifyItemRemoved(itemCount)
                        }
                    }
                }

                if (value) {
                    loadMoreState = STATE_IDLE
                }
            }

        var loadMoreState: LoadState = STATE_IDLE
            set(value) {
                if (field != value) {
                    field = value

                    if (isLoadMoreEnabled) {
                        notifyItemChanged(itemCount - 1, 0)
                    }
                }
            }

        private val observer = object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                notifyDataSetChanged()
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                notifyItemRangeChanged(positionStart, itemCount)
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                notifyItemRangeChanged(positionStart, itemCount, payload)
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                notifyItemRangeInserted(positionStart, itemCount)
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                notifyItemRangeRemoved(positionStart, itemCount)
            }

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                notifyItemMoved(fromPosition, toPosition)
            }
        }

        init {
            innerAdapter.registerAdapterDataObserver(observer)
        }

        private fun isFooterView(position: Int) = isLoadMoreEnabled && position == itemCount - 1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == TYPE_FOOTER) {
                FooterViewHolder(
                    parent.layoutInflater.inflate(
                        R.layout.abc_load_more_view,
                        parent,
                        false
                    ),
                    retryListener
                )
            } else {
                innerAdapter.onCreateViewHolder(parent, viewType)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        }

        override fun onBindViewHolder(
            holder: RecyclerView.ViewHolder,
            position: Int,
            payloads: MutableList<Any>
        ) {
            if (holder is FooterViewHolder) {
                holder.setState(loadMoreState)
            } else {
                innerAdapter.onBindViewHolder(holder, position, payloads)
            }
        }

        override fun getItemCount(): Int {
            val innerItemCount = innerAdapter.itemCount
            return if (isLoadMoreEnabled && innerItemCount > 0) innerItemCount + 1 else innerItemCount
        }

        override fun getItemViewType(position: Int): Int {
            if (isFooterView(position)) {
                return TYPE_FOOTER
            }
            return innerAdapter.getItemViewType(position)
        }

        override fun getItemId(position: Int): Long {
            if (isFooterView(position)) {
                return super.getItemId(position)
            }
            return innerAdapter.getItemId(position)
        }

        override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
            if (isFooterView(holder.layoutPosition)) {
                super.onViewRecycled(holder)
            } else {
                innerAdapter.onViewRecycled(holder)
            }
        }

        override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
            if (isFooterView(holder.layoutPosition)) {
                val lp = holder.itemView.layoutParams as? StaggeredGridLayoutManager.LayoutParams
                    ?: return
                lp.isFullSpan = true
            } else {
                innerAdapter.onViewAttachedToWindow(holder)
            }
        }

        override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
            if (!isFooterView(holder.layoutPosition)) {
                innerAdapter.onViewDetachedFromWindow(holder)
            }
        }

        override fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
            if (isFooterView(holder.layoutPosition)) {
                return super.onFailedToRecycleView(holder)
            }
            return innerAdapter.onFailedToRecycleView(holder)
        }

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            innerAdapter.onAttachedToRecyclerView(recyclerView)
            val layout = recyclerView.layoutManager as? GridLayoutManager ?: return
            val originLookup = layout.spanSizeLookup
            layout.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (isFooterView(position)) {
                        layout.spanCount
                    } else originLookup?.getSpanSize(position) ?: 1
                }
            }
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            innerAdapter.onDetachedFromRecyclerView(recyclerView)
        }

        private class FooterViewHolder(
            itemView: View,
            private val retryListener: () -> Unit
        ) : RecyclerView.ViewHolder(itemView) {

            private var loadMoreState: LoadState? = null

            private val progress: ProgressBar = itemView.findViewById(R.id.progress)
            private val message: TextView = itemView.findViewById(R.id.message)

            private val clickListener = OnClickListener {
                retryListener()
            }

            fun setState(state: LoadState) {
                if (loadMoreState == state) {
                    return
                }

                loadMoreState = state

                itemView.post { updateViewWithState(state) }
            }

            private fun updateViewWithState(state: LoadState) = when (state) {
                STATE_RUNNING -> {
                    progress.isVisible = true
                    message.isVisible = true
                    message.textResource = R.string.load_more_running_message
                    itemView.setOnClickListener(null)
                }
                STATE_FAILED -> {
                    progress.isGone = true
                    message.isVisible = true
                    message.textResource = R.string.load_more_failed_message
                    itemView.setOnClickListener(clickListener)
                }
                STATE_COMPLETED -> {
                    progress.isGone = true
                    message.isVisible = true
                    message.textResource = R.string.load_more_completed_message
                    itemView.setOnClickListener(null)
                }
                else -> {
                    progress.isVisible = false
                    message.isVisible = false
                    itemView.setOnClickListener(null)
                }
            }

        }
    }

    companion object {

        private val LAYOUT_MANAGER_CONSTRUCTOR_SIGNATURE = arrayOf(
            Context::class.java,
            AttributeSet::class.java,
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType
        )

        private const val TYPE_FOOTER = Int.MAX_VALUE / 2

    }

}

var LoadableRecyclerView.adapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>?
    get() = getAdapter()
    set(value) {
        setAdapter(value)
    }

var LoadableRecyclerView.layoutManager: RecyclerView.LayoutManager?
    get() = getLayoutManager()
    set(value) {
        setLayoutManager(value)
    }

var LoadableRecyclerView.itemAnimator: RecyclerView.ItemAnimator?
    get() = getItemAnimator()
    set(value) {
        setItemAnimator(value)
    }