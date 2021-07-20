@file:Suppress("unused")

package com.nice.common.helper

import androidx.viewpager2.widget.ViewPager2

inline fun ViewPager2.doOnPageScrolled(
        crossinline action: (
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
        ) -> Unit
) = registerOnPageChangeCallback(onPageScrolled = action)

inline fun ViewPager2.doOnPageSelected(crossinline action: (position: Int) -> Unit) = registerOnPageChangeCallback(onPageSelected = action)

inline fun ViewPager2.doOnPageScrollStateChanged(crossinline action: (position: Int) -> Unit) = registerOnPageChangeCallback(onPageScrollStateChanged = action)

inline fun ViewPager2.registerOnPageChangeCallback(
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
): ViewPager2.OnPageChangeCallback {
    val callback = object : ViewPager2.OnPageChangeCallback() {
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
    registerOnPageChangeCallback(callback)
    return callback
}