package com.easy.kotlins.adapter.anim

import android.animation.Animator
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.easy.kotlins.helper.opt


abstract class BaseItemViewAnimation(private val itemAnimationMode: ItemViewAnimationMode) :
    ItemViewAnimation {

    private var lastAnimateIndex = -1

    final override fun getAnimators(holder: RecyclerView.ViewHolder): Array<Animator> {
        val animators: Array<Animator> = when (getAnimationMode()) {
            ItemViewAnimationMode.UPWARD -> (lastAnimateIndex < holder.layoutPosition).opt(
                getAnimators(
                    holder.itemView
                ), emptyArray()
            )
            ItemViewAnimationMode.DOWNWARD -> (lastAnimateIndex > holder.layoutPosition).opt(
                getAnimators(
                    holder.itemView
                ), emptyArray()
            )
            ItemViewAnimationMode.NORMAL -> getAnimators(
                holder.itemView
            )
        }
        lastAnimateIndex = holder.layoutPosition
        return animators
    }

    override fun getAnimationMode(): ItemViewAnimationMode {
        return itemAnimationMode
    }

    abstract fun getAnimators(view: View): Array<Animator>
}