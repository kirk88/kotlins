@file:Suppress("unused")

package com.easy.kotlins.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.annotation.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.contains
import androidx.core.widget.TextViewCompat
import com.easy.kotlins.R
import com.easy.kotlins.helper.activity
import com.easy.kotlins.helper.appCompatActivity
import com.google.android.material.appbar.AppBarLayout

class TitleBar(context: Context, attrs: AttributeSet?) : AppBarLayout(context, attrs) {
    @IntDef(value = [SHOW_BOTTOM_DIVIDER_IF_NEED, SHOW_BOTTOM_DIVIDER_ALWAYS, SHOW_BOTTOM_DIVIDER_NEVER])
    @Retention(AnnotationRetention.SOURCE)
    annotation class BottomDividerMode

    private val useCustomTitle: Boolean

    private var showBottomDivider: Int
    private val bottomDividerHeight: Int
    private val bottomDividerColor: Int
    private val bottomDividerDrawable: ColorDrawable by lazy { ColorDrawable() }

    private val toolbar: Toolbar = Toolbar(context)
    private var titleTextView: TextView? = null
    private var subtitleTextView: TextView? = null

    private var actionBar: ActionBar? = null

    private var titleClickListener: OnClickListener? = null
    private var subtitleClickListener: OnClickListener? = null

    var popupTheme: Int
        get() = toolbar.popupTheme
        set(theme) {
            toolbar.popupTheme = theme
        }

    var navigationIcon: Drawable?
        get() = toolbar.navigationIcon
        set(drawable) {
            tintDrawable(drawable, navigationIconTint, navigationIconTintMode)

            toolbar.navigationIcon = drawable
        }

    var navigationIconTint: ColorStateList? = null
        set(tint) {
            tintDrawable(toolbar.navigationIcon, tint, navigationIconTintMode)

            field = tint
        }

    var navigationIconTintMode: PorterDuff.Mode = PorterDuff.Mode.SRC_ATOP
        set(mode) {
            tintDrawable(toolbar.navigationIcon, navigationIconTint, mode)

            field = mode
        }

    var navigationContentDescription: CharSequence?
        get() = toolbar.navigationContentDescription
        set(navigationContentDescription) {
            toolbar.navigationContentDescription = navigationContentDescription
        }

    var title: CharSequence? = null
        set(title) {
            when {
                useCustomTitle -> {
                    ensureTitleTextView()
                    titleTextView!!.text = title
                }
                actionBar != null -> actionBar!!.title = title
                else -> toolbar.title = title
            }

            tryGetTitleTextView(toolbar) {
                if (hasOnClickListeners()) return@tryGetTitleTextView
                setOnClickListener(titleClickListener)
            }

            field = title
        }

    var subtitle: CharSequence? = null
        set(subtitle) {
            if (actionBar != null) actionBar!!.subtitle = subtitle
            else toolbar.subtitle = subtitle

            tryGetSubtitleTextView(toolbar) {
                if (hasOnClickListeners()) return@tryGetSubtitleTextView
                setOnClickListener(subtitleClickListener)
            }

            field = subtitle
        }


    val menu: Menu
        get() = toolbar.menu

    fun setTitle(@StringRes id: Int) {
        title = context.getText(id)
    }

    fun setSubtitle(@StringRes id: Int) {
        subtitle = resources.getText(id)
    }

    fun setTitleTextColor(@ColorInt color: Int) {
        if (useCustomTitle) {
            ensureTitleTextView()
            titleTextView!!.setTextColor(color)
        } else {
            toolbar.setTitleTextColor(color)
        }
    }

    fun setTitleTextAppearance(@StyleRes id: Int) {
        if (useCustomTitle) {
            ensureTitleTextView()
            TextViewCompat.setTextAppearance(titleTextView!!, id)
        } else {
            toolbar.setTitleTextAppearance(context, id)
        }
    }

    fun setSubtitleTextColor(@ColorInt color: Int) {
        toolbar.setSubtitleTextColor(color)
    }

    fun setSubtitleTextAppearance(@StyleRes id: Int) {
        toolbar.setSubtitleTextAppearance(context, id)
    }

    fun setNavigationIcon(@DrawableRes id: Int) {
        navigationIcon = ContextCompat.getDrawable(context, id)
    }

    fun setNavigationContentDescription(@StringRes id: Int) {
        navigationContentDescription = context.getText(id)
    }

    fun setNavigationOnClickListener(clickListener: OnClickListener?) {
        toolbar.setNavigationOnClickListener(clickListener)
    }

    fun inflateMenu(@MenuRes id: Int): Menu {
        toolbar.inflateMenu(id)
        return toolbar.menu
    }

