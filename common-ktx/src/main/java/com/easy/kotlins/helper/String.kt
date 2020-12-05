package com.easy.kotlins.helper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import androidx.annotation.ColorInt
import com.google.gson.JsonParser

/**
 * Create by LiZhanPing on 2020/8/27
 */

fun CharSequence.asEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

fun CharSequence.asSpannableBuilder(): SpannableStringBuilder =
    if (this is SpannableStringBuilder) this else SpannableStringBuilder(this)

fun CharSequence.heightLight(keywords: String?, color: Int = Color.RED): SpannableStringBuilder {
    return if (keywords.isNullOrBlank()) this.asSpannableBuilder() else keywords.let {
        SpannableStringBuilder(this).also {
            var result = keywords.toRegex().find(it)
            while (result != null) {
                it.setSpan(
                    ForegroundColorSpan(color),
                    result.range.first,
                    result.range.last + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                result = result.next()
            }
        }
    }
}

fun CharSequence.heightLight(
    start: Int = 0,
    end: Int = length,
    @ColorInt color: Int = Color.RED
): SpannableStringBuilder? {
    return this.asSpannableBuilder().apply {
        setSpan(ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}

fun CharSequence.insertImage(
    where: Int,
    drawable: Drawable,
    textSize: Int = 0,
    prefix: String = "",
    postfix: String = ""
): SpannableStringBuilder {
    if (textSize > 0) drawable.setBounds(0, 0, textSize, textSize)
    else drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    return asSpannableBuilder().insert(where, SpannableString("$prefix $postfix").apply {
        setSpan(
            CenterAlignImageSpan(drawable),
            prefix.length,
            length - postfix.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    })
}

fun CharSequence.appendImage(
    drawable: Drawable,
    textSize: Int = 0,
    prefix: String = "",
    postfix: String = ""
): SpannableStringBuilder {
    if (textSize > 0) drawable.setBounds(0, 0, textSize, textSize)
    else drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    return asSpannableBuilder().append(SpannableString("$prefix $postfix").apply {
        setSpan(
            CenterAlignImageSpan(drawable),
            prefix.length,
            length - postfix.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    })
}

fun CharSequence.append(
    text: CharSequence,
    span: Any? = null,
    start: Int = 0,
    end: Int = text.length,
    flags: Int = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
): SpannableStringBuilder {
    return asSpannableBuilder().apply {
        append(text.asSpannableBuilder().also {
            it.setSpan(span, start, end, flags)
        })
    }
}

fun CharSequence.appendLine(
    text: CharSequence,
    span: Any? = null,
    start: Int = 0,
    end: Int = text.length,
    flags: Int = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
): SpannableStringBuilder {
    return asSpannableBuilder().apply {
        append('\n').append(text.asSpannableBuilder().also {
            if (span != null) {
                it.setSpan(span, start, end, flags)
            }
        })
    }
}

class CenterAlignImageSpan(drawable: Drawable) : ImageSpan(drawable) {

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val d = drawable
        val fm = paint.fontMetricsInt
        val transY = (y + fm.descent + y + fm.ascent) / 2 - d.bounds.bottom / 2
        canvas.save()
        canvas.translate(x, transY.toFloat())
        d.draw(canvas)
        canvas.restore()
    }
}

fun CharSequence.withSpan(
    what: Any,
    start: Int = 0,
    end: Int = length,
    flags: Int = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
): CharSequence {
    return this.asSpannableBuilder().apply {
        setSpan(what, start, end, flags)
    }
}

inline fun String?.notNull(defaultValue: () -> String = { "" }): String =
    if (this == null || this == "null") defaultValue() else this

inline fun String?.notNullOrEmpty(defaultValue: () -> String = { "" }): String =
    if (this.isNullOrEmpty()) defaultValue() else this

inline fun String?.notNullOrBlank(defaultValue: () -> String = { "" }): String =
    if (this.isNullOrBlank()) defaultValue() else this

inline fun String.notEmpty(defaultValue: () -> String = { "" }): String =
    if (this.isEmpty()) defaultValue() else this

inline fun String.notBlank(defaultValue: () -> String = { "" }): String =
    if (this.isBlank()) defaultValue() else this

val String.isJsonArray: Boolean
    get() = this.runCatching { JsonParser().parse(this) }.getOrNull()?.isJsonArray ?: false

val String.isJsonObject: Boolean
    get() = this.runCatching { JsonParser().parse(this) }.getOrNull()?.isJsonObject ?: false

fun CharSequence.splitNotBlank(
    vararg delimiters: String,
    ignoreCase: Boolean = false,
    limit: Int = 0
): List<String> {
    return split(delimiters = delimiters, ignoreCase, limit).filter { it.isNotBlank() }
}


fun <K, V> Map<K, V>.joinKeysToString(
    separator: CharSequence = ", ",
    prefix: CharSequence = "",
    postfix: CharSequence = "",
    limit: Int = -1,
    truncated: CharSequence = "...",
    transform: ((K) -> CharSequence)? = null
): String {
    return keys.joinToString(separator, prefix, postfix, limit, truncated, transform)
}

fun <K, V> Map<K, V>.joinValuesToString(
    separator: CharSequence = ", ",
    prefix: CharSequence = "",
    postfix: CharSequence = "",
    limit: Int = -1,
    truncated: CharSequence = "...",
    transform: ((V) -> CharSequence)? = null
): String {
    return values.joinToString(separator, prefix, postfix, limit, truncated, transform)
}

fun <K, V, A : Appendable> Map<K, V>.joinKeysTo(
    buffer: A,
    separator: CharSequence = ", ",
    prefix: CharSequence = "",
    postfix: CharSequence = "",
    limit: Int = -1,
    truncated: CharSequence = "...",
    transform: ((K) -> CharSequence)? = null
): A {
    return keys.joinTo(buffer, separator, prefix, postfix, limit, truncated, transform)
}

fun <K, V, A : Appendable> Map<K, V>.joinValuesTo(
    buffer: A,
    separator: CharSequence = ", ",
    prefix: CharSequence = "",
    postfix: CharSequence = "",
    limit: Int = -1,
    truncated: CharSequence = "...",
    transform: ((V) -> CharSequence)? = null
): A {
    return values.joinTo(buffer, separator, prefix, postfix, limit, truncated, transform)
}