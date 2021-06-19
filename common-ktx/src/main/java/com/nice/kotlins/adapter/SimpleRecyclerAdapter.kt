@file:Suppress("unused")

package com.nice.kotlins.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

abstract class SimpleRecyclerAdapter<T>(
        context: Context,
        @LayoutRes private val itemLayoutId: Int = 0
) : CommonRecyclerAdapter<T, ItemViewHolder>(context) {

    final override fun onCreateItemViewHolder(
            inflater: LayoutInflater,
            parent: ViewGroup,
            viewType: Int
    ): ItemViewHolder {
        return ItemViewHolder(onCreateItemView(inflater, parent, viewType))
    }

    open fun onCreateItemView(
            inflater: LayoutInflater,
            parent: ViewGroup,
            viewType: Int
    ): View {
        return inflater.inflate(itemLayoutId, parent, false)
    }


    override fun onBindItemViewHolder(holder: ItemViewHolder, item: T, payloads: MutableList<Any>) {

    }

}