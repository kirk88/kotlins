package com.easy.kotlins.adapter.anim

import androidx.recyclerview.widget.RecyclerView

interface ItemViewAnimation {

    fun start(holder: RecyclerView.ViewHolder)

    fun stop()
}