package com.easy.kotlins.adapter.anim

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View


class SlideInRightAnimation(
    private val from: Float = DEFAULT_TRANSLATION_FROM,
    itemAnimationMode: ItemViewAnimationMode = ItemViewAnimationMode.UPWARD,
) : BaseItemViewAnimation(itemAnimationMode) {

    override fun getAnimators(view: View): Array<Animator> =
        arrayOf(ObjectAnimator.ofFloat(view, "translationX", view.rootView.width.toFloat() * from, 0f))

    companion object{
        private const val DEFAULT_TRANSLATION_FROM = 1f
    }
}
