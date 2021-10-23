@file:Suppress("UNUSED")

package com.nice.common.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nice.common.adapter.anim.ItemViewAnimation

abstract class BaseRecyclerAdapter<T, VH : ItemViewHolder>(
    val context: Context
) : RecyclerView.Adapter<VH>() {

    private var itemClickListener: OnItemClickListener<T, VH>? = null
    private var itemLongClickListener: OnItemLongClickListener<T, VH>? = null
    private var itemChildClickListener: OnItemChildClickListener<T, VH>? = null
    private var itemChildLongClickListener: OnItemChildLongClickListener<T, VH>? = null

    private var itemClickable: Boolean = true
    private var itemLongClickable: Boolean = true

    private var itemViewAnimation: ItemViewAnimation? = null
    val itemAnimation: ItemViewAnimation? get() = itemViewAnimation

    private var recyclerView: RecyclerView? = null
    val parent: RecyclerView? get() = recyclerView

    val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    open val items: List<T>
        get() = emptyList()

    fun isEmpty(): Boolean = itemCount == 0

    fun getItem(position: Int): T = items[position]

    fun setOnItemClickListener(listener: OnItemClickListener<T, VH>?) {
        itemClickListener = listener
        itemClickable = true
        notifyItemRangeChanged(0, itemCount)
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener<T, VH>?) {
        itemLongClickListener = listener
        itemLongClickable = true
        notifyItemRangeChanged(0, itemCount)
    }

    fun setOnItemChildClickListener(listener: OnItemChildClickListener<T, VH>?) {
        itemChildClickListener = listener
        notifyItemRangeChanged(0, itemCount)
    }

    fun setOnItemChildLongClickListener(listener: OnItemChildLongClickListener<T, VH>?) {
        itemChildLongClickListener = listener
        notifyItemRangeChanged(0, itemCount)
    }

    fun setItemClickable(itemClickable: Boolean) {
        if (this.itemClickable != itemClickable) {
            this.itemClickable = itemClickable
            notifyItemRangeChanged(0, itemCount)
        }
    }

    fun setItemLongClickable(itemLongClickable: Boolean) {
        if (this.itemLongClickable != itemLongClickable) {
            this.itemLongClickable = itemLongClickable
            notifyItemRangeChanged(0, itemCount)
        }
    }

    protected fun callOnItemClick(holder: VH) {
        if (onItemClick(holder)) return
        itemClickListener?.onItemClick(this, holder)
    }

    protected fun callOnItemLongClick(holder: VH) {
        if (onItemLongClick(holder)) return
        itemLongClickListener?.onItemLongClick(this, holder)
    }

    protected fun callOnItemChildClick(holder: VH, view: View) {
        if (onItemChildClick(holder, view)) return
        itemChildClickListener?.onItemChildClick(this, holder, view)
    }

    protected fun callOnItemChildLongClick(holder: VH, view: View) {
        if (onItemChildLongClick(holder, view)) return
        itemChildLongClickListener?.onItemChildLongClick(this, holder, view)
    }

    fun setItemAnimation(animation: ItemViewAnimation?) {
        itemViewAnimation = animation
    }

    open fun getSpanSize(position: Int): Int {
        return 1
    }

    open fun onItemClick(holder: VH): Boolean {
        return false
    }

    open fun onItemLongClick(holder: VH): Boolean {
        return false
    }

    open fun onItemChildClick(holder: VH, view: View): Boolean {
        return false
    }

    open fun onItemChildLongClick(holder: VH, view: View): Boolean {
        return false
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItemOrNull(position)
        return if (item is AdapterItem) item.itemViewType else 0
    }

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val holder = onCreateItemViewHolder(layoutInflater, parent, viewType)

        if (itemClickable) {
            holder.setOnClickListener {
                if (!onItemClick(holder)) {
                    itemClickListener?.onItemClick(this, holder)
                }
            }
        } else {
            holder.removeOnClickListener()
        }

        if (itemLongClickable) {
            holder.setOnLongClickListener {
                if (!onItemLongClick(holder))
                    itemLongClickListener?.onItemLongClick(this, holder) ?: false
                else true
            }
        } else {
            holder.removeOnLongClickListener()
        }

        holder.setOnChildClickListener {
            if (!onItemChildClick(holder, it))
                itemChildClickListener?.onItemChildClick(this, holder, it)
        }

        holder.setOnChildLongClickListener {
            if (!onItemChildLongClick(holder, it))
                itemChildLongClickListener?.onItemChildLongClick(this, holder, it) ?: false
            else true
        }

        onItemViewHolderCreated(holder, viewType)

        return holder
    }

    abstract fun onCreateItemViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): VH

    open fun onItemViewHolderCreated(holder: VH, viewType: Int) {

    }

    abstract fun onBindItemViewHolder(holder: VH, item: T, payloads: List<Any>)

    final override fun onBindViewHolder(holder: VH, position: Int) {

    }

    final override fun onBindViewHolder(
        holder: VH,
        position: Int,
        payloads: List<Any>
    ) {
        val item = getItemOrNull(holder.layoutPosition) ?: return
        onBindItemViewHolder(holder, item, payloads)
    }

    @CallSuper
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView

        val manager = recyclerView.layoutManager
        if (manager is GridLayoutManager) {
            manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return this@BaseRecyclerAdapter.getSpanSize(position)
                }
            }
        }
    }

    @CallSuper
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = null
    }

    override fun onViewAttachedToWindow(holder: VH) {
        itemViewAnimation?.start(holder)
    }

}

operator fun <T> BaseRecyclerAdapter<T, *>.iterator(): Iterator<T> = items.iterator()

operator fun <T> BaseRecyclerAdapter<T, *>.get(position: Int): T = getItem(position)

fun <T> BaseRecyclerAdapter<T, *>.getItemOrNull(position: Int): T? = items.getOrNull(position)

fun <T> BaseRecyclerAdapter<T, *>.getItemOrDefault(position: Int, defaultValue: T): T =
    items.getOrNull(position) ?: defaultValue

fun <T> BaseRecyclerAdapter<T, *>.getItemOrElse(position: Int, defaultValue: (Int) -> T) =
    items.getOrNull(position) ?: defaultValue(position)

fun BaseRecyclerAdapter<*, *>.isNotEmpty(): Boolean = !isEmpty()