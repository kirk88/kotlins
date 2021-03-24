@file:Suppress("unused")

package com.easy.kotlins.helper

import android.util.TypedValue
import android.widget.TextView
import androidx.core.content.ContextCompat
import kotlin.DeprecationLevel.ERROR

var TextView.string: String?
    get() = text?.toString()
    set(value) {
        text = value?.asEditable()
    }

var TextView.textResource: Int
    @Deprecated("NO_GETTER", level = ERROR) get() = error("No getter")
    set(value) {
        setText(value)
    }

var TextView.textColor: Int
    get() = textColors?.defaultColor ?: 0
    set(value) {
        setTextColor(value)
    }

var TextView.textColorResource: Int
    @Deprecated("NO_GETTER", level = ERROR) get() = error("No getter")
    set(value) {
        setTextColor(ContextCompat.getColorStateList(context, value))
    }

var TextView.textPixelSize: Int
    @Deprecated("NO_GETTER", level = ERROR) get() = error("No getter")
    set(value) {
        setTextSize(TypedValue.COMPLEX_UNIT_PX, value.toFloat())
    }

var TextView.textSizeResource: Int
    @Deprecated("NO_GETTER", level = ERROR) get() = error("No getter")
    set(value) {
        setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(value).toFloat())
    }

fun TextView.isEmpty(): Boolean = this.text?.isEmpty() ?: true

fun TextView.isBlank(): Boolean = this.text?.isBlank() ?: true

fun TextView.isNullOrEmpty(): Boolean = this.text.isNullOrEmpty()

fun TextView.isNullOrBlank(): Boolean = this.text.isNullOrBlank()

fun TextView.isNotEmpty(): Boolean = this.text?.isNotEmpty() ?: false

fun TextView.isNotBlank(): Boolean = this.text?.isNotBlank() ?: false

fun TextView.setBoldTextStyle(bold: Boolean = true) = run { this.paint?.isFakeBoldText = bold }