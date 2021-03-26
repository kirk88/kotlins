package com.easy.kotlins.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

abstract class ItemViewDelegate<ITEM>(
    val context: Context,
    @LayoutRes private val layoutResId: Int = 0
) {

    open fun onCreateItemView(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): View {
        return inflater.inflate(layoutResId, parent, false)
    }

    open fun onViewHolderCreated(holder: ItemViewHolder) {

    }

    abstract fun onBindViewHolder(holder: ItemViewHolder, item: ITEM, payloads: MutableList<Any>)

}