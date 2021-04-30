package com.nice.kotlins.adapter.anim

import android.animation.Animator
import android.animation.AnimatorSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseItemViewAnimation(private val animationMode: ItemViewAnimationMode) :
    ItemViewAnimation {

    private var lastAnimateIndex = -1

    protected open fun getAnimatorSet(holder: RecyclerView.ViewHolder): AnimatorSet? {
        val animatorSet = when (animationMode) {
            ItemViewAnimationMode.UPWARD -> if (lastAnimateIndex < holder.layoutPosition) {
                AnimatorSet().apply { playTogether(getAnimators(holder.itemView)) }
            } else null
            ItemViewAnimationMode.DOWNWARD -> if (lastAnimateIndex > holder.layoutPosition) {
                AnimatorSet().apply { playTogether(getAnimators(holder.itemView)) }
            } else null
            ItemViewAnimationMode.NORMAL -> AnimatorSet().apply { playTogether(getAnimators(holder.itemView)) }
        }
        lastAnimateIndex = holder.layoutPosition
        return animatorSet
    }

    override fun start(holder: RecyclerView.ViewHolder) {
        getAnimatorSet(holder)?.start()
    }

    override fun reset() {
        lastAnimateIndex = -1
    }

    protected abstract fun getAnimators(view: View): List<Animator>

}