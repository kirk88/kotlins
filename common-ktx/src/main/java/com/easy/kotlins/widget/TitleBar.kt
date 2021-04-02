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
import android.widget.TextView
import androidx.annotation.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.TextViewCompat
import com.easy.kotlins.R
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
    private var titleView: TextView? = null

    private var actionBar: ActionBar? = null

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
                    titleView!!.text = title
                }
                actionBar != null -> actionBar!!.title = title
                else -> toolbar.title = title
            }
            field = title
        }

    var subtitle: CharSequence? = null
        set(subtitle) {
            if (useCustomTitle) {
                return
            }
            if (actionBar != null) actionBar!!.subtitle = subtitle
            else toolbar.subtitle = subtitle
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
            titleView!!.setTextColor(color)
        } else {
            toolbar.setTitleTextColor(color)
        }
    }

    fun setTitleTextAppearance(@StyleRes id: Int) {
        if (useCustomTitle) {
            ensureTitleTextView()
            TextViewCompat.setTextAppearance(titleView!!, id)
        } else {
            toolbar.setTitleTextAppearance(context, id)
        }
    }

    fun setSubtitleTextColor(@ColorInt color: Int) {
        if (!useCustomTitle) {
            toolbar.setSubtitleTextColor(color)
        }
    }

    fun setSubtitleTextAppearance(@StyleRes id: Int) {
        if (!useCustomTitle) {
            toolbar.setSubtitleTextAppearance(context, id)
        }
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

    fun showCustomTitle() {
        if (!useCustomTitle) {
            return
        }

        if (titleView != null) {
            titleView!!.visibility = VISIBLE
        } else {
            ensureTitleTextView()
        }
    }

    fun hideCustomTitle() {
        if (!useCustomTitle) {
            return
        }

        titleView?.visibility = GONE
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
        if (titleView == null) {
            val textView = AppCompatTextView(context).also {
                titleView = it
            }
            TextViewCompat.setTextAppearance(
                textView,
                R.style.TextAppearance_Widget_AppCompat_Toolbar_Title
            )
            textView.gravity = Gravity.CENTER
            textView.maxEms = 20
            textView.maxLines = 1
            textView.ellipsize = TextUtils.TruncateAt.END
            val layoutParams = Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT
            )
            layoutParams.gravity = Gravity.CENTER
            toolbar.addView(titleView, layoutParams)
        }
    }

    private fun isShowBottomDivider(): Boolean {
        return (showBottomDivider == SHOW_BOTTOM_DIVIDER_ALWAYS
                || showBottomDivider == SHOW_BOTTOM_DIVIDER_IF_NEED && Build.VERSION.SDK_INT < 21)
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
            actionBar.setDisplayShowTitleEnabled(showTitle)
            actionBar.setDisplayShowHomeEnabled(showHome)
            actionBar.setDisplayHomeAsUpEnabled(showHomeAsUp)
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

        if (ta.getBoolean(R.styleable.TitleBar_attachToActivity, true)) {
            val showHome = ta.getBoolean(R.styleable.TitleBar_showHomeEnabled, true)
            val showHomeAsUp = ta.getBoolean(R.styleable.TitleBar_showHomeAsUpEnabled, true)
            actionBar = context.appCompatActivity?.let {
                getSupportActionBar(it, toolbar, !useCustomTitle, showHome, showHomeAsUp)
            }
        }

        title = ta.getString(R.styleable.TitleBar_title)
        subtitle = ta.getString(R.styleable.TitleBar_subtitle)

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