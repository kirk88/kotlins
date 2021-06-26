@file:Suppress("unused")

package com.nice.kotlins.adapter

import android.content.Context
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.AsyncListDiffer.ListListener

abstract class DifferRecyclerAdapter<T, VH : ItemViewHolder> : BaseRecyclerAdapter<T, VH> {


    override val items: List<T>
        get() = differ.currentList

    val differ: AsyncListDiffer<T>
    private val changeListener = ListListener<T> { previousList, currentList ->
        this@DifferRecyclerAdapter.onCurrentListChanged(
            previousList,
            currentList
        )
    }

    private val updateCallback by lazy {
        AdapterListUpdateCallback(this)
    }

    constructor(context: Context, diffCallback: DiffUtil.ItemCallback<T>) : super(context) {
        differ = AsyncListDiffer(updateCallback, AsyncDifferConfig.Builder(diffCallback).build())
        differ.addListListener(changeListener)
    }

    constructor(context: Context, diffConfig: AsyncDifferConfig<T>) : super(context) {
        differ = AsyncListDiffer(updateCallback, diffConfig)
        differ.addListListener(changeListener)
    }

    fun submitList(list: List<T>?) {
        differ.submitList(list)
    }

    fun submitList(list: List<T>?, commitCallback: Runnable) {
        differ.submitList(list, commitCallback)
    }

    open fun onCurrentListChanged(previousList: List<T>, currentList: List<T>) {

    }

}

operator fun <T> DifferRecyclerAdapter<T, *>.plusAssign(items: List<T>) = submitList(items)

operator fun <T> DifferRecyclerAdapter<T, *>.plusAssign(items: Array<T>) = submitList(items.toList())