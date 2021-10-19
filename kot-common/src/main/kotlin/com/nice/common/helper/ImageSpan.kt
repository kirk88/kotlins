@file:Suppress("unused")

package com.nice.common.helper

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ImageSpan
import androidx.core.text.set
import androidx.core.text.toSpannable

internal class CenterAlignImageSpan(drawable: Drawable) : ImageSpan(drawable) {

    override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        val d = drawable
        canvas.save()
        canvas.translate(x, (bottom - top - d.bounds.bottom) / 2.toFloat() + top)
        d.draw(canvas)
        canvas.restore()
    }

    override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        val d = drawable
        val rect = d.bounds
        if (fm != null) {
            val fmInt = paint.fontMetricsInt
            val fontHeight = fmInt.bottom - fmInt.top
            val drHeight = rect.bottom - rect.top
            val top = drHeight / 2 - fontHeight / 4
            val bottom = drHeight / 2 + fontHeight / 4
            fm.ascent = -bottom
            fm.top = -bottom
            fm.bottom = top
            fm.descent = top
        }
        return rect.right
    }
}

fun Drawable.toSpannable(
    width: Int = 0,
    height: Int = 0,
    prefix: String = "",
    postfix: String = ""
): Spannable {
    if (width > 0 && height > 0) setBounds(0, 0, width, height)
    else if (bounds.isEmpty) setBounds(0, 0, intrinsicWidth, intrinsicHeight)
    val spannable = "$prefix $postfix".toSpannable()
    spannable[prefix.length, spannable.length - postfix.length] = CenterAlignImageSpan(this)
    return spannable
}

fun Drawable.toSpanned(
    width: Int = 0,
    height: Int = 0,
    prefix: String = "",
    postfix: String = ""
): Spanned {
    if (width > 0 && height > 0) setBounds(0, 0, width, height)
    else if (bounds.isEmpty) setBounds(0, 0, intrinsicWidth, intrinsicHeight)
    val string = SpannableString("$prefix $postfix")
    string[prefix.length, string.length - postfix.length] = CenterAlignImageSpan(this)
    return string
}