package com.nice.kotlins.adapter.anim

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View

class ScaleInAnimation(
    private val fromX: Float = DEFAULT_SCALE_FROM_X,
    private val fromY: Float = DEFAULT_SCALE_FROM_Y,
    itemAnimationMode: ItemViewAnimationMode = ItemViewAnimationMode.UPWARD,
) : BaseItemViewAnimation(itemAnimationMode) {

    override fun getAnimators(view: View): Array<Animator> {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", fromX, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", fromY, 1f)
        return arrayOf(scaleX, scaleY)
    }

    companion object {
        private const val DEFAULT_SCALE_FROM_X = .5f
        private const val DEFAULT_SCALE_FROM_Y = .5f
    }
}