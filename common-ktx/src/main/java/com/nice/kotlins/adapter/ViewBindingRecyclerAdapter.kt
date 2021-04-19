package com.nice.kotlins.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

abstract class ViewBindingRecyclerAdapter<T, VB : ViewBinding>(
    context: Context,
) : CommonRecyclerAdapter<T, ViewBindingHolder<VB>>(context) {

    abstract fun onCreateItemView(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): VB

    final override fun onCreateItemViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ViewBindingHolder<VB> {
        return ViewBindingHolder(onCreateItemView(inflater, parent, viewType))
    }

}