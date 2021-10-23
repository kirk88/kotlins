@file:Suppress("UNUSED")

package com.nice.common.helper

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat

fun Drawable.wrappedCompat(): Drawable = DrawableCompat.wrap(this)

fun <T : Drawable> Drawable.unwrappedCompat(): T = DrawableCompat.unwrap(this)

fun Drawable.setTintCompat(@ColorInt color: Int): Drawable = wrappedCompat().also {
    DrawableCompat.setTint(it, color)
}

fun Drawable.setTintListCompat(color: ColorStateList): Drawable = wrappedCompat().also {
    DrawableCompat.setTintList(it, color)
}

fun Drawable.setTintModeCompat(mode: PorterDuff.Mode): Drawable = wrappedCompat().also {
    DrawableCompat.setTintMode(it, mode)
}

fun Drawable.clearColorFilterCompat() = DrawableCompat.clearColorFilter(this)

fun Drawable.withIntrinsicBounds(): Drawable = apply {
    setBounds(0, 0, intrinsicWidth, intrinsicHeight)
}