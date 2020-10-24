package com.easy.kotlins.adapter

import android.content.Context
import androidx.annotation.LayoutRes

abstract class SimpleRecyclerAdapter<ITEM>(context: Context, @LayoutRes private val resource: Int) :
    CommonRecyclerAdapter<ITEM>(context) {

    init {
        addItemViewDelegate(0, object : ItemViewDelegate<ITEM>(context, resource) {

            override fun convert(holder: ItemViewHolder, item: ITEM, payloads: MutableList<Any>) {
                this@SimpleRecyclerAdapter.convert(holder, item, payloads)
            }

            override fun registerListener(holder: ItemViewHolder) {
                this@SimpleRecyclerAdapter.registerListener(holder)
            }
        })
    }

    /**
     * 如果使用了事件回调,回调里不要直接使用item,会出现不更新的问题,
     * 使用getItem(holder.layoutPosition)来获取item
     */
    abstract fun convert(holder: ItemViewHolder, item: ITEM, payloads: MutableList<Any>)

    /**
     * 注册事件
     */
    open fun registerListener(holder: ItemViewHolder) {

    }
}