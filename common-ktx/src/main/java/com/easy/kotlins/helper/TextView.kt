package com.easy.kotlins.helper

import android.widget.TextView
import kotlin.DeprecationLevel.ERROR

/**
 * Create by LiZhanPing on 2020/8/28
 */

var TextView?.string: String?
    get() = this?.text?.toString()
    set(value) {
        this?.text = value.asEditable()
    }

var TextView?.textResource: Int
    @Deprecated(message = "NO_GETTER", level = ERROR) get() = error("no getter")
    set(value) {
        this?.setText(value)
    }

var TextView?.textColor: Int
    get() = this?.textColors?.defaultColor ?: 0
    set(value) {
        this?.setTextColor(value)
    }

fun TextView.isEmpty(): Boolean = this.text?.isEmpty() ?: true

fun TextView.isBlank(): Boolean = this.text?.isBlank() ?: true

fun TextView?.isNullOrEmpty(): Boolean = this?.text?.isEmpty() ?: true

fun TextView?.isNullOrBlank(): Boolean = this?.text?.isBlank() ?: true

fun TextView.isNotEmpty(): Boolean = this.text?.isNotEmpty() ?: false

fun TextView.isNotBlank(): Boolean = this.text?.isNotBlank() ?: false

fun TextView?.setBoldTextStyle(bold: Boolean = true) = run { this?.paint?.isFakeBoldText = bold }