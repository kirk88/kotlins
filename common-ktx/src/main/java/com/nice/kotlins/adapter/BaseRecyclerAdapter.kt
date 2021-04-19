@file:Suppress("unused")

package com.nice.kotlins.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nice.kotlins.adapter.anim.ItemViewAnimation

abstract class BaseRecyclerAdapter<T, VH : ItemViewHolder>(
    val context: Context,
) : RecyclerView.Adapter<VH>() {

    private var itemClickListener: OnItemClickListener<T, VH>? = null
    private var itemLongClickListener: OnItemLongClickListener<T, VH>? = null
    private var itemChildClickListener: OnItemChildClickListener<T, VH>? = null
    private var itemChildLongClickListener: OnItemChildLongClickListener<T, VH>? = null

    private var itemAnimation: ItemViewAnimation? = null
    private var itemClickable: Boolean = false
    private var itemLongClickable: Boolean = false
    private var isAttachToRecyclerView: Boolean = false

    protected val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    open val items: List<T>
        get() = emptyList()

    fun setOnItemClickListener(listener: OnItemClickListener<T, VH>) {
        itemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener<T, VH>) {
        itemLongClickListener = listener
    }

    fun setOnItemChildClickListener(listener: OnItemChildClickListener<T, VH>) {
        itemChildClickListener = listener
    }

    fun setOnItemChildLongClickListener(listener: OnItemChildLongClickListener<T, VH>) {
        itemChildLongClickListener = listener
    }

    protected fun callOnItemClick(holder: VH) {
        if (onItemClick(holder)) return
        itemClickListener?.onItemClick(this, holder)
    }

    protected fun callOnItemLongClick(holder: VH) {
        if (setOnItemLongClickListener(holder)) return
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

    fun setItemClickable(itemClickable: Boolean) {
        if (this.itemClickable == itemClickable) {
            return
        }
        this.itemClickable = itemClickable
        if (isAttachToRecyclerView) {
            notifyDataSetChanged()
        }
    }

    fun setItemLongClickable(itemLongClickable: Boolean) {
        if (this.itemLongClickable == itemLongClickable) {
            return
        }
        this.itemLongClickable = itemLongClickable
        if (isAttachToRecyclerView) {
            notifyDataSetChanged()
        }
    }

    fun setItemAnimation(itemAnimation: ItemViewAnimation) {
        this.itemAnimation = itemAnimation
    }

    fun resetItemAnimation() {
        this.itemAnimation?.reset()
    }

    fun getItem(position: Int): T {
        return items[position]
    }

    fun isEmpty(): Boolean = items.isEmpty()

    open fun getSpanSize(item: T, position: Int): Int {
        return 1
    }

    open fun onItemClick(holder: VH): Boolean {
        return false
    }

    open fun setOnItemLongClickListener(holder: VH): Boolean {
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
        return if (item is AdapterItem) item.type else 0
    }

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val holder = onCreateItemViewHolder(layoutInflater, parent, viewType)

        if (itemClickListener != null || itemClickable) {
            holder.setOnClickListener {
                if (!onItemClick(holder)) {
                    itemClickListener?.onItemClick(this, holder)
                }
            }
        }

        if (itemLongClickListener != null || itemLongClickable) {
            holder.setOnLongClickListener {
                if (!setOnItemLongClickListener(holder))
                    itemLongClickListener?.onItemLongClick(this, holder) ?: false
                else true
            }
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

        onItemViewHolderCreated(holder)

        return holder
    }

    abstract fun onCreateItemViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): VH

    open fun onItemViewHolderCreated(holder: VH) {

    }

    abstract fun onBindItemViewHolder(holder: VH, item: T, payloads: MutableList<Any>)

    final override fun onBindViewHolder(holder: VH, position: Int) {

    }

    final override fun onBindViewHolder(
        holder: VH,
        position: Int,
        payloads: MutableList<Any>
    ) {
        val item = getItemOrNull(holder.layoutPosition) ?: return
        onBindItemViewHolder(holder, item, payloads)
    }

    @CallSuper
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        isAttachToRecyclerView = true

        val manager = recyclerView.layoutManager
        if (manager is GridLayoutManager) {
            manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return getItemOrNull(position)?.let {
                        getSpanSize(it, position)
                    } ?: manager.spanCount
                }
            }
        }
    }

    @CallSuper
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        isAttachToRecyclerView = false
    }

    @CallSuper
    override fun onViewAttachedToWindow(holder: VH) {
        itemAnimation?.start(holder)
    }

    @CallSuper
    override fun onViewDetachedFromWindow(holder: VH) {
        itemAnimation?.cancel()
    }

}

fun <T> BaseRecyclerAdapter<T, *>.getItemOrNull(position: Int): T? {
    return items.getOrNull(position)
}

fun <T> BaseRecyclerAdapter<T, *>.getItemOrDefault(position: Int, defaultValue: T): T {
    return items.getOrNull(position) ?: defaultValue
}

fun <T> BaseRecyclerAdapter<T, *>.getItemOrElse(position: Int, defaultValue: () -> T): T {
    return items.getOrNull(position) ?: defaultValue()
}

fun <T> BaseRecyclerAdapter<T, *>.containsItem(item: T): Boolean {
    return items.contains(item)
}

fun <T> BaseRecyclerAdapter<T, *>.indexOfItem(item: T): Int {
    return items.indexOf(item)
}

fun BaseRecyclerAdapter<*, *>.isNotEmpty(): Boolean = !isEmpty()

operator fun <T> BaseRecyclerAdapter<T, *>.get(position: Int): T = getItem(position)

fun <T, VH : ItemViewHolder> BaseRecyclerAdapter<T, VH>.onItemClick(listener: (adapter: BaseRecyclerAdapter<T, VH>, holder: VH) -> Unit) {
    setOnItemClickListener(listener)
}

fun <T, VH : ItemViewHolder> BaseRecyclerAdapter<T, VH>.onItemLongClick(listener: (adapter: BaseRecyclerAdapter<T, VH>, holder: VH) -> Boolean) {
    setOnItemLongClickListener(listener)
}

fun <T, VH : ItemViewHolder> BaseRecyclerAdapter<T, VH>.onItemChildClick(listener: (adapter: BaseRecyclerAdapter<T, VH>, holder: VH, view: View) -> Unit) {
    setOnItemChildClickListener(listener)
}

fun <T, VH : ItemViewHolder> BaseRecyclerAdapter<T, VH>.onItemChildLongClick(listener: (adapter: BaseRecyclerAdapter<T, VH>, holder: VH, view: View) -> Boolean) {
    setOnItemChildLongClickListener(listener)
}
