package com.easy.kotlins.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
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
    annotation class BottomDividerMode

    private var _navigationIcon: Drawable?
    private var _navigationContentDescription: CharSequence?
    private val _navigationIconTint: ColorStateList?
    private val _navigationIconTintMode: Int
    private val _useCustomTitle: Boolean
    private val _showHomeEnabled: Boolean
    private val _homeAsUpEnabled: Boolean
    private var _titleTextAppearance = 0
    private var _titleTextColor = 0
    private var _subtitleTextAppearance = 0
    private var _subtitleTextColor = 0
    private var _titleText: CharSequence?
    private var _subtitleText: CharSequence?
    private var titleView: TextView? = null
    private val _showBottomDivider: Int
    private var _bottomDividerDrawable: Drawable? = null

    private var actionBar: ActionBar? = null
    private val toolbar: Toolbar = Toolbar(context)

    var navigationIcon: Drawable?
        get() = if (_navigationIcon == null) toolbar.navigationIcon else _navigationIcon
        set(drawable) {
            if (!_showHomeEnabled) return
            _navigationIcon = drawable
            wrapDrawableTint(_navigationIcon, _navigationIconTint, _navigationIconTintMode)
            toolbar.navigationIcon = _navigationIcon
        }
    var navigationContentDescription: CharSequence?
        get() = if (_navigationContentDescription == null) toolbar.navigationContentDescription else _navigationContentDescription
        set(navigationContentDescription) {
            _navigationContentDescription = navigationContentDescription
            toolbar.navigationContentDescription = navigationContentDescription
        }

    var title: CharSequence?
        get() = _titleText
        set(title) {
            _titleText = title
            if (_useCustomTitle) {
                if (actionBar != null) actionBar!!.title = _titleText else toolbar.title =
                    _titleText
            } else {
                ensureTitleTextView()
                titleView!!.text = _titleText
            }
        }
    var subtitle: CharSequence?
        get() = _subtitleText
        set(subtitle) {
            _subtitleText = subtitle
            if (_useCustomTitle) {
                if (actionBar != null) actionBar!!.subtitle = _titleText
                toolbar.subtitle = subtitle
            }
        }

    fun setNavigationContentDescription(@StringRes resId: Int) {
        navigationContentDescription = if (resId != 0) this.context.getText(resId) else null
    }

    fun showNavigationIcon() {
        if (!_showHomeEnabled) return
        toolbar.navigationIcon = _navigationIcon
    }

    fun hideNavigationIcon() {
        if (!_showHomeEnabled) return
        toolbar.navigationIcon = null
    }

    fun setNavigationIcon(@DrawableRes id: Int) {
        navigationIcon = ContextCompat.getDrawable(context, id)
    }

    fun setNavigationOnClickListener(clickListener: OnClickListener?) {
        toolbar.setNavigationOnClickListener(clickListener)
    }

    val menu: Menu
        get() = toolbar.menu

    fun inflateMenu(@MenuRes id: Int): Menu {
        toolbar.inflateMenu(id)
        return toolbar.menu
    }

    fun addMenu(title: CharSequence?): MenuItem {
        return toolbar.menu.add(title)
    }

    fun addMenu(groupId: Int, itemId: Int, order: Int, title: CharSequence?): MenuItem {
        return toolbar.menu.add(groupId, itemId, order, title)
    }

    fun addMenu(groupId: Int, itemId: Int, order: Int, @StringRes titleId: Int): MenuItem {
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
        if (!_useCustomTitle) {
            if (titleView != null) {
                titleView!!.visibility = VISIBLE
            } else {
                ensureTitleTextView()
            }
        }
    }

    fun hideCustomTitle() {
        if (titleView != null) {
            titleView!!.visibility = GONE
        }
    }

    fun setTitle(titleId: Int) {
        _titleText = resources.getString(titleId)
        title = _titleText
    }

    fun setSubtitle(subtitleId: Int) {
        subtitle = resources.getString(subtitleId)
    }

    fun setTitleTextColor(@ColorInt color: Int) {
        _titleTextColor = color
        if (_useCustomTitle) {
            toolbar.setTitleTextColor(color)
        } else {
            ensureTitleTextView()
            titleView!!.setTextColor(color)
        }
    }

    fun setTitleTextAppearance(@StyleRes id: Int) {
        _titleTextAppearance = id
        if (_useCustomTitle) {
            toolbar.setTitleTextAppearance(context, id)
        } else {
            ensureTitleTextView()
            titleView!!.setTextAppearance(context, id)
        }
    }

    fun setSubtitleTextColor(@ColorInt color: Int) {
        _subtitleTextColor = color
        if (_useCustomTitle) {
            toolbar.setSubtitleTextColor(color)
        }
    }

    fun setSubtitleTextAppearance(@StyleRes id: Int) {
        _subtitleTextAppearance = id
        if (_useCustomTitle) {
            toolbar.setSubtitleTextAppearance(context, id)
        }
    }

    fun setShowBottomDivider(showBottomDivider: Int) {}
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val drawable = _bottomDividerDrawable ?: return
        if (showBottomDivider(_showBottomDivider)) {
            val bounds = drawable.bounds
            bounds.right = width
            bounds.top = height - bounds.bottom
            bounds.bottom = height
            drawable.draw(canvas)
        }
    }

    private fun attachToActivity(activity: AppCompatActivity) {
        activity.setSupportActionBar(toolbar)
        actionBar = activity.supportActionBar
        if (actionBar != null) {
            actionBar!!.setDisplayShowTitleEnabled(!_useCustomTitle)
            actionBar!!.setDisplayShowHomeEnabled(_showHomeEnabled)
            actionBar!!.setDisplayHomeAsUpEnabled(_homeAsUpEnabled)
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
            textView.setSingleLine()
            textView.ellipsize = TextUtils.TruncateAt.END
            val layoutParams = Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT
            )
            layoutParams.gravity = Gravity.CENTER
            toolbar.addView(titleView, layoutParams)
        }
    }

    companion object {
        private val ATTRS = intArrayOf(R.attr.titleBarSize)
        const val SHOW_BOTTOM_DIVIDER_IF_NEED = 0
        const val SHOW_BOTTOM_DIVIDER_ALWAYS = 1
        const val SHOW_BOTTOM_DIVIDER_NEVER = 2
        private fun showBottomDivider(showBottomDivider: Int): Boolean {
            return (showBottomDivider == SHOW_BOTTOM_DIVIDER_ALWAYS
                    || showBottomDivider == SHOW_BOTTOM_DIVIDER_IF_NEED && Build.VERSION.SDK_INT < 21)
        }

        private fun wrapDrawableTint(drawable: Drawable?, tint: ColorStateList?, tintMode: Int) {
            if (drawable == null) return
            val wrappedDrawable = DrawableCompat.wrap(drawable.mutate())
            DrawableCompat.setTintList(wrappedDrawable, tint)
            DrawableCompat.setTintMode(wrappedDrawable, intToMode(tintMode))
        }

        private fun getTitleBarHeight(context: Context): Int {
            val a = context.obtainStyledAttributes(ATTRS)
            val scale = context.resources.displayMetrics.density
            val height = a.getDimensionPixelSize(0, (48 * scale + 0.5f).toInt())
            a.recycle()
            return height
        }

        private fun getDrawable(
            context: Context,
            a: TypedArray,
            @StyleableRes index: Int
        ): Drawable? {
            val id = a.getResourceId(index, -1)
            return if (id != -1) {
                AppCompatResources.getDrawable(context, id)
            } else null
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
        addView(toolbar, -1, getTitleBarHeight(context))
        val ta = context.obtainStyledAttributes(
            attrs, R.styleable.TitleBar,
            R.attr.titleBarStyle, 0
        )
        _titleText = ta.getString(R.styleable.TitleBar_title)
        _subtitleText = ta.getString(R.styleable.TitleBar_subtitle)
        _navigationIcon = getDrawable(context, ta, R.styleable.TitleBar_navigationIcon)
        _navigationContentDescription =
            ta.getText(R.styleable.TitleBar_navigationContentDescription)
        _useCustomTitle = ta.getBoolean(R.styleable.TitleBar_useCustomTitle, false)
        _showHomeEnabled = ta.getBoolean(R.styleable.TitleBar_showHomeEnabled, true)
        _homeAsUpEnabled = ta.getBoolean(R.styleable.TitleBar_homeAsUpEnabled, true)
        _navigationIconTint = ta.getColorStateList(R.styleable.TitleBar_navigationIconTint)
        _navigationIconTintMode = ta.getInt(R.styleable.TitleBar_navigationIconTintMode, 9)
        if (!TextUtils.isEmpty(_titleText)) {
            if (_useCustomTitle) {
                toolbar.title = _titleText
            } else {
                ensureTitleTextView()
                titleView!!.text = _titleText
            }
        }
        if (ta.hasValue(R.styleable.TitleBar_titleTextAppearance)) {
            setTitleTextAppearance(ta.getResourceId(R.styleable.TitleBar_titleTextAppearance, 0))
        }
        if (ta.hasValue(R.styleable.TitleBar_titleTextColor)) {
            setTitleTextColor(ta.getColor(R.styleable.TitleBar_titleTextColor, -0x1))
        }
        if (ta.hasValue(R.styleable.TitleBar_menu)) {
            inflateMenu(ta.getResourceId(R.styleable.TitleBar_menu, 0))
        }
        if (_useCustomTitle && !TextUtils.isEmpty(_subtitleText)) {
            toolbar.subtitle = _titleText
            if (ta.hasValue(R.styleable.TitleBar_subtitleTextAppearance)) {
                setSubtitleTextAppearance(
                    ta.getResourceId(
                        R.styleable.TitleBar_subtitleTextAppearance,
                        0
                    )
                )
            }
            if (ta.hasValue(R.styleable.TitleBar_subtitleTextColor)) {
                setSubtitleTextColor(ta.getColor(R.styleable.TitleBar_subtitleTextColor, -0x1))
            }
        }
        if (_navigationIcon != null) {
            wrapDrawableTint(_navigationIcon, _navigationIconTint, _navigationIconTintMode)
            toolbar.navigationIcon = _navigationIcon
        }
        if (_navigationContentDescription != null && _navigationIcon != null) {
            toolbar.navigationContentDescription = _navigationContentDescription
        }
        _showBottomDivider =
            ta.getInt(R.styleable.TitleBar_showBottomDivider, SHOW_BOTTOM_DIVIDER_IF_NEED)
        if (showBottomDivider(_showBottomDivider)) {
            _bottomDividerDrawable =
                ColorDrawable(ta.getColor(R.styleable.TitleBar_bottomDividerColor, Color.GRAY))
            _bottomDividerDrawable?.setBounds(
                0,
                0,
                0,
                ta.getDimensionPixelSize(R.styleable.TitleBar_bottomDividerHeight, 1)
            )
        }
        if (ta.getBoolean(R.styleable.TitleBar_attachToActivity, true)) {
            val activity = context.appCompatActivity
            if (activity != null) {
                attachToActivity(activity)
            }
        }
        ta.recycle()
    }
}