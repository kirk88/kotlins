package com.easy.kotlins.adapter

import android.view.View

fun interface OnItemClickListener<T, VH : ItemViewHolder> {

    fun onItemClick(adapter: BaseRecyclerAdapter<T, VH>, holder: VH)

}

fun interface OnItemLongClickListener<T, VH: ItemViewHolder>{

    fun onItemLongClick(adapter: BaseRecyclerAdapter<T, VH>, holder: VH): Boolean

}


fun interface OnItemChildClickListener<T, VH : ItemViewHolder> {

    fun onItemChildClick(adapter: BaseRecyclerAdapter<T, VH>, holder: VH, view: View)

}

fun interface OnItemChildLongClickListener<T, VH: ItemViewHolder>{

    fun onItemChildLongClick(adapter: BaseRecyclerAdapter<T, VH>, holder: VH, view: View): Boolean

}