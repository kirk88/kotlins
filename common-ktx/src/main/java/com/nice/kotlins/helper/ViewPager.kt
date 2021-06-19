@file:Suppress("unused")

package com.nice.kotlins.helper

import androidx.viewpager.widget.ViewPager

inline fun ViewPager.doOnPageScrolled(
        crossinline action: (
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
        ) -> Unit
) = addOnPageChangeListener(onPageScrolled = action)

inline fun ViewPager.doOnPageSelected(crossinline action: (position: Int) -> Unit) =
        addOnPageChangeListener(onPageSelected = action)

inline fun ViewPager.doOnPageScrollStateChanged(crossinline action: (position: Int) -> Unit) =
        addOnPageChangeListener(onPageScrollStateChanged = action)

inline fun ViewPager.addOnPageChangeListener(
        crossinline onPageScrolled: (
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
        ) -> Unit = { _, _, _ -> },
        crossinline onPageSelected: (
                position: Int
        ) -> Unit = { _ -> },
        crossinline onPageScrollStateChanged: (
                position: Int
        ) -> Unit = { _ -> }
): ViewPager.OnPageChangeListener {
    val listener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
        ) {
            onPageScrolled.invoke(position, positionOffset, positionOffsetPixels)
        }

        override fun onPageSelected(position: Int) {
            onPageSelected.invoke(position)
        }

        override fun onPageScrollStateChanged(position: Int) {
            onPageScrollStateChanged.invoke(position)
        }

    }
    addOnPageChangeListener(listener)
    return listener
}