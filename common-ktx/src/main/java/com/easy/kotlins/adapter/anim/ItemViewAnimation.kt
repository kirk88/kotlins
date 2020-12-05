package com.easy.kotlins.adapter.anim

import android.animation.Animator
import androidx.recyclerview.widget.RecyclerView

interface ItemViewAnimation {

    fun getAnimators(holder: RecyclerView.ViewHolder): Array<Animator>

    fun getAnimationMode(): ItemViewAnimationMode
}