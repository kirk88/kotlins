package com.easy.kotlins.helper

import android.graphics.Color
import android.text.Editable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.annotation.ColorInt

/**
 * Create by LiZhanPing on 2020/8/27
 */

fun String?.asEditable(): Editable? {
    return this?.let { Editable.Factory.getInstance().newEditable(it) }
}

fun String?.asSpannableBuilder(): SpannableStringBuilder {
    return SpannableStringBuilder(this ?: "")
}

fun String.heightLight(keywords: String?, color: Int = Color.RED): CharSequence {
    return if (keywords.isNullOrBlank()) this else keywords.let {
        SpannableString(this).also {
            var result = keywords.toRegex().find(it)
            while (result != null) {
                it.setSpan(ForegroundColorSpan(color), result.range.first, result.range.last + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                result = result.next()
            }
        }
    }
}

fun String.heightLight(start: Int = 0, end: Int = length, @ColorInt color: Int = Color.RED): CharSequence? {
    return SpannableString(this).apply {
        setSpan(ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}

fun String?.notNull(def: String = ""): String = if (this == null || this == "null") def else this

fun String?.notNullOrEmpty(def: String = ""): String = if (this.isNullOrEmpty()) def else this

fun String?.notNullOrBlank(def: String = ""): String = if (this.isNullOrBlank()) def else this

fun String.notEmpty(def: String = ""): String = if (this.isEmpty()) def else this

fun String.notBlank(def: String = ""): String = if (this.isBlank()) def else this

fun CharSequence.splitNotBlank(vararg delimiters: String, limit: Int = 0): List<String> {
    return split(delimiters = delimiters, limit = limit).filter { it.isNotBlank() }
}

fun <K, V> Map<K, V>.joinKeysToString(separator: CharSequence = ", "): String {
    return keys.joinToString(separator)
}

fun <K, V> Map<K, V>.joinValuesToString(separator: CharSequence = ", "): String {
    return values.joinToString(separator)
}

fun <K, V, A : Appendable> Map<K, V>.joinKeysTo(buffer: A, separator: CharSequence = ", "): A {
    return keys.joinTo(buffer, separator)
}

fun <K, V, A : Appendable> Map<K, V>.joinValuesTo(buffer: A, separator: CharSequence = ", "): A {
    return values.joinTo(buffer, separator)
}
