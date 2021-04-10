package com.nice.kotlins.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes

abstract class SimpleRecyclerAdapter<T>(
    context: Context,
    @LayoutRes private val itemLayoutId: Int
) : CommonRecyclerAdapter<T, ItemViewHolder>(context) {

    final override fun onCreateItemViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder {
        return ItemViewHolder(inflater.inflate(itemLayoutId, parent, false))
    }

}