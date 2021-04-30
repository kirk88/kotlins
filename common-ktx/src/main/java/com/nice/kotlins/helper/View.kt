@file:Suppress("unused")

package com.nice.kotlins.helper

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.widget.CompoundButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.nice.kotlins.helper.Internals.NO_GETTER
import com.nice.kotlins.helper.Internals.NO_GETTER_MESSAGE

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
    @Deprecated(NO_GETTER_MESSAGE, level = DeprecationLevel.ERROR) get() = NO_GETTER
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
    @Deprecated(NO_GETTER_MESSAGE, level = DeprecationLevel.ERROR) get() = NO_GETTER
    set(value) {
        setPaddingRelative(paddingStart, value, paddingEnd, value)
    }

var View.padding: Int
    @Deprecated(NO_GETTER_MESSAGE, level = DeprecationLevel.ERROR) get() = NO_GETTER
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
    @Deprecated(NO_GETTER_MESSAGE, level = DeprecationLevel.ERROR) get() = NO_GETTER
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
    @Deprecated(NO_GETTER_MESSAGE, level = DeprecationLevel.ERROR) get() = NO_GETTER
    set(value) {
        (layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
            topMargin = value
            bottomMargin = value
            requestLayout()
        }
    }

var View.margin: Int
    @Deprecated(NO_GETTER_MESSAGE, level = DeprecationLevel.ERROR) get() = NO_GETTER
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

val View.layoutInflater: LayoutInflater
    get() = LayoutInflater.from(context)

inline fun <T : View> T.onClick(crossinline action: (view: T) -> Unit) {
    @Suppress("UNCHECKED_CAST")
    setOnClickListener { v -> action(v as T) }
}

inline fun <T : View> T.onLongClick(crossinline action: (view: T) -> Boolean) {
    @Suppress("UNCHECKED_CAST")
    setOnLongClickListener { v -> action(v as T) }
}

inline fun <T : View> View.onTouch(crossinline action: (view: T, event: MotionEvent) -> Boolean) {
    @Suppress("ClickableViewAccessibility", "UNCHECKED_CAST")
    setOnTouchListener { v, event -> action(v as T, event) }
}

inline fun TextView.onEditorActionEvent(crossinline action: (view: TextView, actionId: Int, event: KeyEvent?) -> Boolean) {
    setOnEditorActionListener { v, actionId, event ->
        action(v, actionId, event)
    }
}

inline fun CompoundButton.onCheckedChanged(crossinline action: (buttonView: CompoundButton, isChecked: Boolean) -> Unit) {
    setOnCheckedChangeListener { buttonView, isChecked -> action(buttonView, isChecked) }
}

inline fun RadioGroup.onCheckedChanged(crossinline action: (group: RadioGroup, checkedId: Int) -> Unit) {
    setOnCheckedChangeListener { group, checkedId -> action(group, checkedId) }
}


inline fun MenuItem.onMenuItemClick(crossinline action: (item: MenuItem) -> Boolean) {
    setOnMenuItemClickListener { item ->
        action(item)
    }
}

inline fun ViewPager.onPageScrolled(
    crossinline action: (
        position: Int,
        positionOffset: Float,
        positionOffsetPixels: Int,
    ) -> Unit,
) {
    addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int,
        ) {
            action(position, positionOffset, positionOffsetPixels)
        }

        override fun onPageSelected(position: Int) {
        }

        override fun onPageScrollStateChanged(position: Int) {
        }

    })
}

inline fun ViewPager.onPageSelected(crossinline action: (position: Int) -> Unit) {
    addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int,
        ) {
        }

        override fun onPageSelected(position: Int) {
            action(position)
        }

        override fun onPageScrollStateChanged(position: Int) {
        }

    })
}

inline fun ViewPager.onPageScrollStateChanged(crossinline action: (position: Int) -> Unit) {
    addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int,
        ) {
        }

        override fun onPageSelected(position: Int) {
        }

        override fun onPageScrollStateChanged(position: Int) {
            action(position)
        }

    })
}

inline fun ViewPager2.onPageScrolled(
    crossinline action: (
        position: Int,
        positionOffset: Float,
        positionOffsetPixels: Int,
    ) -> Unit,
) {
    registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int,
        ) {
            action(position, positionOffset, positionOffsetPixels)
        }

        override fun onPageSelected(position: Int) {
        }

        override fun onPageScrollStateChanged(position: Int) {
        }

    })
}

inline fun ViewPager2.onPageSelected(crossinline action: (position: Int) -> Unit) {
    registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int,
        ) {
        }

        override fun onPageSelected(position: Int) {
            action(position)
        }

        override fun onPageScrollStateChanged(position: Int) {
        }

    })
}

inline fun ViewPager2.onPageScrollStateChanged(crossinline action: (position: Int) -> Unit) {
    registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int,
        ) {
        }

        override fun onPageSelected(position: Int) {
        }

        override fun onPageScrollStateChanged(position: Int) {
            action(position)
        }

    })
}


inline fun TabLayout.onTabSelected(crossinline action: (tab: TabLayout.Tab) -> Unit) {
    addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            tab?.let { action(it) }
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {
        }
    })
}

inline fun TabLayout.onTabUnselected(crossinline action: (tab: TabLayout.Tab) -> Unit) {
    addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
            tab?.let { action(it) }
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {
        }
    })
}

inline fun TabLayout.onTabReselected(crossinline action: (tab: TabLayout.Tab) -> Unit) {
    addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {

        }

        override fun onTabReselected(tab: TabLayout.Tab?) {
            tab?.let { action(it) }
        }
    })
}

inline fun BottomNavigationView.onItemSelected(crossinline action: (item: MenuItem) -> Boolean) {
    setOnNavigationItemSelectedListener {
        action(it)
    }
}

inline fun BottomNavigationView.onItemReselected(crossinline action: (item: MenuItem) -> Unit) {
    setOnNavigationItemReselectedListener {
        action(it)
    }
}


fun View.visible(anim: Boolean = true) {
    visibility = View.VISIBLE
    if (anim) {
        alpha = 0.0f
        animate().apply {
            cancel()
            alpha(1.0f)
            setListener(null)
            start()
        }
    }
}

fun View.invisible(anim: Boolean = true) {
    alpha = 1.0f
    if (anim) {
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

fun View.gone(anim: Boolean = true) {
    alpha = 1.0f
    if (anim) {
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