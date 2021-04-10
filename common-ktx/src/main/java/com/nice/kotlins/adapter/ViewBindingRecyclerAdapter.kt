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

    abstract fun onBindItemView(
        binding: VB,
        item: T,
        payloads: MutableList<Any>
    )

    final override fun onCreateItemViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ViewBindingHolder<VB> {
        return ViewBindingHolder(onCreateItemView(inflater, parent, viewType))
    }

    final override fun onBindItemViewHolder(
        holder: ViewBindingHolder<VB>,
        item: T,
        payloads: MutableList<Any>
    ) {
        onBindItemView(holder.binding, item, payloads)
    }

}