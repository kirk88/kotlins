@file:Suppress("UNUSED")

package com.nice.common.helper

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.annotation.Px
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.nice.common.external.NO_GETTER
import com.nice.common.external.NO_GETTER_MESSAGE

var View.backgroundColor: Int
    get() = if (background is ColorDrawable) (background as ColorDrawable).color else 0
    set(value) {
        setBackgroundColor(value)
    }

var View.backgroundResource: Int
    @Deprecated(NO_GETTER_MESSAGE, level = DeprecationLevel.ERROR) get() = NO_GETTER
    set(value) {
        setBackgroundResource(value)
    }

var View.layoutWidth: Int
    get() = width
    set(value) {
        layoutParams?.width = value
        requestLayout()
    }

var View.layoutHeight: Int
    get() = height
    set(value) {
        layoutParams?.height = value
        requestLayout()
    }

val View.layoutInflater: LayoutInflater
    get() = LayoutInflater.from(context)

val View.windowInsetsControllerCompat: WindowInsetsControllerCompat?
    get() = ViewCompat.getWindowInsetsController(this)

fun View.setPaddingRelative(@Px size: Int) {
    setPaddingRelative(size, size, size, size)
}

inline fun <T : View> T.doOnClick(crossinline action: (view: T) -> Unit) {
    @Suppress("UNCHECKED_CAST")
    setOnClickListener { view -> action(view as T) }
}

inline fun <T : View> T.doOnLongClick(crossinline action: (view: T) -> Boolean) {
    @Suppress("UNCHECKED_CAST")
    setOnLongClickListener { view -> action(view as T) }
}

inline fun <T : View> T.doOnTouch(crossinline action: (view: T, event: MotionEvent) -> Boolean) {
    @Suppress("ClickableViewAccessibility", "UNCHECKED_CAST")
    setOnTouchListener { view, event -> action(view as T, event) }
}

fun View.visible(animate: Boolean = true) {
    if (animate) {
        alpha = 0.0f
        animate().apply {
            cancel()
            alpha(1.0f)
            setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    visibility = View.VISIBLE
                }
            })
            start()
        }
    } else {
        visibility = View.VISIBLE
    }
}

fun View.invisible(animate: Boolean = true) {
    if (animate) {
        alpha = 1.0f
        animate().apply {
            cancel()
            alpha(0.0f)
            setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    visibility = View.INVISIBLE
                }
            })
            start()
        }
    } else {
        visibility = View.INVISIBLE
    }
}

fun View.gone(animate: Boolean = true) {
    if (animate) {
        alpha = 1.0f
        animate().apply {
            cancel()
            alpha(0.0f)
            setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    visibility = View.GONE
                }
            })
            start()
        }
    } else {
        visibility = View.GONE
    }
}

fun View.showIme() {
    val shown = Runnable {
        windowInsetsControllerCompat?.show(WindowInsetsCompat.Type.ime())
    }
    if (Build.VERSION.SDK_INT >= 30) {
        requestFocus()
        post(shown)
    } else {
        shown.run()
    }
}

fun View.hideIme() {
    val hidden = Runnable {
        windowInsetsControllerCompat?.hide(WindowInsetsCompat.Type.ime())
    }
    if (Build.VERSION.SDK_INT >= 30) {
        requestFocus()
        post(hidden)
    } else {
        hidden.run()
    }
}