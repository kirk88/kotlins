package com.easy.kotlins.adapter

import android.content.Context
import androidx.annotation.LayoutRes

abstract class ItemViewDelegate<ITEM>(protected val context: Context,@LayoutRes val layoutResId: Int) {

    /**
     * 如果使用了事件回调,回调里不要直接使用item,会出现不更新的问题,
     * 使用getItem(holder.layoutPosition)来获取item,
     * 或者使用registerListener(holder: ItemViewHolder, position: Int)
     */
    abstract fun convert(holder: ItemViewHolder, item: ITEM, payloads: MutableList<Any>)


    /**
     * 注册事件
     */
    open fun registerListener(holder: ItemViewHolder){

    }

}