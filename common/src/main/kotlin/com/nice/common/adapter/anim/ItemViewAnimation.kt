package com.nice.common.adapter.anim

import androidx.recyclerview.widget.RecyclerView

interface ItemViewAnimation {

    fun setStartPosition(position: Int)

    fun start(holder: RecyclerView.ViewHolder)

}