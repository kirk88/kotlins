package com.easy.kotlins.adapter

import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.easy.kotlins.adapter.anim.BaseItemViewAnimation
import com.easy.kotlins.adapter.anim.ItemViewAnimation
import java.util.*

open class CommonRecyclerAdapter<ITEM>(
    val context: Context,
    vararg itemDelegates: Pair<Int, ItemViewDelegate<out ITEM>>,
    private val itemAnimation: ItemViewAnimation? = null,
    private var itemClickable: Boolean = false,
    private var itemLongClickable: Boolean = false
) :
    RecyclerView.Adapter<ItemViewHolder>() {

    private val headerViews: SparseArray<View> by lazy { SparseArray() }
    private val footerViews: SparseArray<View> by lazy { SparseArray() }

    private val itemDelegates: HashMap<Int, ItemViewDelegate<out ITEM>> = hashMapOf(*itemDelegates)

    private var itemClickListener: ((CommonRecyclerAdapter<ITEM>, ItemViewHolder) -> Unit)? = null
    private var itemLongClickListener: ((CommonRecyclerAdapter<ITEM>, ItemViewHolder) -> Boolean)? =
        null
    private var itemChildClickListener: ((CommonRecyclerAdapter<ITEM>, ItemViewHolder, View) -> Unit)? =
        null
    private var itemChildLongClickListener: ((CommonRecyclerAdapter<ITEM>, ItemViewHolder, View) -> Boolean)? =
        null

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

    fun callOnItemClick(holder: ItemViewHolder) {
        if (onItemClick(holder)) return
        itemClickListener?.invoke(this, holder)
    }

    fun callOnItemLongClick(holder: ItemViewHolder) {
        if (onItemLongClick(holder)) return
        itemLongClickListener?.invoke(this, holder)
    }

    fun callOnItemChildClick(holder: ItemViewHolder, view: View) {
        if (onItemChildClick(holder, view)) return
        itemChildClickListener?.invoke(this, holder, view)
    }

    fun callOnItemChildLongClick(holder: ItemViewHolder, view: View) {
        if (onItemChildLongClick(holder, view)) return
        itemChildLongClickListener?.invoke(this, holder, view)
    }

    fun setItemClickable(itemClickable: Boolean) {
        this.itemClickable = itemClickable
        notifyDataSetChanged()
    }

    fun setItemLongClickable(itemLongClickable: Boolean) {
        this.itemLongClickable = itemLongClickable
        notifyDataSetChanged()
    }

    fun <DELEGATE : ItemViewDelegate<out ITEM>> addItemViewDelegate(
        viewType: Int,
        delegate: DELEGATE
    ) {
        itemDelegates[viewType] = delegate
        notifyDataSetChanged()
    }

    fun <DELEGATE : ItemViewDelegate<out ITEM>> addItemViewDelegates(vararg delegates: Pair<Int, DELEGATE>) {
        delegates.forEach {
            addItemViewDelegate(it.first, it.second)
        }
        notifyDataSetChanged()
    }

    fun addHeaderView(header: View) {
        (header.parent as? ViewGroup)?.removeView(header)

        synchronized(lock) {
            val position = headerViews.size()
            headerViews.put(TYPE_HEADER_VIEW + headerViews.size(), header)
            notifyItemInserted(position)
        }
    }

    fun addFooterView(footer: View) {
        (footer.parent as? ViewGroup)?.removeView(footer)

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
        if (itemAnimation is BaseItemViewAnimation) {
            itemAnimation.reset()
        }

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

    fun removeItem(predicate: (ITEM) -> Boolean) {
        synchronized(lock) {
            val iterator = this.modifiableItems.listIterator()
            while (iterator.hasNext()) {
                val position = iterator.nextIndex()
                val item = iterator.next()
                if (predicate(item)) {
                    iterator.remove()
                    notifyItemRemoved(position + headerCount)
                    break
                }
            }
        }
    }

    fun removeItems(predicate: (ITEM) -> Boolean) {
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

    fun updateItems(payload: Any? = null, predicate: (ITEM) -> Boolean) {
        synchronized(lock) {
            for (position in 0 until actualItemCount) {
                if (predicate(modifiableItems[position])) {
                    notifyItemChanged(position + headerCount, payload)
                }
            }
        }
    }

    fun updateItem(item: ITEM, payload: Any? = null) {
        synchronized(lock) {
            val position = this.modifiableItems.indexOf(item)
            if (position >= 0) {
                this.modifiableItems[position] = item
                notifyItemChanged(position + headerCount, payload)
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

    fun updateItem(payload: Any? = null, predicate: (ITEM) -> Boolean) {
        synchronized(lock) {
            for (position in 0 until actualItemCount) {
                if (predicate(modifiableItems[position])) {
                    notifyItemChanged(position + headerCount, payload)
                    break
                }
            }
        }
    }

    fun replaceItems(position: Int, item: ITEM, payload: Any? = null) {
        synchronized(lock) {
            val oldSize = actualItemCount
            if (position in 0 until oldSize) {
                this.modifiableItems[position] = item
                notifyItemChanged(position + headerCount, payload)
            }
        }
    }

    fun replaceItem(item: ITEM, payload: Any? = null, predicate: (ITEM) -> Boolean) {
        synchronized(lock) {
            for (position in 0 until actualItemCount) {
                if (predicate(modifiableItems[position])) {
                    modifiableItems[position] = item
                    notifyItemChanged(position + headerCount, payload)
                    break
                }
            }
        }
    }

    fun refreshItems(payload: Any? = null) {
        notifyItemRangeChanged(headerCount, actualItemCount, payload)
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

    fun getActualPosition(position: Int): Int = position - headerCount

    fun getItem(position: Int): ITEM = modifiableItems[position - headerCount]

    fun getItemOrNull(position: Int): ITEM? = modifiableItems.getOrNull(position - headerCount)

    fun findItem(predicate: (ITEM) -> Boolean): ITEM? {
        synchronized(lock) {
            return modifiableItems.find(predicate)
        }
    }

    protected open fun getItemViewType(item: ITEM, position: Int): Int {
        if (itemDelegates.size == 1) return 0
        return if (item is AdapterItem) item.type else 0
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
            isFooter(position) -> TYPE_FOOTER_VIEW + position
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

            viewType > TYPE_FOOTER_VIEW -> {
                ItemViewHolder(footerViews.get(viewType))
            }

            else -> {
                val holder = ItemViewHolder(
                    layoutInflater.inflate(
                        itemDelegates.getValue(viewType).layoutResId,
                        parent,
                        false
                    )
                )

                itemDelegates.getValue(viewType)
                    .registerListener(holder)

                registerListener(holder)

                holder.setOnChildClickListener {
                    if (!onItemChildClick(holder, it))
                        itemChildClickListener?.invoke(this, holder, it)
                }

                holder.setOnChildLongClickListener {
                    if (!onItemChildLongClick(holder, it))
                        itemChildLongClickListener?.invoke(this, holder, it) ?: false
                    else true
                }

                if (itemClickListener != null || itemClickable) {
                    holder.setOnClickListener {
                        if (!onItemClick(holder)) {
                            itemClickListener?.invoke(this, holder)
                        }
                    }
                }

                if (itemLongClickListener != null || itemLongClickable) {
                    holder.setOnLongClickListener {
                        if (!onItemLongClick(holder))
                            itemLongClickListener?.invoke(this, holder) ?: false
                        else true
                    }
                }

                holder
            }
        }
    }

    open fun registerListener(holder: ItemViewHolder) {

    }

    final override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

    }

    @Suppress("UNCHECKED_CAST")
    final override fun onBindViewHolder(
        holder: ItemViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (!isHeader(holder.layoutPosition) && !isFooter(holder.layoutPosition)) {
            getItemOrNull(holder.layoutPosition)?.let {
                val delegate = itemDelegates.getValue(getItemViewType(holder.layoutPosition))
                (delegate as ItemViewDelegate<ITEM>).convert(holder, it, payloads)
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

    @CallSuper
    override fun onViewAttachedToWindow(holder: ItemViewHolder) {
        itemAnimation?.start(holder)
    }

    private fun isHeader(position: Int): Boolean = position < headerCount

    private fun isFooter(position: Int): Boolean = position >= actualItemCount + headerCount

    companion object {
        private const val TYPE_HEADER_VIEW = Int.MIN_VALUE
        private const val TYPE_FOOTER_VIEW = Int.MAX_VALUE / 2
    }

}

operator fun <T> CommonRecyclerAdapter<T>.get(position: Int): T = getItem(position)

operator fun <T, A : CommonRecyclerAdapter<T>> A.plus(item: T): A = this.apply { addItem(item) }

operator fun <T, A : CommonRecyclerAdapter<T>> A.plus(items: List<T>): A =
    this.apply { addItems(items) }

operator fun <T, A : CommonRecyclerAdapter<T>> A.minus(item: T): A = this.apply { removeItem(item) }

operator fun <T, A : CommonRecyclerAdapter<T>> A.minus(items: List<T>): A =
    this.apply { removeItems(items) }

operator fun <T> CommonRecyclerAdapter<T>.plusAssign(items: List<T>) = setItems(items)
