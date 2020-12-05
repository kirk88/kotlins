package com.easy.kotlins.adapter.anim

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View


class SlideInRightAnimation(
    itemAnimationMode: ItemViewAnimationMode = ItemViewAnimationMode.UPWARD,
) : BaseItemViewAnimation(itemAnimationMode) {

    override fun getAnimators(view: View): Array<Animator> =
        arrayOf(ObjectAnimator.ofFloat(view, "translationX", view.rootView.width.toFloat(), 0f))
}
