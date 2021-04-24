@file:Suppress("unused")

package com.nice.kotlins.helper

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.nice.kotlins.R

fun Context.getColorCompat(@ColorRes resId: Int): Int = ContextCompat.getColor(this, resId)

fun Context.getColorStateListCompat(@ColorRes resId: Int): ColorStateList? =
    ContextCompat.getColorStateList(this, resId)

fun Context.getDrawableCompat(@DrawableRes resId: Int): Drawable = requireNotNull(
    ContextCompat.getDrawable(
        this,
        resId
    )
)

fun Fragment.getColorCompat(@ColorRes resId: Int): Int = ContextCompat.getColor(
    requireContext(),
    resId
)

fun Fragment.getColorStateListCompat(@ColorRes resId: Int): ColorStateList? =
    ContextCompat.getColorStateList(
        requireContext(),
        resId
    )

fun Fragment.getDrawableCompat(@DrawableRes resId: Int): Drawable = requireNotNull(
    ContextCompat.getDrawable(
        requireContext(),
        resId
    )
)

fun Context.getDimension(@DimenRes resId: Int): Float = resources.getDimension(resId)

fun Context.getDimensionPixelOffset(@DimenRes resId: Int): Int =
    resources.getDimensionPixelOffset(resId)

fun Context.getDimensionPixelSize(@DimenRes resId: Int): Int =
    resources.getDimensionPixelSize(resId)

val Context.screenWidthPixels: Int
    get() = resources.displayMetrics.widthPixels

val Context.screenHeightPixels: Int
    get() = resources.displayMetrics.heightPixels

val Context.actionBarHeight: Int
    get() {
        val ta = this.obtainStyledAttributes(intArrayOf(R.attr.actionBarSize))
        val result = ta.getDimensionPixelSize(0, 0)
        ta.recycle()
        return result
    }

val Context.statusBarHeight: Int
    get() {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

val Context.navigationBarHeight: Int
    get() {
        var result = 0
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }