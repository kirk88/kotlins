@file:Suppress("unused")

package com.nice.kotlins.helper

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nice.kotlins.adapter.*
import com.nice.kotlins.adapter.anim.ItemViewAnimation

fun <T, VH : ItemViewHolder> adapterBuilder(
        context: Context,
        items: List<T>? = null
): RecyclerViewAdapter.Builder<T, VH> = RecyclerViewAdapter.Builder(context, items)

class RecyclerViewAdapter<T, VH : ItemViewHolder> private constructor(
        context: Context,
        private val viewHolderCreators: Map<Int, ViewHolderCreator<VH>>,
        private val viewHolderBinders: Map<Int, ViewHolderBinder<T, VH>>,
        private val itemViewTypeSelector: AdapterItemViewTypeSelector?
) : CommonRecyclerAdapter<T, VH>(context) {

    override fun getItemViewType(position: Int): Int {
        return itemViewTypeSelector?.select(position) ?: super.getItemViewType(position)
    }

    override fun onCreateItemViewHolder(
            inflater: LayoutInflater,
            parent: ViewGroup,
            viewType: Int
    ): VH {
        return viewHolderCreators.getValue(viewType).create(inflater, parent)
    }

    override fun onBindItemViewHolder(holder: VH, item: T, payloads: MutableList<Any>) {
        viewHolderBinders.getValue(holder.itemViewType).bind(holder, item, payloads)
    }

    class Builder<T, VH : ItemViewHolder>(
            private val context: Context,
            private val items: List<T>? = null
    ) {

        internal val viewHolderCreators = mutableMapOf<Int, ViewHolderCreator<VH>>()
        internal val viewHolderBinders = mutableMapOf<Int, ViewHolderBinder<T, VH>>()
        internal var itemViewTypeSelector: AdapterItemViewTypeSelector? = null

        private var itemClickListener: OnItemClickListener<T, VH>? = null
        private var itemLongClickListener: OnItemLongClickListener<T, VH>? = null
        private var itemChildClickListener: OnItemChildClickListener<T, VH>? = null
        private var itemChildLongClickListener: OnItemChildLongClickListener<T, VH>? = null

        private var itemAnimation: ItemViewAnimation? = null

        fun register(
                viewType: Int,
                creator: ViewHolderCreator<out VH>
        ) = apply {
            @Suppress("UNCHECKED_CAST")
            viewHolderCreators[viewType] = creator as ViewHolderCreator<VH>
        }

        fun register(creator: ViewHolderCreator<out VH>) = register(0, creator)

        fun bind(
                viewType: Int,
                binder: ViewHolderBinder<out T, out VH>
        ) = apply {
            @Suppress("UNCHECKED_CAST")
            viewHolderBinders[viewType] = binder as ViewHolderBinder<T, VH>
        }

        fun bind(binder: ViewHolderBinder<out T, out VH>) = bind(0, binder)

        fun typedBy(selector: AdapterItemViewTypeSelector) = apply {
            itemViewTypeSelector = selector
        }

        fun onItemClick(listener: OnItemClickListener<T, VH>) = apply {
            itemClickListener = listener
        }

        fun onItemLongClick(listener: OnItemLongClickListener<T, VH>) = apply {
            itemLongClickListener = listener
        }

        fun onItemChildClick(listener: OnItemChildClickListener<T, VH>) = apply {
            itemChildClickListener = listener
        }

        fun onItemChildLongClick(listener: OnItemChildLongClickListener<T, VH>) = apply {
            itemChildLongClickListener = listener
        }

        fun itemAnimation(animation: ItemViewAnimation) = apply {
            itemAnimation = animation
        }

        fun build(): RecyclerViewAdapter<T, VH> = RecyclerViewAdapter(
                        context,
                        viewHolderCreators,
                        viewHolderBinders,
                        itemViewTypeSelector
                ).also {
                    it.setItemAnimation(itemAnimation)
                    it.setOnItemClickListener(itemClickListener)
                    it.setOnItemLongClickListener(itemLongClickListener)
                    it.setOnItemChildClickListener(itemChildClickListener)
                    it.setOnItemChildLongClickListener(itemChildLongClickListener)
                    if (items != null) {
                        it.setItems(items)
                    }
                }

        fun into(recyclerView: RecyclerView): RecyclerViewAdapter<T, VH> = build().also { recyclerView.adapter = it }

    }

}

fun interface AdapterItemViewTypeSelector {

    fun select(position: Int): Int

}

fun interface ViewHolderCreator<VH : ItemViewHolder> {

    fun create(inflater: LayoutInflater, parent: ViewGroup): VH

}


fun interface ViewHolderBinder<T, VH : ItemViewHolder> {

    fun bind(holder: VH, item: T, payloads: MutableList<Any>)

}
