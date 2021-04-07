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
import androidx.core.view.contains
import androidx.core.widget.TextViewCompat
import com.easy.kotlins.R
import com.easy.kotlins.helper.NO_GETTER
import com.easy.kotlins.helper.NO_GETTER_MESSAGE
import com.easy.kotlins.helper.appCompatActivity
import com.google.android.material.appbar.AppBarLayout

class TitleBar(context: Context, attrs: AttributeSet?) : AppBarLayout(context, attrs) {
    @IntDef(value = [SHOW_BOTTOM_DIVIDER_IF_NEED, SHOW_BOTTOM_DIVIDER_ALWAYS, SHOW_BOTTOM_DIVIDER_NEVER])
    @Retention(AnnotationRetention.SOURCE)
    annotation class BottomDividerMode

    private val displayShowCustomTitleEnabled: Boolean

    private var showBottomDivider: Int
    private val bottomDividerHeight: Int
    private val bottomDividerColor: Int
    private val bottomDividerDrawable: ColorDrawable by lazy { ColorDrawable() }

    private val toolbar: Toolbar
    private var titleTextView: TextView? = null
    private var subtitleTextView: TextView? = null

    private var actionBar: ActionBar? = null

    var popupTheme: Int
        get() = toolbar.popupTheme
        set(theme) {
            toolbar.popupTheme = theme
        }

    var navigationIcon: Drawable?
        get() = toolbar.navigationIcon
        set(drawable) {
            toolbar.navigationIcon =
                maybeTintDrawable(drawable, navigationIconTint, navigationIconTintMode)
        }

    var navigationIconTint: ColorStateList? = null
        set(tint) {
            maybeTintDrawable(toolbar.navigationIcon, tint, navigationIconTintMode)

            field = tint
        }

    var navigationIconTintMode: PorterDuff.Mode = PorterDuff.Mode.SRC_ATOP
        set(mode) {
            maybeTintDrawable(toolbar.navigationIcon, navigationIconTint, mode)

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
                displayShowCustomTitleEnabled -> {
                    if (!title.isNullOrEmpty()) {
                        ensureCustomTitleTextView()
                    } else if (isToolbarChild(titleTextView)) {
                        toolbar.removeView(titleTextView)
                    }
                    titleTextView?.text = title
                }
                actionBar != null -> actionBar!!.title = title
                else -> toolbar.title = title
            }

