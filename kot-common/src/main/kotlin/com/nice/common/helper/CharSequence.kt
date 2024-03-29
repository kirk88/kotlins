@file:Suppress("UNUSED")

package com.nice.common.helper

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ScaleXSpan
import android.text.style.StyleSpan
import androidx.annotation.ColorInt

fun CharSequence.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

inline fun CharSequence.toSpannableStringBuilder(builderAction: SpannableStringBuilder.() -> Unit = {}): SpannableStringBuilder {
    val builder = if (this is SpannableStringBuilder) this else SpannableStringBuilder(this)
    return builder.apply(builderAction)
}

fun CharSequence.highlight(
    target: String,
    startIndex: Int = 0,
    @ColorInt color: Int = Color.RED,
    size: Int = 0,
    style: Int = Typeface.NORMAL
): CharSequence {
    return toSpannableStringBuilder {
        for (result in target.toRegex().findAll(this, startIndex)) {
            setSpan(
                ForegroundColorSpan(color),
                result.range.first,
                result.range.last + 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            if (size != 0) {
                setSpan(
                    AbsoluteSizeSpan(size),
                    result.range.first,
                    result.range.last + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            if (style != Typeface.NORMAL) {
                setSpan(
                    StyleSpan(style),
                    result.range.first,
                    result.range.last + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }
}

fun CharSequence.highlight(
    start: Int = 0,
    end: Int = length,
    @ColorInt color: Int = Color.RED,
    size: Int = 0,
    style: Int = Typeface.NORMAL
): CharSequence {
    return toSpannableStringBuilder {
        setSpan(ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        if (size != 0) {
            setSpan(AbsoluteSizeSpan(size), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        if (style != Typeface.NORMAL) {
            setSpan(StyleSpan(style), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
}

fun CharSequence.insertImage(
    where: Int,
    drawable: Drawable,
    width: Int = 0,
    height: Int = 0,
    prefix: String = "",
    postfix: String = ""
): CharSequence = toSpannableStringBuilder {
    insert(where, drawable.toSpanned(width, height, prefix, postfix))
}

fun CharSequence.appendImage(
    drawable: Drawable,
    width: Int = 0,
    height: Int = 0,
    prefix: String = "",
    postfix: String = ""
): CharSequence = toSpannableStringBuilder {
    append(drawable.toSpanned(width, height, prefix, postfix))
}

fun CharSequence.insert(
    where: Int,
    text: CharSequence,
    span: Any? = null,
    start: Int = 0,
    end: Int = text.length,
    flags: Int = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
): CharSequence {
    return toSpannableStringBuilder {
        insert(where, text.toSpannableStringBuilder().also {
            if (span != null) {
                it.setSpan(span, start, end, flags)
            }
        })
    }
}

fun CharSequence.append(
    text: CharSequence,
    span: Any? = null,
    start: Int = 0,
    end: Int = text.length,
    flags: Int = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
): CharSequence {
    return toSpannableStringBuilder {
        append(if (span != null) text.withSpan(span, start, end, flags) else text)
    }
}

fun CharSequence.appendLine(
    text: CharSequence,
    span: Any? = null,
    start: Int = 0,
    end: Int = text.length,
    flags: Int = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
): CharSequence {
    return toSpannableStringBuilder {
        append('\n').append(if (span != null) text.withSpan(span, start, end, flags) else text)
    }
}

fun CharSequence.withSpan(
    what: Any,
    start: Int = 0,
    end: Int = length,
    flags: Int = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
): CharSequence {
    return toSpannableStringBuilder {
        setSpan(what, start, end, flags)
    }
}

fun CharSequence.justify(ems: Int): SpannableStringBuilder {
    val builder = SpannableStringBuilder(this)
    if (length >= ems || length <= 1) {
        return builder
    }
    val scale = (ems - length).toFloat() / (length - 1)
    val expectSize = length * 2 - 2
    var index = 0
    while (index < expectSize) {
        val blank = SpannableString("　")
        blank.setSpan(ScaleXSpan(scale), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.insert(index + 1, blank)
        index += 2
    }
    return builder
}

inline fun <R : CharSequence, C : R> C?.ifNull(defaultValue: () -> R): R = this ?: defaultValue()

inline fun <R : CharSequence, C : R> C?.ifNullOrEmpty(defaultValue: () -> R): R = if (this.isNullOrEmpty()) defaultValue() else this

inline fun <R : CharSequence, C : R> C?.ifNullOrBlack(defaultValue: () -> R): R = if (this.isNullOrBlank()) defaultValue() else this

fun CharSequence.splitSkipBlank(
    vararg delimiters: String,
    ignoreCase: Boolean = false,
    limit: Int = 0
): List<String> {
    return split(
        ignoreCase = ignoreCase,
        limit = limit,
        delimiters = delimiters
    ).filter { it.isNotBlank() }
}