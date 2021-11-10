@file:Suppress("UNUSED")

package com.nice.common.helper

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

fun Context.getColorCompat(@ColorRes resId: Int): Int = ContextCompat.getColor(this, resId)

fun Context.getColorStateListCompat(@ColorRes resId: Int): ColorStateList? = ContextCompat.getColorStateList(this, resId)

fun Context.getDrawableCompat(@DrawableRes resId: Int): Drawable? = ContextCompat.getDrawable(this, resId)

fun Fragment.getColorCompat(@ColorRes resId: Int): Int = ContextCompat.getColor(requireContext(), resId)

fun Fragment.getColorStateListCompat(@ColorRes resId: Int): ColorStateList? = ContextCompat.getColorStateList(requireContext(), resId)

fun Fragment.getDrawableCompat(@DrawableRes resId: Int): Drawable? = ContextCompat.getDrawable(requireContext(), resId)

fun Context.getDimension(@DimenRes resId: Int): Float = resources.getDimension(resId)

fun Context.getDimensionPixelOffset(@DimenRes resId: Int): Int = resources.getDimensionPixelOffset(resId)

fun Context.getDimensionPixelSize(@DimenRes resId: Int): Int = resources.getDimensionPixelSize(resId)