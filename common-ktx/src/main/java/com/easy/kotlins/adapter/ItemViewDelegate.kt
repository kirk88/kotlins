package com.easy.kotlins.adapter

import android.content.Context
import androidx.annotation.LayoutRes

abstract class ItemViewDelegate<ITEM>(protected val context: Context,@LayoutRes val layoutResId: Int) {

    abstract fun onBindViewHolder(holder: ItemViewHolder, item: ITEM, payloads: MutableList<Any>)


    open fun onViewHolderCreated(holder: ItemViewHolder){

    }

}