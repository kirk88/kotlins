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

fun Context.getCompatColor(@ColorRes resId: Int): Int = ContextCompat.getColor(this, resId)

fun Context.getCompatColorStateList(@ColorRes resId: Int): ColorStateList? =
    ContextCompat.getColorStateList(this, resId)

fun Context.getCompatDrawable(@DrawableRes resId: Int): Drawable = requireNotNull(
    ContextCompat.getDrawable(
        this,
        resId
    )
)

fun Context.getDimension(@DimenRes resId: Int): Float = resources.getDimension(resId)

fun Context.getDimensionPixelOffset(@DimenRes resId: Int): Int =
    resources.getDimensionPixelOffset(resId)

fun Context.getDimensionPixelSize(@DimenRes resId: Int): Int =
    resources.getDimensionPixelSize(resId)

fun Fragment.getCompatColor(@ColorRes resId: Int): Int = ContextCompat.getColor(
    requireContext(),
    resId
)

fun Fragment.getCompatColorStateList(@ColorRes resId: Int): ColorStateList? =
    ContextCompat.getColorStateList(
        requireContext(),
        resId
    )

fun Fragment.getCompatDrawable(@DrawableRes resId: Int): Drawable = requireNotNull(
    ContextCompat.getDrawable(
        requireContext(),
        resId
    )
)

fun Fragment.getDimension(@DimenRes resId: Int): Float = resources.getDimension(resId)

fun Fragment.getDimensionPixelOffset(@DimenRes resId: Int): Int =
    resources.getDimensionPixelOffset(resId)

fun Fragment.getDimensionPixelSize(@DimenRes resId: Int): Int =
    resources.getDimensionPixelSize(resId)

val Context.screenWidthPixels: Int
    get() = resources.displayMetrics.widthPixels

val Context.screenHeightPixels: Int
    get() = resources.displayMetrics.heightPixels