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
import androidx.annotation.IntDef
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.nice.kotlins.R
import com.nice.kotlins.helper.layoutInflater
import com.nice.kotlins.helper.textResource
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException

class RefreshRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

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
        val ta = context.obtainStyledAttributes(attrs, R.styleable.RefreshRecyclerView)
        val padding =
            ta.getDimensionPixelSize(R.styleable.RefreshRecyclerView_android_padding, 0)
        val paddingTop =
            ta.getDimensionPixelSize(R.styleable.RefreshRecyclerView_android_paddingTop, padding)
        val paddingBottom =
            ta.getDimensionPixelSize(R.styleable.RefreshRecyclerView_android_paddingBottom, padding)
        val paddingLeft =
            ta.getDimensionPixelSize(R.styleable.RefreshRecyclerView_android_paddingLeft, padding)
        val paddingRight =
            ta.getDimensionPixelSize(R.styleable.RefreshRecyclerView_android_paddingRight, padding)
        val paddingStart = ta.getDimensionPixelSize(
            R.styleable.RefreshRecyclerView_android_paddingStart,
            paddingLeft
        )
        val paddingEnd = ta.getDimensionPixelSize(
            R.styleable.RefreshRecyclerView_android_paddingEnd,
            paddingRight
        )
        recyclerView.setPaddingRelative(paddingStart, paddingTop, paddingEnd, paddingBottom)

        if (ta.hasValue(R.styleable.RefreshRecyclerView_android_overScrollMode)) {
            val overScrollMode =
                ta.getInt(R.styleable.RefreshRecyclerView_android_overScrollMode, 0)
            recyclerView.overScrollMode = overScrollMode
        }

        val layoutManagerName = ta.getString(R.styleable.RefreshRecyclerView_layoutManager)
        ta.recycle()

        createLayoutManager(context, layoutManagerName, attrs, defStyleAttr)

        refreshView.addView(recyclerView, -1, -1)
        addView(refreshView, -1, -1)

        refreshView.setOnRefreshListener {
            recyclerView.isEnabled = false
            post(refreshRunnable)
        }
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

    fun setAdapter(adapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>?) {
        recyclerView.adapter = adapter
    }

    fun getAdapter(): RecyclerView.Adapter<out RecyclerView.ViewHolder>? = recyclerView.adapter

    fun setRefreshState(@State state: Int) {
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

    fun isRefreshEnabled() = isRefreshEnabled

    fun setOnRefreshListener(listener: OnRefreshListener?) {
        refreshListener = listener
    }

    fun setLoadMoreState(@State state: Int) {
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

    fun isLoadMoreEnabled() = isLoadMoreEnabled

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

        var loadMoreState: Int
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

        var isLoadMoreEnabled = true
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

        var loadMoreState = STATE_IDLE
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
                super.onViewAttachedToWindow(holder)
            } else {
                innerAdapter.onViewAttachedToWindow(holder)
            }
        }

        override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
            if (isFooterView(holder.layoutPosition)) {
                super.onViewDetachedFromWindow(holder)
            } else {
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
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            innerAdapter.onDetachedFromRecyclerView(recyclerView)
        }

        class FooterViewHolder(
            itemView: View,
            private val retryListener: () -> Unit
        ) : RecyclerView.ViewHolder(itemView) {

            val progress: ProgressBar = itemView.findViewById(R.id.progress)
            val message: TextView = itemView.findViewById(R.id.message)

            private var loadMoreState = NO_STATE

            private val clickListener = OnClickListener {
                retryListener()
            }

            fun setState(state: Int) {
                if (loadMoreState == state) {
                    return
                }

                loadMoreState = state

                when (state) {
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
    }

    companion object {

        private val LAYOUT_MANAGER_CONSTRUCTOR_SIGNATURE = arrayOf(
            Context::class.java,
            AttributeSet::class.java,
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType
        )

        private const val TYPE_FOOTER = Int.MAX_VALUE / 2

        private const val NO_STATE = Int.MIN_VALUE / 2

        const val STATE_IDLE = -1
        const val STATE_RUNNING = 0x001
        const val STATE_FAILED = 0x002
        const val STATE_COMPLETED = 0x003

    }

    @IntDef(STATE_IDLE, STATE_RUNNING, STATE_FAILED, STATE_COMPLETED)
    @Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
    annotation class State
}

var RefreshRecyclerView.adapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>?
    get() = getAdapter()
    set(value) {
        setAdapter(value)
    }

var RefreshRecyclerView.isRefreshing: Boolean
    get() = refreshState == RefreshRecyclerView.STATE_RUNNING
    set(value) {
        setRefreshState(if (value) RefreshRecyclerView.STATE_RUNNING else RefreshRecyclerView.STATE_IDLE)
    }

var RefreshRecyclerView.isLoadingMore: Boolean
    get() = loadMoreState == RefreshRecyclerView.STATE_RUNNING
    set(value) {
        setLoadMoreState(if (value) RefreshRecyclerView.STATE_RUNNING else RefreshRecyclerView.STATE_IDLE)
    }