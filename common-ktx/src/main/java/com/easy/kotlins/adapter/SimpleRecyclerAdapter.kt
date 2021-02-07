package com.easy.kotlins.adapter

import android.content.Context
import androidx.annotation.LayoutRes
import com.easy.kotlins.adapter.anim.ItemViewAnimation

abstract class SimpleRecyclerAdapter<ITEM>(
    context: Context, @LayoutRes private val layoutResId: Int,
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
        addItemViewDelegate(0, object : ItemViewDelegate<ITEM>(context, layoutResId) {

            override fun convert(holder: ItemViewHolder, item: ITEM, payloads: MutableList<Any>) {
                this@SimpleRecyclerAdapter.convert(holder, item, payloads)
            }

        })
    }

    abstract fun convert(holder: ItemViewHolder, item: ITEM, payloads: MutableList<Any>)

}