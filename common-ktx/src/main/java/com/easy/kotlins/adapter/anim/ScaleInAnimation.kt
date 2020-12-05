package com.easy.kotlins.adapter.anim

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View


class ScaleInAnimation @JvmOverloads constructor(
    private val from: Float = DEFAULT_SCALE_FROM,
    itemAnimationMode: ItemViewAnimationMode = ItemViewAnimationMode.UPWARD,
) : BaseItemViewAnimation(itemAnimationMode) {

    override fun getAnimators(view: View): Array<Animator> {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", from, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", from, 1f)
        return arrayOf(scaleX, scaleY)
    }

    companion object {
        private const val DEFAULT_SCALE_FROM = .5f
    }
}