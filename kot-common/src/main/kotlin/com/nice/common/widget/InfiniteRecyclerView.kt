@file:Suppress("unused")

package com.nice.common.widget

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
import com.nice.common.R
import com.nice.common.helper.inflate
import com.nice.common.helper.orZero
import com.nice.common.helper.textResource
import com.nice.common.widget.InfiniteState.*
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException

class InfiniteRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr), InfiniteView {

    internal var refreshState = STATE_IDLE
    internal var loadMoreState = STATE_IDLE

    private var isRefreshEnabled = true
    private var isLoadMoreEnabled = true

    private var refreshListener: OnRefreshListener? = null
    private var loadMoreListener: OnLoadMoreListener? = null

    private val refreshView = SwipeRefreshLayout(context)
    private val recyclerView = ScrollLoadMoreRecyclerView(context)

    private val refreshRunnable = Runnable {
        refreshState = STATE_RUNNING
        refreshListener?.onRefresh()
    }

    private val loadRunnable = Runnable {
        loadMoreState = STATE_RUNNING
        loadMoreListener?.onLoadMore()
    }

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.InfiniteRecyclerView)
        val padding = ta.getDimensionPixelSize(R.styleable.InfiniteRecyclerView_android_padding, 0)
        val paddingTop = ta.getDimensionPixelSize(R.styleable.InfiniteRecyclerView_android_paddingTop, padding)
        val paddingBottom = ta.getDimensionPixelSize(
            R.styleable.InfiniteRecyclerView_android_paddingBottom, padding
        )
        val paddingLeft = ta.getDimensionPixelSize(R.styleable.InfiniteRecyclerView_android_paddingLeft, padding)
        val paddingRight = ta.getDimensionPixelSize(R.styleable.InfiniteRecyclerView_android_paddingRight, padding)
        val paddingStart = ta.getDimensionPixelSize(
            R.styleable.InfiniteRecyclerView_android_paddingStart, paddingLeft
        )
        val paddingEnd = ta.getDimensionPixelSize(
            R.styleable.InfiniteRecyclerView_android_paddingEnd, paddingRight
        )
        recyclerView.setPaddingRelative(paddingStart, paddingTop, paddingEnd, paddingBottom)

        if (ta.hasValue(R.styleable.InfiniteRecyclerView_android_overScrollMode)) {
            val overScrollMode = ta.getInt(R.styleable.InfiniteRecyclerView_android_overScrollMode, 0)
            recyclerView.overScrollMode = overScrollMode
        }

        setRefreshEnabled(ta.getBoolean(R.styleable.InfiniteRecyclerView_refreshEnabled, true))
        setLoadMoreEnabled(ta.getBoolean(R.styleable.InfiniteRecyclerView_loadMoreEnabled, true))

        val layoutManagerName = ta.getString(R.styleable.InfiniteRecyclerView_layoutManager)
        ta.recycle()

        createLayoutManager(context, layoutManagerName, attrs, defStyleAttr)

        refreshView.addView(recyclerView, -1, -1)
        addView(refreshView, -1, -1)

        refreshView.setOnRefreshListener {
            recyclerView.isEnabled = false
            post(refreshRunnable)
        }

        recyclerView.setOnLoadMoreListener {
            refreshView.isEnabled = false
            post(loadRunnable)
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

    fun addItemDecoration(decor: RecyclerView.ItemDecoration) = recyclerView.addItemDecoration(decor)

    fun removeItemDecoration(decor: RecyclerView.ItemDecoration) = recyclerView.removeItemDecoration(decor)

    fun invalidateItemDecorations() = recyclerView.invalidateItemDecorations()

    fun setItemAnimator(animator: RecyclerView.ItemAnimator?) {
        recyclerView.itemAnimator = animator
    }

    fun getItemAnimator(): RecyclerView.ItemAnimator? = recyclerView.itemAnimator

    fun setItemViewCacheSize(size: Int) = recyclerView.setItemViewCacheSize(size)

    fun scrollToPosition(position: Int) = recyclerView.scrollToPosition(position)

    fun smoothScrollToPosition(position: Int) = recyclerView.smoothScrollToPosition(position)

    fun setHasFixedSize(hasFixedSize: Boolean) = recyclerView.setHasFixedSize(hasFixedSize)

    fun addOnScrollListener(listener: RecyclerView.OnScrollListener) = recyclerView.addOnScrollListener(listener)

    fun removeOnScrollListener(listener: RecyclerView.OnScrollListener) = recyclerView.removeOnScrollListener(listener)

    fun setColorSchemeColors(@ColorInt vararg colors: Int) = refreshView.setColorSchemeColors(*colors)

    fun setColorSchemeResources(@ColorRes vararg colorResIds: Int) = refreshView.setColorSchemeResources(*colorResIds)

    fun setProgressBackgroundColorSchemeColor(@ColorInt color: Int) = refreshView.setProgressBackgroundColorSchemeColor(color)

    fun setProgressBackgroundColorSchemeResource(@ColorRes colorResId: Int) = refreshView.setProgressBackgroundColorSchemeResource(colorResId)

    override fun setRefreshState(state: InfiniteState) {
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

    override fun setLoadMoreState(state: InfiniteState) {
        if (loadMoreState != state) {
            loadMoreState = state
            recyclerView.infiniteState = state

            if (state != STATE_RUNNING) {
                refreshView.isEnabled = isRefreshEnabled
            }
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
                            throw IllegalStateException("${attrs?.positionDescription}: Error creating LayoutManager $className", e1)
                        }
                    }
                    constructor.isAccessible = true
                    recyclerView.layoutManager = constructor.newInstance(context, attrs, defStyleAttr, 0)
                } catch (e: ClassNotFoundException) {
                    throw IllegalStateException("${attrs?.positionDescription}: Unable to find LayoutManager $className", e)
                } catch (e: InvocationTargetException) {
                    throw IllegalStateException("${attrs?.positionDescription}: Could not instantiate the LayoutManager: $className", e)
                } catch (e: InstantiationException) {
                    throw IllegalStateException("${attrs?.positionDescription}: Could not instantiate the LayoutManager: $className", e)
                } catch (e: IllegalAccessException) {
                    throw IllegalStateException("${attrs?.positionDescription}: Cannot access non-public constructor $className", e)
                } catch (e: ClassCastException) {
                    throw IllegalStateException("${attrs?.positionDescription}: Class is not a LayoutManager $className", e)
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

    private class ScrollLoadMoreRecyclerView(context: Context) : RecyclerView(context) {

        var infiniteState: InfiniteState
            get() = adapterWrapper?.infiniteState ?: STATE_IDLE
            set(value) {
                adapterWrapper?.infiniteState = value
            }

        private var adapterWrapper: LoadMoreAdapterWrapper? = null
        private var loadMoreListener: OnLoadMoreListener? = null

        init {
            addOnScrollListener(object : RecyclerView.OnScrollListener() {

                var lastVisiblePosition = 0
                var lastCompleteVisiblePosition = 0
                var isScrollChanged = false

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    val lastPosition = recyclerView.adapter?.itemCount ?: return
                    if (isEnabled && isScrollChanged
                        && lastVisiblePosition == lastPosition
                        && infiniteState == STATE_IDLE
                        && newState == SCROLL_STATE_IDLE
                    ) {
                        infiniteState = STATE_RUNNING
                        loadMoreListener?.onLoadMore()
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val layout = recyclerView.layoutManager ?: return
                    if (layout is LinearLayoutManager) {
                        isScrollChanged = if (layout.orientation == VERTICAL) dy > 0 else dx > 0
                        lastVisiblePosition = layout.findLastVisibleItemPosition()
                        lastCompleteVisiblePosition = layout.findLastCompletelyVisibleItemPosition()
                    } else if (layout is StaggeredGridLayoutManager) {
                        isScrollChanged = if (layout.orientation == VERTICAL) dy > 0 else dx > 0
                        lastVisiblePosition = layout.findLastVisibleItemPositions(null).maxOrNull().orZero()
                        lastCompleteVisiblePosition = layout.findLastCompletelyVisibleItemPositions(null).maxOrNull().orZero()
                    }
                }

            })
        }

        override fun setAdapter(adapter: Adapter<out ViewHolder>?) {
            super.setAdapter(adapter?.let {
                @Suppress("UNCHECKED_CAST")
                LoadMoreAdapterWrapper(it as Adapter<ViewHolder>) {
                    if (infiniteState == STATE_FAILED) {
                        infiniteState = STATE_RUNNING
                        loadMoreListener?.onLoadMore()
                    }
                }.also { wrapper ->
                    adapterWrapper = wrapper
                }
            })
        }

        override fun getAdapter(): Adapter<out ViewHolder>? {
            val adapter = super.getAdapter()
            return if (adapter is LoadMoreAdapterWrapper) adapter.innerAdapter else adapter
        }

        override fun setEnabled(enabled: Boolean) {
            super.setEnabled(enabled)
            adapterWrapper?.isLoadMoreEnabled = enabled
        }

        fun setOnLoadMoreListener(listener: OnLoadMoreListener?) {
            loadMoreListener = listener
        }

    }

    private class LoadMoreAdapterWrapper(
        val innerAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
        private val retryListener: () -> Unit
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var isLoadMoreEnabled: Boolean = true
            set(value) {
                if (field != value) {
                    field = value
                    if (itemCount > 1) {
                        if (value) {
                            notifyItemInserted(itemCount)
                        } else {
                            notifyItemRemoved(itemCount)
                        }
                    }
                }

                if (value) {
                    infiniteState = STATE_IDLE
                }
            }

        var infiniteState: InfiniteState = STATE_IDLE
            set(value) {
                if (field != value) {
                    field = value
                    if (isLoadMoreEnabled) {
                        notifyItemChanged(itemCount - 1, 0)
                    }
                }
            }

        private val dataObserver = object : RecyclerView.AdapterDataObserver() {
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
            innerAdapter.registerAdapterDataObserver(dataObserver)
        }

        private fun isFooterView(position: Int) = isLoadMoreEnabled && position == innerAdapter.itemCount
        private fun isFooterView(holder: RecyclerView.ViewHolder) = holder.itemViewType == TYPE_FOOTER

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == TYPE_FOOTER) {
                FooterViewHolder(
                    parent.inflate(R.layout.abc_load_more_view, false),
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
                holder.setState(infiniteState)
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
            if (isFooterView(holder)) {
                super.onViewRecycled(holder)
            } else {
                innerAdapter.onViewRecycled(holder)
            }
        }

        override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
            if (isFooterView(holder)) {
                (holder.itemView.layoutParams as? StaggeredGridLayoutManager.LayoutParams)?.isFullSpan = true
            } else {
                innerAdapter.onViewAttachedToWindow(holder)
            }
        }

        override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
            if (isFooterView(holder)) {
                innerAdapter.onViewDetachedFromWindow(holder)
            }
        }

        override fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
            if (isFooterView(holder)) {
                return super.onFailedToRecycleView(holder)
            }
            return innerAdapter.onFailedToRecycleView(holder)
        }

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            innerAdapter.onAttachedToRecyclerView(recyclerView)
            val layout = recyclerView.layoutManager as? GridLayoutManager ?: return
            val originalSpanSizeLookup = layout.spanSizeLookup
            layout.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (isFooterView(position)) {
                        layout.spanCount
                    } else originalSpanSizeLookup?.getSpanSize(position) ?: 1
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

            private var infiniteState: InfiniteState? = null

            private val progress: ProgressBar = itemView.findViewById(R.id.progress)
            private val message: TextView = itemView.findViewById(R.id.message)

            private val clickListener = OnClickListener {
                retryListener()
            }

            fun setState(state: InfiniteState) {
                if (infiniteState == state) {
                    return
                }

                infiniteState = state

                itemView.post { updateViewWithState(state) }
            }

            private fun updateViewWithState(state: InfiniteState) = when (state) {
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

var InfiniteRecyclerView.adapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>?
    get() = getAdapter()
    set(value) {
        setAdapter(value)
    }

var InfiniteRecyclerView.layoutManager: RecyclerView.LayoutManager?
    get() = getLayoutManager()
    set(value) {
        setLayoutManager(value)
    }

var InfiniteRecyclerView.itemAnimator: RecyclerView.ItemAnimator?
    get() = getItemAnimator()
    set(value) {
        setItemAnimator(value)
    }