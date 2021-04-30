@file:Suppress("unused")

package com.nice.kotlins.adapter.anim

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View

class SlideInBottomAnimation(
    private val from: Float = DEFAULT_TRANSLATION_FROM,
    itemAnimationMode: ItemViewAnimationMode = ItemViewAnimationMode.UPWARD,
) : BaseItemViewAnimation(itemAnimationMode) {

    override fun getAnimators(view: View): List<Animator> =
        listOf(
            ObjectAnimator.ofFloat(
                view,
                "translationY",
                view.measuredHeight.toFloat() * from,
                0f
            )
        )

    companion object {
        private const val DEFAULT_TRANSLATION_FROM = 1f
    }
}