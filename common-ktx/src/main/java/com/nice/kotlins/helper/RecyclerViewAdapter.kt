@file:Suppress("unused")

package com.nice.kotlins.helper

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.nice.kotlins.adapter.BaseRecyclerAdapter
import com.nice.kotlins.adapter.CommonRecyclerAdapter
import com.nice.kotlins.adapter.ItemViewHolder
import com.nice.kotlins.adapter.ViewBindingHolder

fun <T, VH : ItemViewHolder> adapterBuilder(context: Context): RecyclerViewAdapter.Builder<T, VH> =
    RecyclerViewAdapter.Builder(context)

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

    class Builder<T, VH : ItemViewHolder>(private val context: Context) {

        internal val viewHolderCreators = mutableMapOf<Int, ViewHolderCreator<VH>>()
        internal val viewHolderBinders = mutableMapOf<Int, ViewHolderBinder<T, VH>>()
        internal var itemViewTypeSelector: AdapterItemViewTypeSelector? = null

        fun register(
            viewType: Int = 0,
            creator: ViewHolderCreator<out VH>
        ) = apply {
            @Suppress("UNCHECKED_CAST")
            viewHolderCreators[viewType] = creator as ViewHolderCreator<VH>
        }

        fun bind(
            viewType: Int = 0,
            binder: ViewHolderBinder<out T, out VH>
        ) = apply {
            @Suppress("UNCHECKED_CAST")
            viewHolderBinders[viewType] = binder as ViewHolderBinder<T, VH>
        }

        fun typedBy(
            selector: AdapterItemViewTypeSelector
        ) = apply {
            itemViewTypeSelector = selector
        }

        fun build(): RecyclerViewAdapter<T, VH> =
            RecyclerViewAdapter(
                context,
                viewHolderCreators,
                viewHolderBinders,
                itemViewTypeSelector
            )

        fun into(recyclerView: RecyclerView): RecyclerViewAdapter<T, VH> =
            build().also { recyclerView.adapter = it }

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

fun <T, VH : ItemViewHolder, A : BaseRecyclerAdapter<T, VH>> A.onItemClick(listener: (adapter: BaseRecyclerAdapter<T, VH>, holder: VH) -> Unit) =
    apply {
        setOnItemClickListener(listener)
    }

fun <T, VH : ItemViewHolder, A : BaseRecyclerAdapter<T, VH>> A.onItemLongClick(listener: (adapter: BaseRecyclerAdapter<T, VH>, holder: VH) -> Boolean) =
    apply {
        setOnItemLongClickListener(listener)
    }

fun <T, VH : ItemViewHolder, A : BaseRecyclerAdapter<T, VH>> A.onItemChildClick(listener: (adapter: BaseRecyclerAdapter<T, VH>, holder: VH, view: View) -> Unit) =
    apply {
        setOnItemChildClickListener(listener)
    }

fun <T, VH : ItemViewHolder, A : BaseRecyclerAdapter<T, VH>> A.onItemChildLongClick(listener: (adapter: BaseRecyclerAdapter<T, VH>, holder: VH, view: View) -> Boolean) =
    apply {
        setOnItemChildLongClickListener(listener)
    }