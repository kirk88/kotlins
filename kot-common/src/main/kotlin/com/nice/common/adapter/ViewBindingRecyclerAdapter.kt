package com.nice.common.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

abstract class ViewBindingRecyclerAdapter<T, VB : ViewBinding>(
    context: Context,
    items: List<T>? = null
) : CommonRecyclerAdapter<T, ViewBindingHolder<VB>>(context, items) {

    abstract fun onCreateItemViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): VB

    final override fun onCreateItemViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ViewBindingHolder<VB> {
        return ViewBindingHolder(onCreateItemViewBinding(inflater, parent, viewType))
    }

}