            field = title
        }

    var titleTextColor: ColorStateList? = null
        set(color) {
            if (color == null) {
                return
            }

            if (displayShowCustomTitleEnabled) {
                titleTextView?.setTextColor(color)
            } else {
                toolbar.setTitleTextColor(color)
            }

            field = color
        }

    var titleTextAppearance: Int
        @Deprecated(NO_GETTER_MESSAGE, level = DeprecationLevel.ERROR) get() = NO_GETTER
        set(id) {
            if (displayShowCustomTitleEnabled) {
                titleTextView?.let { TextViewCompat.setTextAppearance(it, id) }
            } else {
                toolbar.setTitleTextAppearance(context, id)
            }
        }

    var subtitle: CharSequence? = null
        set(subtitle) {
            if (actionBar != null) actionBar!!.subtitle = subtitle
            else toolbar.subtitle = subtitle

            field = subtitle
        }

    var subtitleTextColor: ColorStateList? = null
        set(color) {
            if (color == null) {
                return
            }

            toolbar.setSubtitleTextColor(color)

            field = color
        }

    var subtitleTextAppearance: Int
        @Deprecated(NO_GETTER_MESSAGE, level = DeprecationLevel.ERROR) get() = NO_GETTER
        set(id) {
            toolbar.setSubtitleTextAppearance(context, id)
        }

    fun setTitle(@StringRes id: Int) {
        title = context.getText(id)
    }

    fun setSubtitle(@StringRes id: Int) {
        subtitle = context.getText(id)
    }

    fun setTitleTextColor(@ColorInt color: Int) {
        titleTextColor = ColorStateList.valueOf(color)
    }

    fun setSubtitleTextColor(@ColorInt color: Int) {
        subtitleTextColor = ColorStateList.valueOf(color)
    }

    fun setNavigationIcon(@DrawableRes id: Int) {
        navigationIcon = ContextCompat.getDrawable(context, id)
    }

    fun setNavigationIconTint(@ColorInt color: Int) {
        navigationIconTint = ColorStateList.valueOf(color)
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
        title: CharSequence,
        groupId: Int = Menu.NONE,
        itemId: Int = Menu.NONE,
        order: Int = Menu.NONE
    ): MenuItem {
        return toolbar.menu.add(groupId, itemId, order, title)
    }

    fun addMenu(
        @StringRes titleId: Int,
        groupId: Int = Menu.NONE,
        itemId: Int = Menu.NONE,
        order: Int = Menu.NONE
    ): MenuItem {
        return toolbar.menu.add(groupId, itemId, order, titleId)
    }

    fun removeAllMenus() {
        toolbar.menu.clear()
    }

    fun findMenuItem(@IdRes menuId: Int): MenuItem {
        return toolbar.menu.findItem(menuId)
    }

    fun getMenuItem(index: Int): MenuItem {
        return toolbar.menu.getItem(index)
    }

    fun setOnMenuItemClickListener(listener: Toolbar.OnMenuItemClickListener?) {
        toolbar.setOnMenuItemClickListener(listener)
    }

    fun setOnTitleClickListener(listener: OnClickListener?) {
        tryGetTitleTextView {
            setOnClickListener(listener)
        }
    }

    fun setOnSubtitleClickListener(listener: OnClickListener?) {
        tryGetSubtitleTextView {
            setOnClickListener(listener)
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
    private fun ensureCustomTitleTextView() {
        if (titleTextView == null) {
            titleTextView = findViewById(R.id.custom_title)
        }

        if (titleTextView == null) {
            val textView = AppCompatTextView(context)
            textView.id = R.id.custom_title
            textView.setSingleLine()
            textView.ellipsize = TextUtils.TruncateAt.END
            TextViewCompat.setTextAppearance(
                textView,
                R.style.TextAppearance_Widget_AppCompat_Toolbar_Title
            )
            textView.layoutParams = Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
            toolbar.addView(textView)
            titleTextView = textView
        }
    }

    private fun isToolbarChild(view: TextView?): Boolean {
        return view != null && toolbar.contains(view)
    }

    private fun isShowBottomDivider(): Boolean {
        return (showBottomDivider == SHOW_BOTTOM_DIVIDER_ALWAYS
                || showBottomDivider == SHOW_BOTTOM_DIVIDER_IF_NEED && Build.VERSION.SDK_INT < 21)
    }

    private fun tryGetTitleTextView(block: TextView.() -> Unit) {
        if (titleTextView != null) {
            titleTextView!!.block()
            return
        }

        val field = toolbar.javaClass.getDeclaredField("mTitleTextView")
        field.isAccessible = true
        try {
            titleTextView = field.get(toolbar) as TextView?
            titleTextView?.block()
        } catch (_: IllegalArgumentException) {
        } catch (_: IllegalAccessException) {
        }
    }

    private fun tryGetSubtitleTextView(block: TextView.() -> Unit) {
        if (subtitleTextView != null) {
            subtitleTextView!!.block()
            return
        }

        val field = toolbar.javaClass.getDeclaredField("mSubtitleTextView")
        field.isAccessible = true
        try {
            subtitleTextView = field.get(toolbar) as TextView?
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

        private fun maybeTintDrawable(
            drawable: Drawable?,
            tint: ColorStateList?,
            tintMode: PorterDuff.Mode
        ): Drawable? {
            return if (drawable != null) {
                DrawableCompat.wrap(drawable.mutate()).also {
                    DrawableCompat.setTintList(it, tint)
                    DrawableCompat.setTintMode(it, tintMode)
                }
            } else {
                drawable
            }
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

        toolbar = if (ta.hasValue(R.styleable.TitleBar_customLayout)) {
            inflate(context, ta.getResourceId(R.styleable.TitleBar_customLayout, 0), this)
            requireNotNull(findViewById(R.id.custom_toolbar)) {
                "CustomLayout must have a Toolbar"
            }
        } else {
            Toolbar(context).also {
                addView(it, -1, -2)
            }
        }

        displayShowCustomTitleEnabled =
            ta.getBoolean(R.styleable.TitleBar_displayShowCustomTitleEnabled, false)

        if (ta.getBoolean(R.styleable.TitleBar_provideSupportActionBar, false)) {
            val showHome = ta.getBoolean(R.styleable.TitleBar_displayShowHomeEnabled, false)
            val showHomeAsUp = ta.getBoolean(R.styleable.TitleBar_displayShowHomeAsUpEnabled, false)
            actionBar = context.appCompatActivity?.let {
                getSupportActionBar(
                    it,
                    toolbar,
                    !displayShowCustomTitleEnabled,
                    showHome,
                    showHomeAsUp
                )
            }
        }

        val titleText = ta.getText(R.styleable.TitleBar_title)
        if (!titleText.isNullOrEmpty()) {
            title = titleText
        }

        val subtitleText = ta.getText(R.styleable.TitleBar_subtitle)
        if (!subtitleText.isNullOrEmpty()) {
            subtitle = subtitleText
        }

        if (ta.hasValue(R.styleable.TitleBar_titleTextAppearance)) {
            titleTextAppearance = ta.getResourceId(R.styleable.TitleBar_titleTextAppearance, 0)
        }

        if (ta.hasValue(R.styleable.TitleBar_subtitleTextAppearance)) {
            subtitleTextAppearance =
                ta.getResourceId(R.styleable.TitleBar_subtitleTextAppearance, 0)
        }

        if (ta.hasValue(R.styleable.TitleBar_titleTextColor)) {
            titleTextColor = ta.getColorStateList(R.styleable.TitleBar_titleTextColor)
        }

        if (ta.hasValue(R.styleable.TitleBar_subtitleTextColor)) {
            subtitleTextColor = ta.getColorStateList(R.styleable.TitleBar_subtitleTextColor)
        }

        if (ta.hasValue(R.styleable.TitleBar_navigationIcon)) {
            navigationIcon = AppCompatResources.getDrawable(
                context,
                ta.getResourceId(R.styleable.TitleBar_navigationIcon, 0)
            )
            navigationIconTint = ta.getColorStateList(R.styleable.TitleBar_navigationIconTint)
            navigationIconTintMode = intToMode(
                ta.getInt(R.styleable.TitleBar_navigationIconTintMode, 9)
            )

            navigationContentDescription =
                ta.getText(R.styleable.TitleBar_navigationContentDescription)
        }

        showBottomDivider =
            ta.getInt(R.styleable.TitleBar_showBottomDivider, SHOW_BOTTOM_DIVIDER_IF_NEED)
        bottomDividerHeight = ta.getDimensionPixelSize(R.styleable.TitleBar_bottomDividerHeight, 1)
        bottomDividerColor = ta.getColor(R.styleable.TitleBar_bottomDividerColor, Color.GRAY)

        if (ta.hasValue(R.styleable.TitleBar_popupTheme)) {
            popupTheme = ta.getResourceId(R.styleable.TitleBar_popupTheme, 0)
        }

        if (ta.hasValue(R.styleable.TitleBar_menu)) {
            inflateMenu(ta.getResourceId(R.styleable.TitleBar_menu, 0))
        }

        ta.recycle()
    }

}