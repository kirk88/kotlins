@file:Suppress("unused")

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
import com.google.android.material.tabs.TabLayout

/**
 * Create by LiZhanPing on 2020/8/24
 */

var View.isVisible: Boolean
    get() = visibility == View.VISIBLE
    set(value) {
        visibility = if (value) View.VISIBLE else View.INVISIBLE
    }

var View.isInvisible
    get() = visibility == View.INVISIBLE
    set(value) {
        visibility = if (value) View.INVISIBLE else View.VISIBLE
    }

var View.isGone
    get() = visibility == View.GONE
    set(value) {
        visibility = if (value) View.GONE else View.VISIBLE
    }

var View.backgroundColor: Int
    get() = if (background is ColorDrawable) (background as ColorDrawable).color else 0
    set(value) {
        setBackgroundColor(value)
    }

var View.backgroundResource: Int
    @Deprecated("NO_GETTER", level = DeprecationLevel.ERROR) get() = error("no getter")
    set(value) {
        setBackgroundResource(value)
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

var View.horizontalPadding: Int
    @Deprecated("NO_GETTER", level = DeprecationLevel.ERROR) get() = error("no getter")
    set(value) {
        setPaddingRelative(value, paddingTop, value, paddingBottom)
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

var View.verticalPadding: Int
    @Deprecated("NO_GETTER", level = DeprecationLevel.ERROR) get() = error("no getter")
    set(value) {
        setPaddingRelative(paddingStart, value, paddingEnd, value)
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
            leftMargin = value
            requestLayout()
        }
    }

var View.endMargin: Int
    get() = (layoutParams as? ViewGroup.MarginLayoutParams)?.marginEnd ?: 0
    set(value) {
        (layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
            rightMargin = value
            requestLayout()
        }
    }

var View.horizontalMargin: Int
    @Deprecated("NO_GETTER", level = DeprecationLevel.ERROR) get() = error("no getter")
    set(value) {
        (layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
            leftMargin = value
            rightMargin = value
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

var View.verticalMargin: Int
    @Deprecated("NO_GETTER", level = DeprecationLevel.ERROR) get() = error("no getter")
    set(value) {
        (layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
            topMargin = value
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
        layoutParams?.width = value
        requestLayout()
    }

var View.layoutHeight: Int
    get() = height
    set(value) {
        layoutParams?.height = value
        requestLayout()
    }

operator fun <T : View> View.get(@IdRes id: Int): T = findViewById<T>(id)
    ?: error("can not find view by id: $id")

inline fun <T : View> T.onClick(crossinline action: (view: T) -> Unit) {
    @Suppress("UNCHECKED_CAST")
    setOnClickListener { v -> action(v as T) }
}

inline fun <T : View> T.onLongClick(crossinline action: (view: T) -> Boolean) {
    @Suppress("UNCHECKED_CAST")
    setOnLongClickListener { v -> action(v as T) }
}

inline fun <T: View> View.onTouch(crossinline action: (view: T, event: MotionEvent) -> Boolean) {
    @Suppress("ClickableViewAccessibility", "UNCHECKED_CAST")
    setOnTouchListener { v, event -> action(v as T, event) }
}

inline fun TextView.onTextChanged(crossinline action: (text: CharSequence?) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            action(s)
        }

        override fun afterTextChanged(s: Editable?) {
        }

    })
}

inline fun TextView.afterTextChanged(crossinline action: (text: Editable?) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            action(s)
        }

    })
}

inline fun TextView.onEditorActionEvent(crossinline action: (view: TextView, actionId: Int, event: KeyEvent?) -> Unit) {
    setOnEditorActionListener { v, actionId, event ->
        action(v, actionId, event)
        true
    }
}

inline fun CompoundButton.onCheckedChanged(crossinline action: (buttonView: CompoundButton, isChecked: Boolean) -> Unit) {
    setOnCheckedChangeListener { buttonView, isChecked -> action(buttonView, isChecked) }
}

inline fun RadioGroup.onCheckedChanged(crossinline action: (group: RadioGroup, checkedId: Int) -> Unit) {
    setOnCheckedChangeListener { group, checkedId -> action(group, checkedId) }
}


inline fun MenuItem.onMenuItemClick(crossinline action: (item: MenuItem) -> Unit) {
    setOnMenuItemClickListener { item ->
        action(item)
        true
    }
}

inline fun ViewGroup.forEachChild(action: (view: View) -> Unit) = run {
    for (index in 0 until childCount) {
        action(getChildAt(index))
    }
}

inline fun ViewGroup.forEachChildIndexed(action: (index: Int, view: View) -> Unit) = run {
    for (index in 0 until childCount) {
        action(index, getChildAt(index))
    }
}

inline fun ViewPager.onPageSelected(crossinline action: (position: Int) -> Unit) {
    addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
        }

        override fun onPageSelected(position: Int) {
            action(position)
        }

        override fun onPageScrollStateChanged(position: Int) {
        }

    })
}

inline fun TabLayout.onTabSelectedChanged(crossinline action: (tab: TabLayout.Tab) -> Unit) {
    addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            tab?.let { action(it) }
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
            tab?.let { action(it) }
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {
        }
    })
}