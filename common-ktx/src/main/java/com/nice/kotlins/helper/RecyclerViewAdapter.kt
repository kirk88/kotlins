package com.nice.kotlins.helper

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nice.kotlins.adapter.BaseRecyclerAdapter
import com.nice.kotlins.adapter.CommonRecyclerAdapter
import com.nice.kotlins.adapter.ItemViewHolder

fun RecyclerView.Adapter<*>.attachTo(recyclerView: RecyclerView) {
    recyclerView.adapter = this
}

class TestViewHolder(itemView: View) : ItemViewHolder(itemView) {

    val title: String = ""

}

fun <T, VH : ItemViewHolder> RecyclerView.adapter(): RecyclerAdapterBuilder<T, VH> =
    RecyclerAdapterBuilder<T, VH>(context).also { adapter = it }

fun <T, VH : ItemViewHolder> adapterBuilder(context: Context): RecyclerAdapterBuilder<T, VH> =
    RecyclerAdapterBuilder(context)

fun <T, VH : ItemViewHolder> RecyclerAdapterBuilder<T, VH>.selectItemViewTypeBy(
    selector: AdapterItemViewTypeSelector
) = apply {
    setItemViewTypeSelector(selector)
}

fun <T, VH : ItemViewHolder> RecyclerAdapterBuilder<T, VH>.buildViewHolder(
    viewType: Int = 0,
    builder: ViewHolderBuilder<out VH>
) = apply {
    addViewHolderBuilder(viewType, builder)
}

fun <T, VH : ItemViewHolder> RecyclerAdapterBuilder<T, VH>.bindViewHolder(
    viewType: Int = 0,
    binder: ViewHolderBinder<in T, in VH>
) = apply {
    addViewHolderBinder(viewType, binder)
}

class RecyclerAdapterBuilder<T, VH : ItemViewHolder>(context: Context) :
    CommonRecyclerAdapter<T, VH>(context) {

    private var itemViewTypeSelector: AdapterItemViewTypeSelector? = null
    private val viewHolderBuilders = mutableMapOf<Int, ViewHolderBuilder<out VH>>()
    private val viewHolderBinders = mutableMapOf<Int, ViewHolderBinder<in T, in VH>>()

    fun addViewHolderBuilder(viewType: Int, builder: ViewHolderBuilder<out VH>) {
        viewHolderBuilders[viewType] = builder
    }

    fun addViewHolderBinder(viewType: Int, binder: ViewHolderBinder<in T, in VH>) {
        viewHolderBinders[viewType] = binder
    }

    fun setItemViewTypeSelector(selector: AdapterItemViewTypeSelector) {
        itemViewTypeSelector = selector
    }

    override fun getItemViewType(position: Int): Int {
        return itemViewTypeSelector?.select(position) ?: super.getItemViewType(position)
    }

    override fun onCreateItemViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): VH {
        return viewHolderBuilders.getValue(viewType).build(inflater, parent)
    }

    override fun onBindItemViewHolder(holder: VH, item: T, payloads: MutableList<Any>) {
        viewHolderBinders.getValue(holder.itemViewType).bind(holder, item, payloads)
    }

}

fun interface AdapterItemViewTypeSelector {

    fun select(position: Int): Int

}

fun interface ViewHolderBuilder<VH : ItemViewHolder> {

    fun build(inflater: LayoutInflater, parent: ViewGroup): VH

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