    fun addMenu(
        groupId: Int = Menu.NONE,
        itemId: Int = Menu.NONE,
        order: Int = Menu.NONE,
        title: CharSequence
    ): MenuItem {
        return toolbar.menu.add(groupId, itemId, order, title)
    }

    fun addMenu(
        groupId: Int = Menu.NONE,
        itemId: Int = Menu.NONE,
        order: Int = Menu.NONE,
        @StringRes titleId: Int
    ): MenuItem {
        return toolbar.menu.add(groupId, itemId, order, titleId)
    }

    fun removeAllMenus() {
        toolbar.menu.clear()
    }

    fun findMenuItem(@IdRes menuId: Int): MenuItem {
        return menu.findItem(menuId)
    }

    fun getMenuItem(index: Int): MenuItem {
        return menu.getItem(index)
    }

    fun setOnMenuItemClickListener(listener: Toolbar.OnMenuItemClickListener?) {
        toolbar.setOnMenuItemClickListener(listener)
    }

    fun setOnTitleClickListener(listener: OnClickListener?) {
        titleClickListener = listener
        tryGetTitleTextView(toolbar) {
            setOnClickListener(listener)
        }
    }

    fun setOnSubtitleClickListener(listener: OnClickListener?) {
        subtitleClickListener = listener
        tryGetSubtitleTextView(toolbar) {
            setOnClickListener(listener)
        }
    }


    fun setDisplayCustomTitleEnabled(enabled: Boolean) {
        if (!useCustomTitle) {
            return
        }

        if (enabled) {
            ensureTitleTextView()

            if (!isToolbarChild(titleTextView)) {
                toolbar.addView(titleTextView)
            }
        } else if (isToolbarChild(titleTextView)) {
            toolbar.removeView(titleTextView)
        }
    }

    fun setShowBottomDivider(@BottomDividerMode showDivider: Int) {
        if (showBottomDivider != showDivider) {
            showBottomDivider = showDivider
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (isShowBottomDivider()) {
            bottomDividerDrawable.apply {
                color = bottomDividerColor
                setBounds(0, height - bottomDividerHeight, width, height)
            }.draw(canvas)
        }
    }

    @SuppressLint("PrivateResource")
    private fun ensureTitleTextView() {
        if (titleTextView == null) {
            val textView = AppCompatTextView(context).also {
                titleTextView = it
            }
            TextViewCompat.setTextAppearance(
                textView,
                R.style.TextAppearance_Widget_AppCompat_Toolbar_Title
            )
            textView.setSingleLine()
            textView.ellipsize = TextUtils.TruncateAt.END
            val layoutParams = Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT
            )
            layoutParams.gravity = Gravity.CENTER
            textView.layoutParams = layoutParams
            toolbar.addView(titleTextView)
        }
    }

    private fun isToolbarChild(view: TextView?): Boolean {
        return view != null && toolbar.contains(view)
    }

    private fun isShowBottomDivider(): Boolean {
        return (showBottomDivider == SHOW_BOTTOM_DIVIDER_ALWAYS
                || showBottomDivider == SHOW_BOTTOM_DIVIDER_IF_NEED && Build.VERSION.SDK_INT < 21)
    }

    private fun tryGetTitleTextView(parent: View, block: TextView.() -> Unit) {
        if (titleTextView != null) {
            titleTextView!!.block()
            return
        }

        val field = parent.javaClass.getDeclaredField("mTitleTextView")
        field.isAccessible = true
        try {
            titleTextView = field.get(parent) as TextView?
            titleTextView?.block()
        } catch (_: IllegalArgumentException) {
        } catch (_: IllegalAccessException) {
        }
    }

    private fun tryGetSubtitleTextView(parent: View, block: TextView.() -> Unit) {
        if (subtitleTextView != null) {
            subtitleTextView!!.block()
            return
        }

        val field = parent.javaClass.getDeclaredField("mSubtitleTextView")
        field.isAccessible = true
        try {
            subtitleTextView = field.get(parent) as TextView?
            subtitleTextView?.block()
        } catch (_: IllegalArgumentException) {
        } catch (_: IllegalAccessException) {
        }
    }

