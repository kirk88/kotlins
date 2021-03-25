package com.easy.kotlins.adapter.anim

import android.animation.Animator
import android.animation.AnimatorSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.easy.kotlins.helper.opt

abstract class BaseItemViewAnimation(protected open val animationMode: ItemViewAnimationMode) :
    ItemViewAnimation {

    private var lastAnimateIndex = -1

    private var animatorSet: AnimatorSet? = null

    protected open fun getAnimators(holder: RecyclerView.ViewHolder): Array<Animator> {
        val animators: Array<Animator> = when (animationMode) {
            ItemViewAnimationMode.UPWARD -> (lastAnimateIndex < holder.layoutPosition).opt(
                getAnimators(holder.itemView), emptyArray()
            )
            ItemViewAnimationMode.DOWNWARD -> (lastAnimateIndex > holder.layoutPosition).opt(
                getAnimators(holder.itemView), emptyArray()
            )
            ItemViewAnimationMode.NORMAL -> getAnimators(holder.itemView)
        }
        lastAnimateIndex = holder.layoutPosition
        return animators
    }

    override fun start(holder: RecyclerView.ViewHolder) {
        val animators = getAnimators(holder)
        if (animators.isNotEmpty()) {
            AnimatorSet().also {
                animatorSet = it
                it.playTogether(*animators)
            }.start()
        }
    }

    override fun stop() {
        animatorSet?.cancel()
    }

    override fun reset() {
        lastAnimateIndex = -1
    }

    protected abstract fun getAnimators(view: View): Array<Animator>

}