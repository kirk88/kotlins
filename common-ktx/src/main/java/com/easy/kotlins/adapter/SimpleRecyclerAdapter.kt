@file:Suppress("unused")

package com.easy.kotlins.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.easy.kotlins.adapter.anim.ItemViewAnimation

abstract class SimpleRecyclerAdapter<ITEM>(
    context: Context,
    @LayoutRes private val layoutResId: Int = 0,
    vararg itemDelegates: Pair<Int, ItemViewDelegate<out ITEM>>,
    itemAnimation: ItemViewAnimation? = null,
    itemClickable: Boolean = false,
    itemLongClickable: Boolean = false
) : CommonRecyclerAdapter<ITEM>(
    context = context,
    itemAnimation = itemAnimation,
    itemClickable = itemClickable,
    itemLongClickable = itemLongClickable,
    itemDelegates = itemDelegates
) {

    init {
        addItemViewDelegate(0, object : ItemViewDelegate<ITEM>(context) {

            override fun onCreateItemView(
                inflater: LayoutInflater,
                parent: ViewGroup,
                viewType: Int
            ): View {
                return this@SimpleRecyclerAdapter.onCreateItemView(inflater, parent, viewType)
            }

            override fun onBindViewHolder(
                holder: ItemViewHolder,
                item: ITEM,
                payloads: MutableList<Any>
            ) {
                this@SimpleRecyclerAdapter.onBindViewHolder(holder, item, payloads)
            }

        })
    }

    open fun onCreateItemView(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): View {
        return inflater.inflate(layoutResId, parent, false)
    }


    abstract fun onBindViewHolder(holder: ItemViewHolder, item: ITEM, payloads: MutableList<Any>)

}