@file:Suppress("unused")

package com.nice.kotlins.adapter.anim

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View

class AlphaInAnimation(
        private val from: Float = DEFAULT_ALPHA_FROM,
        itemAnimationMode: ItemViewAnimationMode = ItemViewAnimationMode.UPWARD
) : BaseItemViewAnimation(itemAnimationMode) {

    override fun getAnimators(view: View): List<Animator> = listOf(ObjectAnimator.ofFloat(view, "alpha", from, 1f))


    companion object {
        private const val DEFAULT_ALPHA_FROM = 0f
    }

}
