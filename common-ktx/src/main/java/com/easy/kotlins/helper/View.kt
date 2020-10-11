package com.easy.kotlins.helper

import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.CompoundButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.viewpager.widget.ViewPager

/**
 * Create by LiZhanPing on 2020/8/24
 */

@Deprecated(message = "use isVisible replaced", replaceWith = ReplaceWith("isVisible"))
fun View?.visible(visible: Boolean = true) {
    this?.run { visibility = if (visible) View.VISIBLE else View.INVISIBLE }
}

@Deprecated(message = "use isGone replaced", replaceWith = ReplaceWith("isGone"))
fun View?.gone(gone: Boolean = true) {
    this?.run { visibility = if (gone) View.GONE else View.VISIBLE }
}

var View?.isVisible: Boolean
    get() = this?.visibility == View.VISIBLE
    set(value) {
        this?.visibility = if (value) View.VISIBLE else View.INVISIBLE
    }

var View?.isInvisible
    get() = this?.visibility == View.INVISIBLE
    set(value) {
        this?.visibility = if (value) View.INVISIBLE else View.VISIBLE
    }

var View?.isGone
    get() = this?.visibility == View.GONE
    set(value) {
        this?.visibility = if (value) View.GONE else View.VISIBLE
    }

var View?.backgroundColor: Int
    get() = if (this?.background is ColorDrawable) (background as ColorDrawable).color else 0
    set(value) {
        this?.setBackgroundColor(value)
    }

var View?.backgroundResources: Int
    @Deprecated("NO_GETTER", level = DeprecationLevel.ERROR) get() = error("no getter")
    set(value) {
        this?.setBackgroundResource(value)
    }

var View.startPadding: Int
    get() = paddingStart
    set(value) {
        setPaddingRelative(value, paddingTop, paddingEnd, paddingBottom)
    }

var View.endPadding: Int
    get() = paddingEnd
    set(value) {
        setPaddingRelative(paddingStart, paddingTop, value, paddingBottom)
    }

var View.topPadding: Int
    get() = paddingTop
    set(value) {
        setPaddingRelative(paddingStart, value, paddingEnd, paddingBottom)
    }

var View.bottomPadding: Int
    get() = paddingBottom
    set(value) {
        setPaddingRelative(paddingStart, paddingTop, paddingEnd, value)
    }

var View.padding: Int
    @Deprecated("NO_GETTER", level = DeprecationLevel.ERROR) get() = error("no getter")
    set(value) {
        setPaddingRelative(value, value, value, value)
    }

var View.startMargin: Int
    get() = (layoutParams as? ViewGroup.MarginLayoutParams)?.marginStart ?: 0
    set(value) {
        (layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
            marginStart = value
            requestLayout()
        }
    }

var View.endMargin: Int
    get() = (layoutParams as? ViewGroup.MarginLayoutParams)?.marginEnd ?: 0
    set(value) {
        (layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
            marginEnd = value
            requestLayout()
        }
    }

var View.topMargin: Int
    get() = (layoutParams as? ViewGroup.MarginLayoutParams)?.topMargin ?: 0
    set(value) {
        (layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
            topMargin = value
            requestLayout()
        }
    }

var View.bottomMargin: Int
    get() = (layoutParams as? ViewGroup.MarginLayoutParams)?.bottomMargin ?: 0
    set(value) {
        (layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
            bottomMargin = value
            requestLayout()
        }
    }

var View.margin: Int
    @Deprecated("NO_GETTER", level = DeprecationLevel.ERROR) get() = error("no getter")
    set(value) {
        (layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
            leftMargin = value
            topMargin = value
            rightMargin = value
            bottomMargin = value
            requestLayout()
        }
    }

var View.layoutWidth: Int
    get() = width
    set(value) {
        layoutParams?.apply {
            width = value
            requestLayout()
        }
    }

var View.layoutHeight: Int
    get() = height
    set(value) {
        layoutParams?.apply {
            height = value
            requestLayout()
        }
    }

operator fun <T : View> View.get(@IdRes id: Int): T = findViewById<T?>(id)
        ?: error("can not find view by id: $id")

inline fun View?.onClick(crossinline action: (view: View) -> Unit) {
    this?.setOnClickListener { v -> action(v) }
}

inline fun View?.onLongClick(crossinline action: (view: View) -> Boolean) {
    this?.setOnLongClickListener { v -> action(v) }
}

inline fun Iterable<View>?.onClick(crossinline action: (view: View) -> Unit) {
    val clickListener: View.OnClickListener = View.OnClickListener { v -> action(v) }
    this?.forEach {
        it.setOnClickListener(clickListener)
    }
}

inline fun Iterable<View>?.onLongClick(crossinline action: (view: View) -> Boolean) {
    val longClickListener: View.OnLongClickListener = View.OnLongClickListener { v -> action(v) }
    this?.forEach {
        it.setOnLongClickListener(longClickListener)
    }
}

@Suppress("ClickableViewAccessibility")
inline fun View?.onTouch(crossinline action: (view: View, event: MotionEvent) -> Boolean) {
    this?.setOnTouchListener { v, event -> action(v, event) }
}

inline fun TextView?.onTextChanged(crossinline action: (text: CharSequence?) -> Unit) {
    this?.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            action(s)
        }

        override fun afterTextChanged(s: Editable?) {
        }

    })
}

inline fun TextView?.afterTextChanged(crossinline action: (text: Editable?) -> Unit) {
    this?.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            action(s)
        }

    })
}

inline fun TextView?.onEditorActionEvent(crossinline action: (view: TextView, actionId: Int, event: KeyEvent?) -> Unit) {
    this?.setOnEditorActionListener { v, actionId, event ->
        action(v, actionId, event)
        true
    }
}

inline fun CompoundButton?.onCheckedChanged(crossinline action: (buttonView: CompoundButton, isChecked: Boolean) -> Unit) {
    this?.setOnCheckedChangeListener { buttonView, isChecked -> action(buttonView, isChecked) }
}

inline fun RadioGroup?.onCheckedChanged(crossinline action: (group: RadioGroup, checkedId: Int) -> Unit) {
    this?.setOnCheckedChangeListener { group, checkedId -> action(group, checkedId) }
}


inline fun MenuItem?.onMenuItemClick(crossinline action: (item: MenuItem) -> Unit) {
    this?.setOnMenuItemClickListener { item ->
        action(item)
        true
    }
}

inline fun ViewGroup?.forEachChild(action: (view: View) -> Unit) = this?.run {
    for (index in 0 until childCount) {
        action(getChildAt(index))
    }
}

inline fun ViewGroup?.forEachChildIndexed(action: (index: Int, view: View) -> Unit) = this?.run {
    for (index in 0 until childCount) {
        action(index, getChildAt(index))
    }
}

inline fun ViewPager?.onPageSelected(crossinline action: (position: Int) -> Unit) {
    this?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        }

        override fun onPageSelected(position: Int) {
            action(position)
        }

        override fun onPageScrollStateChanged(position: Int) {
        }

    })
}