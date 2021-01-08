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

    /**
     * 如果使用了事件回调,回调里不要直接使用item,会出现不更新的问题,
     * 使用getItem(holder.layoutPosition)来获取item
     */
    abstract fun convert(holder: ItemViewHolder, item: ITEM, payloads: MutableList<Any>)

}