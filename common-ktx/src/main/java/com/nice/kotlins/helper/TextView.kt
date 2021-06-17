@file:Suppress("unused")

package com.nice.kotlins.helper

import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.KeyEvent
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.nice.kotlins.helper.Internals.NO_GETTER
import com.nice.kotlins.helper.Internals.NO_GETTER_MESSAGE
import kotlin.DeprecationLevel.ERROR

var TextView.string: String?
    get() = text?.toString()
    set(value) {
        text = value?.toEditable()
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

val TextView.drawableLeft: Drawable?
    get() = compoundDrawables[0]

val TextView.drawableStart: Drawable?
    get() = compoundDrawablesRelative[0]

val TextView.drawableTop: Drawable?
    get() = compoundDrawables[1]

val TextView.drawableRight: Drawable?
    get() = compoundDrawables[2]

val TextView.drawableEnd: Drawable?
    get() = compoundDrawablesRelative[2]

val TextView.drawableBottom: Drawable?
    get() = compoundDrawables[3]

fun TextView.updateCompoundDrawable(
    left: Drawable? = null,
    top: Drawable? = null,
    right: Drawable? = null,
    bottom: Drawable? = null,
) {
    val drawables = compoundDrawables
    setCompoundDrawables(
        left ?: drawables[0],
        top ?: drawables[1],
        right ?: drawables[2],
        bottom ?: drawables[3]
    )
}

fun TextView.updateCompoundDrawableRelative(
    start: Drawable? = null,
    top: Drawable? = null,
    end: Drawable? = null,
    bottom: Drawable? = null,
) {
    val drawables = compoundDrawablesRelative
    setCompoundDrawablesRelative(
        start ?: drawables[0],
        top ?: drawables[1],
        end ?: drawables[2],
        bottom ?: drawables[3]
    )
}

fun TextView.updateCompoundDrawableWithIntrinsicBounds(
    left: Drawable? = null,
    top: Drawable? = null,
    right: Drawable? = null,
    bottom: Drawable? = null,
) {
    val drawables = compoundDrawables
    setCompoundDrawablesWithIntrinsicBounds(
        left ?: drawables[0],
        top ?: drawables[1],
        right ?: drawables[2],
        bottom ?: drawables[3]
    )
}

fun TextView.updateCompoundDrawableRelativeWithIntrinsicBounds(
    start: Drawable? = null,
    top: Drawable? = null,
    end: Drawable? = null,
    bottom: Drawable? = null,
) {
    val drawables = compoundDrawablesRelative
    setCompoundDrawablesRelativeWithIntrinsicBounds(
        start ?: drawables[0],
        top ?: drawables[1],
        end ?: drawables[2],
        bottom ?: drawables[3]
    )
}

fun TextView.isEmpty(): Boolean = text.isNullOrEmpty()

fun TextView.isBlank(): Boolean = text.isNullOrBlank()

fun TextView.isNotEmpty(): Boolean = !isEmpty()

fun TextView.isNotBlank(): Boolean = !isBlank()

inline fun TextView.doOnEditorAction(crossinline action: (view: TextView, actionId: Int, event: KeyEvent?) -> Boolean) {
    setOnEditorActionListener { v, actionId, event ->
        action(v, actionId, event)
    }
}