    companion object {

        const val SHOW_BOTTOM_DIVIDER_IF_NEED = 0
        const val SHOW_BOTTOM_DIVIDER_ALWAYS = 1
        const val SHOW_BOTTOM_DIVIDER_NEVER = 2

        private fun getSupportActionBar(
            activity: AppCompatActivity,
            toolbar: Toolbar,
            showTitle: Boolean,
            showHome: Boolean,
            showHomeAsUp: Boolean
        ): ActionBar {
            activity.setSupportActionBar(toolbar)
            val actionBar = activity.supportActionBar!!
            actionBar.setDisplayShowHomeEnabled(showHome)
            actionBar.setDisplayHomeAsUpEnabled(showHomeAsUp)
            if (!showTitle) actionBar.title = null
            return actionBar
        }

        private fun tintDrawable(
            drawable: Drawable?,
            tint: ColorStateList?,
            tintMode: PorterDuff.Mode
        ) {
            if (drawable == null) return
            DrawableCompat.wrap(drawable.mutate())
            DrawableCompat.setTintList(drawable, tint)
            DrawableCompat.setTintMode(drawable, tintMode)
        }

        private fun intToMode(value: Int): PorterDuff.Mode {
            return when (value) {
                0 -> PorterDuff.Mode.CLEAR
                1 -> PorterDuff.Mode.SRC
                2 -> PorterDuff.Mode.DST
                3 -> PorterDuff.Mode.SRC_OVER
                4 -> PorterDuff.Mode.DST_OVER
                5 -> PorterDuff.Mode.SRC_IN
                6 -> PorterDuff.Mode.DST_IN
                7 -> PorterDuff.Mode.SRC_OUT
                8 -> PorterDuff.Mode.DST_OUT
                9 -> PorterDuff.Mode.SRC_ATOP
                10 -> PorterDuff.Mode.DST_ATOP
                11 -> PorterDuff.Mode.XOR
                16 -> PorterDuff.Mode.DARKEN
                17 -> PorterDuff.Mode.LIGHTEN
                13 -> PorterDuff.Mode.MULTIPLY
                14 -> PorterDuff.Mode.SCREEN
                12 -> PorterDuff.Mode.ADD
                15 -> PorterDuff.Mode.OVERLAY
                else -> PorterDuff.Mode.CLEAR
            }
        }

    }

    init {
        val ta = context.obtainStyledAttributes(
            attrs, R.styleable.TitleBar,
            R.attr.titleBarStyle, 0
        )

        useCustomTitle = ta.getBoolean(R.styleable.TitleBar_useCustomTitle, false)

        if (ta.getBoolean(R.styleable.TitleBar_attachToActivity, false)) {
            val showHome = ta.getBoolean(R.styleable.TitleBar_showHomeEnabled, false)
            val showHomeAsUp = ta.getBoolean(R.styleable.TitleBar_showHomeAsUpEnabled, false)
            actionBar = context.appCompatActivity?.let {
                getSupportActionBar(it, toolbar, !useCustomTitle, showHome, showHomeAsUp)
            }
        }

        val defaultTitle = { actionBar?.title ?: context.activity?.title }
        title = ta.getText(R.styleable.TitleBar_title) ?: defaultTitle()
        subtitle = ta.getText(R.styleable.TitleBar_subtitle)

        if (ta.hasValue(R.styleable.TitleBar_titleTextAppearance)) {
            setTitleTextAppearance(ta.getResourceId(R.styleable.TitleBar_titleTextAppearance, 0))
        }

        if (ta.hasValue(R.styleable.TitleBar_titleTextColor)) {
            setTitleTextColor(ta.getColor(R.styleable.TitleBar_titleTextColor, 0))
        }

        if (ta.hasValue(R.styleable.TitleBar_subtitleTextAppearance)) {
            setSubtitleTextAppearance(
                ta.getResourceId(R.styleable.TitleBar_subtitleTextAppearance, 0)
            )
        }

        if (ta.hasValue(R.styleable.TitleBar_subtitleTextColor)) {
            setSubtitleTextColor(ta.getColor(R.styleable.TitleBar_subtitleTextColor, 0))
        }

        navigationIconTint = ta.getColorStateList(R.styleable.TitleBar_navigationIconTint)
        navigationIconTintMode =
            intToMode(ta.getInt(R.styleable.TitleBar_navigationIconTintMode, 9))

        val resourceId = ta.getResourceId(R.styleable.TitleBar_navigationIcon, 0)
        if (resourceId != 0) {
            navigationIcon = AppCompatResources.getDrawable(context, resourceId)
        }
        navigationContentDescription = ta.getText(R.styleable.TitleBar_navigationContentDescription)

        showBottomDivider =
            ta.getInt(R.styleable.TitleBar_showBottomDivider, SHOW_BOTTOM_DIVIDER_IF_NEED)
        bottomDividerHeight = ta.getDimensionPixelSize(R.styleable.TitleBar_bottomDividerHeight, 1)
        bottomDividerColor = ta.getColor(R.styleable.TitleBar_bottomDividerColor, Color.GRAY)

        popupTheme = ta.getResourceId(R.styleable.TitleBar_popupTheme, 0)

        if (ta.hasValue(R.styleable.TitleBar_menu)) {
            inflateMenu(ta.getResourceId(R.styleable.TitleBar_menu, 0))
        }

        ta.recycle()

        addView(toolbar, -1, -2)
    }
}