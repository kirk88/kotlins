@file:Suppress("unused")

package com.nice.kotlins.helper

import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.nice.kotlins.helper.Internals.NO_GETTER
import com.nice.kotlins.helper.Internals.NO_GETTER_MESSAGE
import kotlin.DeprecationLevel.ERROR

var TextView.string: String?
    get() = text?.toString()
    set(value) {
        text = value?.asEditable()
    }

var TextView.textResource: Int
    @Deprecated(NO_GETTER_MESSAGE, level = ERROR) get() = NO_GETTER
    set(value) {
        setText(value)
    }

var TextView.textColor: Int
    get() = textColors?.defaultColor ?: 0
    set(value) {
        setTextColor(value)
    }

var TextView.textColorResource: Int
    @Deprecated(NO_GETTER_MESSAGE, level = ERROR) get() = NO_GETTER
    set(value) {
        setTextColor(ContextCompat.getColorStateList(context, value))
    }

var TextView.textPixelSize: Int
    @Deprecated(NO_GETTER_MESSAGE, level = ERROR) get() = NO_GETTER
    set(value) {
        setTextSize(TypedValue.COMPLEX_UNIT_PX, value.toFloat())
    }

var TextView.textSizeResource: Int
    @Deprecated(NO_GETTER_MESSAGE, level = ERROR) get() = NO_GETTER
    set(value) {
        setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(value).toFloat())
    }

var TextView.isBoldTextStyle: Boolean
    @Deprecated(NO_GETTER_MESSAGE, level = ERROR) get() = NO_GETTER
    set(value) {
        this.paint?.isFakeBoldText = value
    }

var TextView.drawableLeft: Drawable?
    get() = compoundDrawables[0]
    set(value) {
        val drawables = compoundDrawables
        setCompoundDrawables(value, drawables[1], drawables[2], drawables[3])
    }

var TextView.drawableStart: Drawable?
    get() = compoundDrawablesRelative[0]
    set(value) {
        val drawables = compoundDrawablesRelative
        setCompoundDrawablesRelative(value, drawables[1], drawables[2], drawables[3])
    }

var TextView.drawableTop: Drawable?
    get() = compoundDrawables[1]
    set(value) {
        val drawables = compoundDrawables
        setCompoundDrawables(drawables[0], value, drawables[2], drawables[3])
    }

var TextView.drawableRight: Drawable?
    get() = compoundDrawables[2]
    set(value) {
        val drawables = compoundDrawables
        setCompoundDrawables(drawables[0], drawables[1], value, drawables[3])
    }

var TextView.drawableEnd: Drawable?
    get() = compoundDrawablesRelative[2]
    set(value) {
        val drawables = compoundDrawablesRelative
        setCompoundDrawablesRelative(drawables[0], drawables[1], value, drawables[3])
    }

var TextView.drawableBottom: Drawable?
    get() = compoundDrawables[3]
    set(value) {
        val drawables = compoundDrawables
        setCompoundDrawables(drawables[0], drawables[1], drawables[2], value)
    }

fun TextView.updateDrawable(
    left: Drawable? = null,
    top: Drawable? = null,
    right: Drawable? = null,
    bottom: Drawable? = null
) {
    val drawables = compoundDrawables
    setCompoundDrawables(
        left ?: drawables[0],
        top ?: drawables[1],
        right ?: drawables[2],
        bottom ?: drawables[3]
    )
}

fun TextView.updateDrawableRelative(
    start: Drawable? = null,
    top: Drawable? = null,
    end: Drawable? = null,
    bottom: Drawable? = null
) {
    val drawables = compoundDrawablesRelative
    setCompoundDrawablesRelative(
        start ?: drawables[0],
        top ?: drawables[1],
        end ?: drawables[2],
        bottom ?: drawables[3]
    )
}

fun TextView.isEmpty(): Boolean = this.text?.isEmpty() ?: true

fun TextView.isBlank(): Boolean = this.text?.isBlank() ?: true

fun TextView.isNullOrEmpty(): Boolean = this.text.isNullOrEmpty()

fun TextView.isNullOrBlank(): Boolean = this.text.isNullOrBlank()

fun TextView.isNotEmpty(): Boolean = this.text?.isNotEmpty() ?: false

fun TextView.isNotBlank(): Boolean = this.text?.isNotBlank() ?: false