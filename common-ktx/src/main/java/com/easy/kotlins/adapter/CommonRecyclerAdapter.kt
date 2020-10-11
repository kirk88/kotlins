package com.easy.kotlins.adapter

import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.easy.kotlins.helper.onClick
import com.easy.kotlins.helper.onLongClick
import java.util.*

open class CommonRecyclerAdapter<ITEM>(protected val context: Context) :
        RecyclerView.Adapter<ItemViewHolder>() {

    constructor(
            context: Context,
            vararg delegates: Pair<Int, ItemViewDelegate<ITEM>>
    ) : this(context) {
        addItemViewDelegates(*delegates)
    }

    private val headerViews: SparseArray<View> by lazy { SparseArray() }
    private val footerViews: SparseArray<View> by lazy { SparseArray() }

    private val itemDelegates: HashMap<Int, ItemViewDelegate<ITEM>> = hashMapOf()

    private var itemClickListener: ((CommonRecyclerAdapter<ITEM>, ItemViewHolder) -> Unit)? = null
    private var itemLongClickListener: ((CommonRecyclerAdapter<ITEM>, ItemViewHolder) -> Boolean)? = null
    private var itemChildClickListener: ((CommonRecyclerAdapter<ITEM>, ItemViewHolder, View) -> Unit)? = null
    private var itemChildLongClickListener: ((CommonRecyclerAdapter<ITEM>, ItemViewHolder, View) -> Boolean)? = null

    private val lock: Any = Any()

    protected val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    protected var targetView: RecyclerView? = null
        private set

    protected val modifiableItems: MutableList<ITEM> = mutableListOf()

    val items: List<ITEM>
        get() = Collections.unmodifiableList(modifiableItems)

    val actualItemCount: Int
        get() = modifiableItems.size

    val headerCount: Int
        get() = headerViews.size()

    val footerCount: Int
        get() = footerViews.size()

    fun onItemClick(listener: (adapter: CommonRecyclerAdapter<ITEM>, holder: ItemViewHolder) -> Unit) {
        itemClickListener = listener
    }

    fun onItemLongClick(listener: (adapter: CommonRecyclerAdapter<ITEM>, holder: ItemViewHolder) -> Boolean) {
        itemLongClickListener = listener
    }

    fun onItemChildClick(listener: (adapter: CommonRecyclerAdapter<ITEM>, holder: ItemViewHolder, view: View) -> Unit) {
        itemChildClickListener = listener
    }

    fun onItemChildLongClick(listener: (adapter: CommonRecyclerAdapter<ITEM>, holder: ItemViewHolder, view: View) -> Boolean) {
        itemChildLongClickListener = listener
    }

    fun <DELEGATE : ItemViewDelegate<ITEM>> addItemViewDelegate(viewType: Int, delegate: DELEGATE) {
        itemDelegates[viewType] = delegate
    }

    fun addItemViewDelegates(vararg delegates: Pair<Int, ItemViewDelegate<ITEM>>) {
        delegates.forEach {
            addItemViewDelegate(it.first, it.second)
        }
    }

    fun addHeaderView(header: View) {
        synchronized(lock) {
            val position = headerViews.size()
            headerViews.put(TYPE_HEADER_VIEW + headerViews.size(), header)
            notifyItemInserted(position)
        }
    }

    fun addFooterView(footer: View) {
        synchronized(lock) {
            val position = actualItemCount + footerViews.size()
            footerViews.put(TYPE_FOOTER_VIEW + footerViews.size(), footer)
            notifyItemInserted(position)
        }
    }

    fun removeHeaderView(header: View) {
        synchronized(lock) {
            val position = headerViews.indexOfValue(header)
            if (position >= 0) {
                headerViews.remove(position)
                notifyItemRemoved(position)
            }
        }
    }

    fun removeHeaderView(headerPosition: Int) {
        if (headerPosition !in 0 until headerCount) return
        synchronized(lock) {
            headerViews.removeAt(headerPosition)
            notifyItemRemoved(headerPosition)
        }
    }

    fun removeFooterView(footer: View) {
        synchronized(lock) {
            val position = footerViews.indexOfValue(footer)
            if (position >= 0) {
                footerViews.remove(position)
                notifyItemRemoved(actualItemCount + position - 2)
            }
        }
    }

    fun removeFooterView(footerPosition: Int) {
        if (footerPosition !in 0 until footerCount) return
        synchronized(lock) {
            val position = actualItemCount + footerPosition
            footerViews.removeAt(footerPosition)
            notifyItemRemoved(position)
        }
    }

    fun containsHeaderView(header: View): Boolean {
        synchronized(lock) {
            return headerViews.indexOfValue(header) >= 0
        }
    }

    fun containsFooterView(footer: View): Boolean {
        synchronized(lock) {
            return footerViews.indexOfValue(footer) >= 0
        }
    }

    fun setItems(items: List<ITEM>?) {
        synchronized(lock) {
            if (this.modifiableItems.isNotEmpty()) {
                this.modifiableItems.clear()
            }
            if (items != null) {
                this.modifiableItems.addAll(items)
            }
            notifyDataSetChanged()
        }
    }

    fun setItems(items: List<ITEM>?, diffResult: DiffUtil.DiffResult) {
        synchronized(lock) {
            if (this.modifiableItems.isNotEmpty()) {
                this.modifiableItems.clear()
            }
            if (items != null) {
                this.modifiableItems.addAll(items)
            }
            diffResult.dispatchUpdatesTo(this)
        }
    }

    fun setItem(position: Int, item: ITEM, payload: Any? = null) {
        synchronized(lock) {
            val oldSize = actualItemCount
            if (position in 0 until oldSize) {
                this.modifiableItems[position] = item
                notifyItemChanged(position + headerCount)
            }
        }
    }

    fun setItemWhile(item: ITEM, payload: Any? = null, predicate: (ITEM) -> Boolean) {
        synchronized(lock) {
            for (position in 0 until actualItemCount) {
                if (predicate(modifiableItems[position])) {
                    if (item != null) {
                        modifiableItems[position] = item
                    }
                    notifyItemChanged(position + headerCount, payload)
                    break
                }
            }
        }
    }

    fun addItem(item: ITEM) {
        synchronized(lock) {
            val oldSize = actualItemCount
            if (this.modifiableItems.add(item)) {
                notifyItemInserted(oldSize + headerCount)
            }
        }
    }

    fun addItem(position: Int, item: ITEM) {
        synchronized(lock) {
            if (position >= 0) {
                this.modifiableItems.add(position, item)
                notifyItemInserted(position + headerCount)
            }
        }
    }

    fun addItems(position: Int, newItems: List<ITEM>) {
        synchronized(lock) {
            if (this.modifiableItems.addAll(position, newItems)) {
                notifyItemRangeInserted(position + headerCount, newItems.size)
            }
        }
    }

    fun addItems(newItems: List<ITEM>) {
        synchronized(lock) {
            val oldSize = actualItemCount
            if (this.modifiableItems.addAll(newItems)) {
                if (oldSize == 0 && headerCount == 0) {
                    notifyDataSetChanged()
                } else {
                    notifyItemRangeInserted(oldSize + headerCount, newItems.size)
                }
            }
        }
    }

    fun removeItem(position: Int) {
        synchronized(lock) {
            if (this.modifiableItems.removeAt(position) != null) {
                notifyItemRemoved(position + headerCount)
            }
        }
    }

    fun removeItem(item: ITEM) {
        synchronized(lock) {
            val position = this.modifiableItems.indexOf(item)
            if (position >= 0) {
                this.modifiableItems.removeAt(position)
                notifyItemRemoved(position + headerCount)
            }
        }
    }

    fun removeItemIf(predicate: (ITEM) -> Boolean) {
        synchronized(lock) {
            val iterator = this.modifiableItems.listIterator()
            while (iterator.hasNext()) {
                val position = iterator.nextIndex()
                val item = iterator.next()
                if (predicate(item)) {
                    iterator.remove()
                    notifyItemRemoved(position + headerCount)
                }
            }
        }
    }

    fun removeItems(items: List<ITEM>) {
        synchronized(lock) {
            if (this.modifiableItems.removeAll(items)) {
                notifyDataSetChanged()
            }
        }
    }

    fun swapItem(oldPosition: Int, newPosition: Int) {
        synchronized(lock) {
            if (oldPosition in 0 until actualItemCount && newPosition in 0 until actualItemCount) {
                val srcPosition = oldPosition + headerCount
                val targetPosition = newPosition + headerCount
                Collections.swap(this.modifiableItems, srcPosition, targetPosition)
                notifyItemChanged(srcPosition)
                notifyItemChanged(targetPosition)
            }
        }
    }

    fun updateItem(item: ITEM, payload: Any? = null) {
        synchronized(lock) {
            val position = this.modifiableItems.indexOf(item)
            if (position >= 0) {
                this.modifiableItems[position] = item
                notifyItemChanged(position, payload)
            }
        }
    }

    fun updateItems(fromPosition: Int, toPosition: Int, payloads: Any? = null) {
        synchronized(lock) {
            if (fromPosition in 0 until actualItemCount && toPosition in 0 until actualItemCount) {
                notifyItemRangeChanged(
                        fromPosition + headerCount,
                        toPosition - fromPosition + 1,
                        payloads
                )
            }
        }
    }

    fun updateItem(position: Int, payload: Any? = null) {
        synchronized(lock) {
            if (position in 0 until actualItemCount) {
                notifyItemChanged(position + headerCount, payload)
            }
        }
    }

    fun updateItemWhile(payload: Any? = null, predicate: (ITEM) -> Boolean) {
        synchronized(lock) {
            for (position in 0 until actualItemCount) {
                if (predicate(modifiableItems[position])) {
                    notifyItemChanged(position + headerCount, payload)
                    break
                }
            }
        }
    }

    fun updateItemWhen(payload: Any? = null, predicate: (ITEM) -> Boolean) {
        synchronized(lock) {
            for (position in 0 until actualItemCount) {
                if (predicate(modifiableItems[position])) {
                    notifyItemChanged(position + headerCount, payload)
                }
            }
        }
    }

    fun containsItem(item: ITEM): Boolean {
        synchronized(lock) {
            return modifiableItems.contains(item)
        }
    }

    fun indexOfItem(item: ITEM): Int {
        synchronized(lock) {
            return modifiableItems.indexOf(item)
        }
    }

    fun sortItems() {
        synchronized(lock) {
            if (modifiableItems.isNotEmpty()) {
                Collections.sort(modifiableItems, null)
                notifyDataSetChanged()
            }
        }
    }

    fun sortItems(comparator: Comparator<ITEM>) {
        synchronized(lock) {
            if (modifiableItems.isNotEmpty()) {
                Collections.sort(modifiableItems, comparator)
                notifyDataSetChanged()
            }
        }
    }

    fun clearItems() {
        synchronized(lock) {
            this.modifiableItems.clear()
            notifyDataSetChanged()
        }
    }

    fun isEmpty(): Boolean = modifiableItems.isEmpty()

    fun isNotEmpty(): Boolean = modifiableItems.isNotEmpty()

    operator fun get(position: Int): ITEM = modifiableItems[position - headerCount]

    fun getItemOrNull(position: Int): ITEM? = modifiableItems.getOrNull(position - headerCount)

    fun findItem(predicate: (ITEM) -> Boolean): ITEM? {
        synchronized(lock) {
            return modifiableItems.find(predicate)
        }
    }

    protected open fun getItemViewType(item: ITEM, position: Int): Int {
        if (itemDelegates.size == 1) return 0
        return if (item is MultipleItem) item.type else 0
    }

    /**
     * grid 模式下使用
     */
    protected open fun getSpanSize(item: ITEM, position: Int): Int {
        return 1
    }

    protected open fun onItemClick(holder: ItemViewHolder): Boolean {
        return false
    }

    protected open fun onItemLongClick(holder: ItemViewHolder): Boolean {
        return false
    }

    protected open fun onItemChildClick(holder: ItemViewHolder, view: View): Boolean {
        return false
    }

    protected open fun onItemChildLongClick(holder: ItemViewHolder, view: View): Boolean {
        return false
    }

    final override fun getItemCount(): Int {
        return actualItemCount + headerCount + footerCount
    }

    final override fun getItemViewType(position: Int): Int {
        return when {
            isHeader(position) -> TYPE_HEADER_VIEW + position
            isFooter(position) -> TYPE_FOOTER_VIEW + position - actualItemCount - headerCount
            else -> getItemOrNull(position)?.let {
                getItemViewType(it, getActualPosition(position))
            } ?: 0
        }
    }

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return when {
            viewType < TYPE_HEADER_VIEW + headerCount -> {
                ItemViewHolder(headerViews.get(viewType))
            }

            viewType >= TYPE_FOOTER_VIEW -> {
                ItemViewHolder(footerViews.get(viewType))
            }

            else -> {
                val holder = ItemViewHolder(
                        layoutInflater.inflate(
                                itemDelegates.getValue(viewType).layoutId,
                                parent,
                                false
                        )
                )

                itemDelegates.getValue(viewType)
                        .registerListener(holder)

                holder.clickViews.onClick {
                    if (!onItemChildClick(holder, it))
                        itemChildClickListener?.invoke(this, holder, it)
                }

                holder.longClickViews.onLongClick {
                    if (!onItemChildLongClick(holder, it))
                        itemChildLongClickListener?.invoke(this, holder, it) ?: false
                    else true
                }

                if (itemClickListener != null) {
                    holder.itemView.setOnClickListener {
                        if (!onItemClick(holder)) {
                            itemClickListener?.invoke(this, holder)
                        }
                    }
                }

                if (itemLongClickListener != null) {
                    holder.itemView.setOnLongClickListener {
                        if (!onItemLongClick(holder))
                            itemLongClickListener?.invoke(this, holder) ?: false
                        else true
                    }
                }

                holder
            }
        }
    }

    final override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

    }

    final override fun onBindViewHolder(
        holder: ItemViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (!isHeader(holder.layoutPosition) && !isFooter(holder.layoutPosition)) {
            getItemOrNull(holder.layoutPosition)?.let {
                itemDelegates.getValue(getItemViewType(holder.layoutPosition))
                        .convert(holder, it, payloads)
            }
        }
    }

    @CallSuper
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.targetView = recyclerView

        val manager = recyclerView.layoutManager
        if (manager is GridLayoutManager) {
            manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (isHeader(position) || isFooter(position)) {
                        manager.spanCount
                    } else {
                        getItemOrNull(position)?.let {
                            getSpanSize(it, position)
                        } ?: manager.spanCount
                    }
                }
            }
        }
    }

    @CallSuper
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        this.targetView = null
    }

    private fun isHeader(position: Int): Boolean = position < headerCount

    private fun isFooter(position: Int): Boolean = position >= actualItemCount + headerCount

    private fun getActualPosition(position: Int): Int = position - headerCount

    companion object {
        private const val TYPE_HEADER_VIEW = Int.MIN_VALUE
        private const val TYPE_FOOTER_VIEW = Int.MAX_VALUE - 999
    }


}