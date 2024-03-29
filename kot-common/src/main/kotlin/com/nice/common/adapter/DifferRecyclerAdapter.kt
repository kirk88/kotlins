@file:Suppress("UNUSED")

package com.nice.common.adapter

import android.content.Context
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.AsyncListDiffer.ListListener
import androidx.recyclerview.widget.DiffUtil

abstract class DifferRecyclerAdapter<T, VH : ItemViewHolder> : BaseRecyclerAdapter<T, VH> {

    override val items: List<T>
        get() = differ.currentList

    val differ: AsyncListDiffer<T>
    private val changeListener = ListListener<T> { previousList, currentList ->
        onCurrentListChanged(
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

operator fun <T> DifferRecyclerAdapter<T, *>.plusAssign(items: List<T>?) = submitList(items)