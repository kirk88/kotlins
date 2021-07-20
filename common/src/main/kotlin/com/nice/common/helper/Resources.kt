@file:Suppress("unused")

package com.nice.common.helper

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.nice.common.R
import kotlin.math.pow
import kotlin.math.sqrt

fun Context.getColorCompat(@ColorRes resId: Int): Int = ContextCompat.getColor(this, resId)

fun Context.getColorStateListCompat(@ColorRes resId: Int): ColorStateList? = ContextCompat.getColorStateList(this, resId)

fun Context.getDrawableCompat(@DrawableRes resId: Int): Drawable? = ContextCompat.getDrawable(this, resId)

fun Fragment.getColorCompat(@ColorRes resId: Int): Int = ContextCompat.getColor(requireContext(), resId)

fun Fragment.getColorStateListCompat(@ColorRes resId: Int): ColorStateList? = ContextCompat.getColorStateList(requireContext(), resId)

fun Fragment.getDrawableCompat(@DrawableRes resId: Int): Drawable? = ContextCompat.getDrawable(requireContext(), resId)

fun Context.getDimension(@DimenRes resId: Int): Float = resources.getDimension(resId)

fun Context.getDimensionPixelOffset(@DimenRes resId: Int): Int = resources.getDimensionPixelOffset(resId)

fun Context.getDimensionPixelSize(@DimenRes resId: Int): Int = resources.getDimensionPixelSize(resId)

val Context.screenWidthPixels: Int
    get() = resources.displayMetrics.widthPixels

val Context.screenHeightPixels: Int
    get() = resources.displayMetrics.heightPixels

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

val Context.defaultActionBarHeight: Int
    get() {
        val ta = obtainStyledAttributes(intArrayOf(R.attr.actionBarSize))
        val result = ta.getDimensionPixelSize(0, 0)
        ta.recycle()
        return result
    }

val Activity.actionBarHeight: Int
    get() = actionBar?.height.ifNullOrZero { defaultActionBarHeight }

val AppCompatActivity.actionBarHeight: Int
    get() = supportActionBar?.height.ifNullOrZero { defaultActionBarHeight }

val Context.isTabletDevice: Boolean
    get() {
        val dm = resources.displayMetrics
        val x = (dm.widthPixels / dm.xdpi).toDouble().pow(2.0)
        val y = (dm.heightPixels / dm.ydpi).toDouble().pow(2.0)
        val screenInches = sqrt(x + y)
        return screenInches >= 7.0
    }