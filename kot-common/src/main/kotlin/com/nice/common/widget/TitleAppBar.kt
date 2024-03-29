@file:Suppress("UNUSED", "RestrictedApi", "PrivateResource")

package com.nice.common.widget

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.DrawableUtils
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.MarginLayoutParamsCompat
import androidx.core.view.children
import androidx.core.view.contains
import androidx.core.view.isGone
import androidx.core.widget.TextViewCompat
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.internal.ThemeEnforcement
import com.nice.common.R
import com.nice.common.app.HasActionBarSubtitle
import com.nice.common.app.subtitle
import com.nice.common.external.NO_GETTER
import com.nice.common.external.NO_GETTER_MESSAGE
import com.nice.common.helper.appCompatActivity
import java.util.*

class TitleAppBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppBarLayout(
    context,
    attrs
) {

    private val toolbar: TitleToolbar
    private var actionBar: ActionBar? = null

    private var bottomDividerVisible: Boolean = false
    private var bottomDividerHeight: Int = 0
    private var bottomDividerColor: Int = 0
    private val bottomDividerDrawable: ColorDrawable by lazy { ColorDrawable() }

    fun setPopupTheme(@StyleRes theme: Int) {
        toolbar.popupTheme = theme
    }

    fun getPopupTheme(): Int = toolbar.popupTheme

    fun setTitle(title: CharSequence?) {
        if (actionBar != null) {
            actionBar!!.title = title
        } else {
            toolbar.title = title
        }
    }

    fun setTitle(@StringRes resId: Int) = setTitle(context.getText(resId))

    fun getTitle(): CharSequence? = toolbar.title

    fun setTitleTextColor(color: ColorStateList?) {
        color ?: return
        toolbar.setTitleTextColor(color)
    }

    fun setTitleTextColor(@ColorInt color: Int) = setTitleTextColor(ColorStateList.valueOf(color))

    fun setTitleTextAppearance(@StyleRes resId: Int) = toolbar.setTitleTextAppearance(context, resId)

    fun setSubtitle(subtitle: CharSequence?) {
        if (actionBar != null) {
            actionBar!!.subtitle = subtitle
        } else {
            toolbar.subtitle = subtitle
        }
    }

    fun getSubtitle(): CharSequence? = toolbar.subtitle

    fun setSubtitle(@StringRes resId: Int) = setSubtitle(context.getText(resId))

    fun setSubtitleTextColor(color: ColorStateList?) {
        color ?: return
        toolbar.setSubtitleTextColor(color)
    }

    fun setSubtitleTextColor(@ColorInt color: Int) = setSubtitleTextColor(ColorStateList.valueOf(color))

    fun setSubtitleTextAppearance(@StyleRes resId: Int) = toolbar.setSubtitleTextAppearance(context, resId)

    fun setTitleCentered(titleCentered: Boolean) {
        toolbar.isTitleCentered = titleCentered
    }

    fun isTitleCentered(): Boolean = toolbar.isTitleCentered

    fun setSubtitleCentered(subtitleCentered: Boolean) {
        toolbar.isSubtitleCentered = subtitleCentered
    }

    fun isSubtitleCentered(): Boolean = toolbar.isSubtitleCentered

    fun setTitleMargin(start: Int, top: Int, end: Int, bottom: Int) = toolbar.setTitleMargin(start, top, end, bottom)

    fun getTitleMarginStart(): Int = toolbar.titleMarginStart

    fun setTitleMarginStart(margin: Int) {
        toolbar.titleMarginStart = margin
    }

    fun getTitleMarginTop(): Int = toolbar.titleMarginTop

    fun setTitleMarginTop(margin: Int) {
        toolbar.titleMarginTop = margin
    }

    fun getTitleMarginEnd(): Int = toolbar.titleMarginEnd

    fun setTitleMarginEnd(margin: Int) {
        toolbar.titleMarginEnd = margin
    }

    fun getTitleMarginBottom(): Int = toolbar.titleMarginBottom

    fun setTitleMarginBottom(margin: Int) {
        toolbar.titleMarginBottom = margin
    }

    fun setContentInsetsRelative(contentInsetStart: Int, contentInsetEnd: Int) =
        toolbar.setContentInsetsRelative(contentInsetStart, contentInsetEnd)

    fun getContentInsetStart(): Int = toolbar.contentInsetStart

    fun getContentInsetEnd(): Int = toolbar.contentInsetEnd

    fun setContentInsetsAbsolute(contentInsetLeft: Int, contentInsetRight: Int) =
        toolbar.setContentInsetsAbsolute(contentInsetLeft, contentInsetRight)

    fun getContentInsetLeft(): Int = toolbar.contentInsetLeft

    fun getContentInsetRight(): Int = toolbar.contentInsetRight

    fun getContentInsetStartWithNavigation(): Int = toolbar.contentInsetStartWithNavigation

    fun setContentInsetStartWithNavigation(insetStartWithNavigation: Int) {
        toolbar.contentInsetStartWithNavigation = insetStartWithNavigation
    }

    fun getContentInsetEndWithActions(): Int = toolbar.contentInsetEndWithActions

    fun setContentInsetEndWithActions(insetEndWithActions: Int) {
        toolbar.contentInsetEndWithActions = insetEndWithActions
    }

    fun setNavigationIcon(icon: Drawable?) {
        toolbar.navigationIcon = icon
    }

    fun getNavigationIcon(): Drawable? = toolbar.navigationIcon

    fun setNavigationIcon(@DrawableRes resId: Int) = setNavigationIcon(ContextCompat.getDrawable(context, resId))

    fun setNavigationIconTintList(color: ColorStateList?) = toolbar.setNavigationIconTintList(color)

    fun setNavigationIconTint(@ColorInt color: Int) {
        setNavigationIconTintList(ColorStateList.valueOf(color))
    }

    fun getNavigationIconTintList(): ColorStateList? = toolbar.getNavigationIconTintList()

    fun setNavigationIconTintMode(mode: PorterDuff.Mode?) = toolbar.setNavigationIconTintMode(mode)

    fun getNavigationIconTintMode(): PorterDuff.Mode? = toolbar.getNavigationIconTintMode()

    fun setNavigationEnabled(enabled: Boolean) = toolbar.setNavigationEnabled(enabled)

    fun isNavigationEnabled(): Boolean = toolbar.isNavigationEnabled()

    fun setNavigationContentDescription(description: CharSequence?) {
        toolbar.navigationContentDescription = description
    }

    fun setNavigationContentDescription(@StringRes resId: Int) = setNavigationContentDescription(context.getText(resId))

    fun getNavigationContentDescription(): CharSequence? = toolbar.navigationContentDescription

    fun setNavigationOnClickListener(clickListener: OnClickListener?) =
        toolbar.setNavigationOnClickListener(clickListener)

    fun setTitleVisible(visible: Boolean) {
        if (actionBar != null) {
            actionBar!!.setDisplayShowTitleEnabled(visible)
        } else {
            toolbar.setTitleVisible(visible)
        }
    }

    fun isTitleVisible(): Boolean {
        if (actionBar != null) {
            return (actionBar!!.displayOptions and ActionBar.DISPLAY_SHOW_TITLE) != 0
        }
        return toolbar.isTitleVisible()
    }

    fun inflateMenu(@MenuRes id: Int) = toolbar.inflateMenu(id)

    fun getMenu(): Menu = toolbar.menu

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

    fun findMenuItem(@IdRes menuId: Int): MenuItem = toolbar.menu.findItem(menuId)

    fun getMenuItem(index: Int): MenuItem = toolbar.menu.getItem(index)

    fun setOnMenuItemClickListener(listener: Toolbar.OnMenuItemClickListener?) =
        toolbar.setOnMenuItemClickListener(listener)

    fun setOnTitleClickListener(listener: OnClickListener?) = toolbar.setOnTitleClickListener(listener)

    fun setOnSubtitleClickListener(listener: OnClickListener?) = toolbar.setOnSubtitleClickListener(listener)

    fun setBottomDividerVisible(visible: Boolean) {
        if (bottomDividerVisible != visible) {
            bottomDividerVisible = visible
            invalidate()
        }
    }

    fun isBottomDividerVisible(): Boolean = bottomDividerVisible

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        if (heightMode == MeasureSpec.EXACTLY && isDirectToolbar()) {
            val lp = toolbar.layoutParams
            if (lp != null) {
                lp.height = MeasureSpec.getSize(heightMeasureSpec)
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (bottomDividerVisible) {
            bottomDividerDrawable.apply {
                color = bottomDividerColor
                setBounds(0, height - bottomDividerHeight, width, height)
            }.draw(canvas)
        }
    }

    private fun isDirectToolbar(): Boolean {
        return toolbar.parent == this && childCount == 1
    }

    private fun findSuitableTitleToolbar(): TitleToolbar {
        val toolbarView = findViewById<View>(R.id.toolbar)
        if (toolbarView != null) {
            check(toolbarView is TitleToolbar) {
                "The view with ID ${toolbarView.id} is not a TitleToolbar"
            }
            return toolbarView
        }

        val views = LinkedList<View>()
        views.add(this)
        while (!views.isEmpty()) {
            val view = views.removeFirst()
            if (view is TitleToolbar) {
                return view
            }

            if (view is ViewGroup) {
                views.addAll(view.children)
            }
        }

        throw IllegalStateException("Can not find a suitable TitleToolbar")
    }

    private fun attachToActivity(activity: AppCompatActivity, block: ActionBar.() -> Unit = {}) {
        activity.setSupportActionBar(toolbar)
        if (activity is HasActionBarSubtitle) {
            setSubtitle(activity.subtitle)
        }
        actionBar = activity.supportActionBar!!.apply {
            setDisplayShowTitleEnabled(toolbar.isTitleVisible())
            block()
        }
    }

    init {
        val ta = ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.TitleAppBar, R.attr.titleAppBarStyle, R.style.Widget_Design_AppBarLayout
        )

        if (ta.hasValue(R.styleable.TitleAppBar_customLayout)) {
            inflate(context, ta.getResourceId(R.styleable.TitleAppBar_customLayout, 0), this)
        } else {
            inflate(context, R.layout.abc_title_toolbar, this)
        }

        toolbar = findSuitableTitleToolbar()

        val titleMargin: Int = ta.getDimensionPixelOffset(R.styleable.TitleAppBar_titleMargin, 0)
        val marginStart: Int = ta.getDimensionPixelOffset(R.styleable.TitleAppBar_titleMarginStart, titleMargin)
        val marginEnd: Int = ta.getDimensionPixelOffset(R.styleable.TitleAppBar_titleMarginEnd, titleMargin)
        val marginTop: Int = ta.getDimensionPixelOffset(R.styleable.TitleAppBar_titleMarginTop, titleMargin)
        val marginBottom: Int = ta.getDimensionPixelOffset(R.styleable.TitleAppBar_titleMarginBottom, titleMargin)
        if (marginStart != NO_DIMEN || marginEnd != NO_DIMEN || marginTop != NO_DIMEN || marginBottom != NO_DIMEN) {
            setTitleMargin(
                if (marginStart == NO_DIMEN) 0 else marginStart,
                if (marginTop == NO_DIMEN) 0 else marginTop,
                if (marginEnd == NO_DIMEN) 0 else marginEnd,
                if (marginBottom == NO_DIMEN) 0 else marginBottom
            )
        }

        val contentInsetLeft: Int = ta.getDimensionPixelSize(R.styleable.TitleAppBar_contentInsetLeft, NO_DIMEN)
        val contentInsetRight: Int = ta.getDimensionPixelSize(R.styleable.TitleAppBar_contentInsetRight, NO_DIMEN)

        if (contentInsetLeft != NO_DIMEN || contentInsetRight != NO_DIMEN) {
            setContentInsetsAbsolute(
                if (contentInsetLeft == NO_DIMEN) 0 else contentInsetLeft,
                if (contentInsetRight == NO_DIMEN) 0 else contentInsetRight
            )
        }

        val contentInsetStart: Int = ta.getDimensionPixelOffset(R.styleable.TitleAppBar_contentInsetStart, NO_DIMEN)
        val contentInsetEnd: Int = ta.getDimensionPixelOffset(R.styleable.TitleAppBar_contentInsetEnd, NO_DIMEN)
        if (contentInsetStart != NO_DIMEN || contentInsetEnd != NO_DIMEN) {
            setContentInsetsRelative(
                if (contentInsetStart == NO_DIMEN) 0 else contentInsetStart,
                if (contentInsetEnd == NO_DIMEN) 0 else contentInsetEnd
            )
        }

        val contentInsetStartWithNavigation = ta.getDimensionPixelOffset(
            R.styleable.TitleAppBar_contentInsetStartWithNavigation, NO_DIMEN
        )
        if (contentInsetStartWithNavigation != NO_DIMEN) {
            setContentInsetStartWithNavigation(contentInsetStartWithNavigation)
        }
        val contentInsetEndWithActions = ta.getDimensionPixelOffset(
            R.styleable.TitleAppBar_contentInsetEndWithActions, NO_DIMEN
        )
        if (contentInsetEndWithActions != NO_DIMEN) {
            setContentInsetEndWithActions(contentInsetEndWithActions)
        }

        setTitleVisible(ta.getBoolean(R.styleable.TitleAppBar_titleVisible, true))
        setNavigationEnabled(ta.getBoolean(R.styleable.TitleAppBar_navigationEnabled, true))

        val activity = context.appCompatActivity
        if (activity != null && ta.getBoolean(R.styleable.TitleAppBar_provideActionBar, false)) {
            val navigationAsUp = ta.getBoolean(R.styleable.TitleAppBar_navigationAsUp, false)
            attachToActivity(activity) { setDisplayHomeAsUpEnabled(navigationAsUp) }
        }

        val titleText = ta.getText(R.styleable.TitleAppBar_title)
        if (!titleText.isNullOrEmpty()) {
            setTitle(titleText)
        }

        val subtitleText = ta.getText(R.styleable.TitleAppBar_subtitle)
        if (!subtitleText.isNullOrEmpty()) {
            setSubtitle(subtitleText)
        }

        if (ta.hasValue(R.styleable.TitleAppBar_titleTextAppearance)) {
            setTitleTextAppearance(ta.getResourceId(R.styleable.TitleAppBar_titleTextAppearance, 0))
        }

        if (ta.hasValue(R.styleable.TitleAppBar_subtitleTextAppearance)) {
            setSubtitleTextAppearance(ta.getResourceId(R.styleable.TitleAppBar_subtitleTextAppearance, 0))
        }

        if (ta.hasValue(R.styleable.TitleAppBar_titleTextColor)) {
            setTitleTextColor(ta.getColorStateList(R.styleable.TitleAppBar_titleTextColor))
        }

        if (ta.hasValue(R.styleable.TitleAppBar_subtitleTextColor)) {
            setSubtitleTextColor(ta.getColorStateList(R.styleable.TitleAppBar_subtitleTextColor))
        }

        if (ta.hasValue(R.styleable.TitleAppBar_titleCentered)) {
            setTitleCentered(ta.getBoolean(R.styleable.TitleAppBar_titleCentered, false))
        }

        if (ta.hasValue(R.styleable.TitleAppBar_subtitleCentered)) {
            setSubtitleCentered(ta.getBoolean(R.styleable.TitleAppBar_subtitleCentered, false))
        }

        if (ta.hasValue(R.styleable.TitleAppBar_navigationIconTint)) {
            setNavigationIconTintList(ta.getColorStateList(R.styleable.TitleAppBar_navigationIconTint))
        }

        if (ta.hasValue(R.styleable.TitleAppBar_navigationIconTintMode)) {
            setNavigationIconTintMode(
                DrawableUtils.parseTintMode(
                    ta.getInt(R.styleable.TitleAppBar_navigationIconTintMode, 0),
                    PorterDuff.Mode.SRC_ATOP
                )
            )
        }

        val navIcon = ta.getDrawable(R.styleable.TitleAppBar_navigationIcon)
        if (navIcon != null) {
            setNavigationIcon(navIcon)
        }

        val navDesc = ta.getText(R.styleable.TitleAppBar_navigationContentDescription)
        if (!navDesc.isNullOrEmpty()) {
            setNavigationContentDescription(navDesc)
        }

        if (ta.hasValue(R.styleable.TitleAppBar_popupTheme)) {
            setPopupTheme(ta.getResourceId(R.styleable.TitleAppBar_popupTheme, 0))
        }

        if (ta.hasValue(R.styleable.TitleAppBar_menu)) {
            inflateMenu(ta.getResourceId(R.styleable.TitleAppBar_menu, 0))
        }

        bottomDividerHeight = ta.getDimensionPixelSize(R.styleable.TitleAppBar_bottomDividerHeight, 1)
        bottomDividerColor = ta.getColor(R.styleable.TitleAppBar_bottomDividerColor, Color.GRAY)
        bottomDividerVisible = ta.getBoolean(R.styleable.TitleAppBar_bottomDividerVisible, false)

        ta.recycle()
    }

    companion object {

        private const val NO_DIMEN = Int.MIN_VALUE

        val TitleAppBar.menu: Menu
            get() = getMenu()

        var TitleAppBar.title: CharSequence?
            get() = getTitle()
            set(value) {
                setTitle(value)
            }

        var TitleAppBar.subtitle: CharSequence?
            get() = getSubtitle()
            set(value) {
                setSubtitle(value)
            }

        var TitleAppBar.titleTextColor: Int
            @Deprecated(NO_GETTER_MESSAGE) get() = NO_GETTER
            set(value) {
                setTitleTextColor(value)
            }

        var TitleAppBar.subtitleTextColor: Int
            @Deprecated(NO_GETTER_MESSAGE) get() = NO_GETTER
            set(value) {
                setSubtitleTextColor(value)
            }

        var TitleAppBar.titleTextAppearance: Int
            @Deprecated(NO_GETTER_MESSAGE) get() = NO_GETTER
            set(value) {
                setTitleTextAppearance(value)
            }

        var TitleAppBar.subtitleTextAppearance: Int
            @Deprecated(NO_GETTER_MESSAGE) get() = NO_GETTER
            set(value) {
                setSubtitleTextAppearance(value)
            }

        var TitleAppBar.isTitleCentered: Boolean
            get() = isTitleCentered()
            set(value) {
                setTitleCentered(value)
            }

        var TitleAppBar.isSubtitleCentered: Boolean
            get() = isSubtitleCentered()
            set(value) {
                setSubtitleCentered(value)
            }

        var TitleAppBar.titleMargin: Int
            @Deprecated(NO_GETTER_MESSAGE) get() = NO_GETTER
            set(value) {
                setTitleMargin(value, value, value, value)
            }

        var TitleAppBar.titleMarginStart: Int
            get() = getTitleMarginStart()
            set(value) {
                setTitleMarginStart(value)
            }

        var TitleAppBar.titleMarginEnd: Int
            get() = getTitleMarginEnd()
            set(value) {
                setTitleMarginEnd(value)
            }

        var TitleAppBar.titleMarginTop: Int
            get() = getTitleMarginTop()
            set(value) {
                setTitleMarginTop(value)
            }

        var TitleAppBar.titleMarginBottom: Int
            get() = getTitleMarginBottom()
            set(value) {
                setTitleMarginBottom(value)
            }

        var TitleAppBar.horizontalContentInsetRelative: Int
            @Deprecated(NO_GETTER_MESSAGE) get() = NO_GETTER
            set(value) {
                setContentInsetsRelative(value, value)
            }

        var TitleAppBar.horizontalContentInsetAbsolute: Int
            @Deprecated(NO_GETTER_MESSAGE) get() = NO_GETTER
            set(value) {
                setContentInsetsAbsolute(value, value)
            }

        var TitleAppBar.contentInsetStartWithNavigation: Int
            get() = getContentInsetStartWithNavigation()
            set(value) {
                setContentInsetStartWithNavigation(value)
            }

        var TitleAppBar.contentInsetEndWithActions: Int
            get() = getContentInsetEndWithActions()
            set(value) {
                setContentInsetEndWithActions(value)
            }

        var TitleAppBar.navigationIcon: Drawable?
            get() = getNavigationIcon()
            set(value) {
                setNavigationIcon(value)
            }

        var TitleAppBar.navigationIconTint: Int
            @Deprecated(NO_GETTER_MESSAGE) get() = NO_GETTER
            set(value) {
                setNavigationIconTint(value)
            }

        var TitleAppBar.navigationIconTintList: ColorStateList?
            get() = getNavigationIconTintList()
            set(value) {
                setNavigationIconTintList(value)
            }

        var TitleAppBar.navigationIconTintMode: PorterDuff.Mode?
            get() = getNavigationIconTintMode()
            set(value) {
                setNavigationIconTintMode(value)
            }

        var TitleAppBar.isNavigationEnabled: Boolean
            get() = isNavigationEnabled()
            set(value) {
                setNavigationEnabled(value)
            }

        var TitleAppBar.navigationContentDescription: CharSequence?
            get() = getNavigationContentDescription()
            set(value) {
                setNavigationContentDescription(value)
            }

        var TitleAppBar.isTitleVisible: Boolean
            get() = isTitleVisible()
            set(value) {
                setTitleVisible(value)
            }

        var TitleAppBar.isBottomDividerVisible: Boolean
            get() = isBottomDividerVisible()
            set(value) {
                setBottomDividerVisible(value)
            }

        fun AppCompatActivity.setTitleAppBar(
            titleAppBar: TitleAppBar,
            block: ActionBar.() -> Unit = {}
        ) {
            titleAppBar.attachToActivity(this, block)
        }

    }

}

internal class TitleToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : MaterialToolbar(
    context,
    attrs
) {

    private var titleText: CharSequence? = null
    private var titleTextAppearance: Int = 0
    private var titleTextColor: ColorStateList? = null

    private var subtitleText: CharSequence? = null
    private var subtitleTextAppearance: Int = 0
    private var subtitleTextColor: ColorStateList? = null

    private var titleTextView: TextView? = null
    private var subtitleTextView: TextView? = null

    private var titleVisible: Boolean = true

    private var navigationIcon: Drawable? = null
    private var navigationIconTint: ColorStateList? = null
    private var navigationIconTintMode: PorterDuff.Mode? = null
    private var navigationContentDescription: CharSequence? = null

    private var navigationButtonView: ImageButton? = null
    private var navigationWidth: Int = 0

    private var navigationEnabled: Boolean = true

    private var isLayoutInflated: Boolean = false

    private var toolbarLayout: CollapsingToolbarLayout? = null

    override fun setTitle(title: CharSequence?) {
        titleText = title

        if (!isLayoutInflated || !titleVisible) return

        if (hasCollapsingToolbarLayoutParent()) {
            toolbarLayout!!.title = title
            titleTextView?.isGone = true
        } else {
            val titleView = titleTextView
            if (titleView != null) {
                titleView.isGone = title.isNullOrEmpty()
                titleView.text = title
            } else {
                super.setTitle(title)
            }
        }
    }

    override fun getTitle(): CharSequence? = titleText

    override fun setTitleTextAppearance(context: Context, resId: Int) {
        titleTextAppearance = resId

        if (!isLayoutInflated) return

        val titleView = titleTextView
        if (titleView != null) {
            if (!isOverrideAttributes(titleView)) {
                TextViewCompat.setTextAppearance(titleView, resId)
            }
        } else {
            super.setTitleTextAppearance(context, resId)
        }
    }

    override fun setTitleTextColor(color: ColorStateList) {
        titleTextColor = color

        if (!isLayoutInflated) return

        val titleView = titleTextView
        if (titleView != null) {
            if (!isOverrideAttributes(titleView)) {
                titleView.setTextColor(color)
            }
        } else {
            super.setTitleTextColor(color)
        }
    }

    override fun setSubtitle(subtitle: CharSequence?) {
        subtitleText = subtitle

        if (!isLayoutInflated || !titleVisible) return

        if (hasCollapsingToolbarLayoutParent()) {
            subtitleTextView?.isGone = true
        } else {
            val subtitleView = subtitleTextView
            if (subtitleView != null) {
                subtitleView.isGone = subtitle.isNullOrEmpty()
                subtitleView.text = subtitle
            } else {
                super.setSubtitle(subtitle)
            }
        }
    }

    override fun setSubtitleTextAppearance(context: Context, resId: Int) {
        subtitleTextAppearance = resId

        if (!isLayoutInflated) return

        val subtitleView = subtitleTextView
        if (subtitleView != null) {
            if (!isOverrideAttributes(subtitleView)) {
                TextViewCompat.setTextAppearance(subtitleView, resId)
            }
        } else {
            super.setSubtitleTextAppearance(context, resId)
        }
    }

    override fun setSubtitleTextColor(color: ColorStateList) {
        subtitleTextColor = color

        if (!isLayoutInflated) return

        val subtitleView = subtitleTextView
        if (subtitleView != null) {
            if (!isOverrideAttributes(subtitleView)) {
                subtitleView.setTextColor(color)
            }
        } else {
            super.setSubtitleTextColor(color)
        }
    }

    fun setTitleVisible(visible: Boolean) {
        if (titleVisible == visible) {
            return
        }

        titleVisible = visible

        if (visible) {
            title = titleText
            subtitle = subtitleText
        } else {
            title = null
            subtitle = null
        }
    }

    fun isTitleVisible(): Boolean = titleVisible

    override fun setNavigationIcon(icon: Drawable?) {
        navigationIcon = icon

        if (!isLayoutInflated) return

        val navButtonView = navigationButtonView
        if (navButtonView != null) {
            navButtonView.isGone = icon == null
            navButtonView.setImageDrawable(
                if (isOverrideAttributes(navButtonView)) icon
                else maybeTintNavigationIcon(icon)
            )
        } else {
            super.setNavigationIcon(maybeTintNavigationIcon(icon))
        }
    }

    override fun getNavigationIcon(): Drawable? {
        return navigationButtonView?.drawable ?: super.getNavigationIcon()
    }

    fun setNavigationIconTintList(color: ColorStateList?) {
        navigationIconTint = color

        if (!isOverrideAttributes(navigationButtonView)) {
            val navIcon = getNavigationIcon()
            if (navIcon != null) {
                setNavigationIcon(navIcon)
            }
        }
    }

    override fun setNavigationIconTint(@ColorInt color: Int) = setNavigationIconTintList(ColorStateList.valueOf(color))

    fun getNavigationIconTintList(): ColorStateList? = navigationIconTint

    fun setNavigationIconTintMode(mode: PorterDuff.Mode?) {
        navigationIconTintMode = mode

        if (!isOverrideAttributes(navigationButtonView)) {
            val navIcon = getNavigationIcon()
            if (navIcon != null) {
                setNavigationIcon(navIcon)
            }
        }
    }

    fun getNavigationIconTintMode(): PorterDuff.Mode? = navigationIconTintMode

    fun setNavigationEnabled(enabled: Boolean) {
        navigationEnabled = enabled
        if (navigationButtonView != null) {
            navigationButtonView!!.isEnabled = enabled
        }
    }

    fun isNavigationEnabled(): Boolean = navigationEnabled

    override fun setNavigationContentDescription(description: CharSequence?) {
        navigationContentDescription = description

        if (!isLayoutInflated) return

        val navButtonView = navigationButtonView
        if (navButtonView != null) {
            if (!isOverrideAttributes(navButtonView)) {
                navButtonView.contentDescription = description
            }
        } else {
            super.setNavigationContentDescription(description)
        }
    }

    override fun setNavigationOnClickListener(listener: OnClickListener?) {
        if (navigationButtonView != null) {
            navigationButtonView!!.setOnClickListener(listener)
        } else {
            super.setNavigationOnClickListener(listener)
        }
    }

    override fun getNavigationContentDescription(): CharSequence? {
        return navigationContentDescription ?: super.getNavigationContentDescription()
    }

    override fun getCurrentContentInsetStart(): Int {
        return super.getCurrentContentInsetStart() - navigationWidth
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams?): Boolean {
        return super.checkLayoutParams(p) && p is LayoutParams
    }

    override fun generateLayoutParams(attrs: AttributeSet?): Toolbar.LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): Toolbar.LayoutParams {
        return when (p) {
            is LayoutParams -> LayoutParams(p)
            is Toolbar.LayoutParams -> LayoutParams(p)
            is ActionBar.LayoutParams -> LayoutParams(p)
            is MarginLayoutParams -> LayoutParams(p)
            else -> LayoutParams(p)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        isLayoutInflated = true

        ensureTitleTextView()
        ensureSubtitleTextView()
        ensureNavigationButtonView()

        if (!titleText.isNullOrEmpty()) {
            title = titleText
        } else {
            titleTextView?.isGone = true
        }

        if (!subtitleText.isNullOrEmpty()) {
            subtitle = subtitleText
        } else {
            subtitleTextView?.isGone = true
        }

        if (titleTextAppearance != 0) {
            setTitleTextAppearance(context, titleTextAppearance)
        }

        if (subtitleTextAppearance != 0) {
            setSubtitleTextAppearance(context, subtitleTextAppearance)
        }

        if (titleTextColor != null) {
            setTitleTextColor(titleTextColor!!)
        }

        if (subtitleTextColor != null) {
            setSubtitleTextColor(subtitleTextColor!!)
        }

        if (navigationIcon != null) {
            setNavigationIcon(navigationIcon)
        } else {
            navigationButtonView?.isGone = true
        }

        if (!navigationContentDescription.isNullOrEmpty()) {
            setNavigationContentDescription(navigationContentDescription)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val navButtonView = navigationButtonView
        if (navButtonView != null) {
            measureChildWithMargins(navButtonView, widthMeasureSpec, 0, heightMeasureSpec, 0)
            val lp = navButtonView.layoutParams as MarginLayoutParams
            val leftMargin = MarginLayoutParamsCompat.getMarginStart(lp)
            val rightMargin = MarginLayoutParamsCompat.getMarginEnd(lp)
            navigationWidth = navButtonView.measuredWidth + leftMargin + rightMargin
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    fun setOnTitleClickListener(listener: OnClickListener?) {
        val titleView = titleTextView ?: tryGetSystemTitleView("mTitleTextView")
        titleView?.setOnClickListener(listener)
    }

    fun setOnSubtitleClickListener(listener: OnClickListener?) {
        val subtitleView = subtitleTextView ?: tryGetSystemTitleView("mSubtitleTextView")
        subtitleView?.setOnClickListener(listener)
    }

    private fun hasCollapsingToolbarLayoutParent(): Boolean {
        if (toolbarLayout != null) {
            return true
        }

        toolbarLayout = parent as? CollapsingToolbarLayout

        return toolbarLayout != null
    }

    private fun ensureTitleTextView() {
        if (titleTextView != null) {
            return
        }

        val titleView = findViewById<View>(R.id.title) ?: return
        check(titleView is TextView) {
            "The view with ID ${titleView.id} is not a TextView"
        }

        titleTextView = titleView
    }

    private fun ensureSubtitleTextView() {
        if (subtitleTextView != null) {
            return
        }

        val subtitleView = findViewById<View>(R.id.subtitle) ?: return
        check(subtitleView is TextView) {
            "The view with ID ${subtitleView.id} is not a TextView"
        }

        subtitleTextView = subtitleView
    }

    private fun tryGetSystemTitleView(name: String): TextView? {
        try {
            val field = Toolbar::class.java.getDeclaredField(name)
            field.isAccessible = true
            return field.get(this) as TextView?
        } catch (_: NoSuchFieldException) {
        } catch (_: IllegalArgumentException) {
        } catch (_: IllegalAccessException) {
        }
        return null
    }

    private fun ensureNavigationButtonView() {
        if (navigationButtonView != null) {
            return
        }

        val buttonView = findViewById<View>(R.id.navigation) ?: return
        check(buttonView is ImageButton) {
            "The view with ID ${buttonView.id} is not a ImageButton"
        }
        buttonView.isEnabled = navigationEnabled

        navigationButtonView = buttonView
    }

    private fun maybeTintNavigationIcon(drawable: Drawable?): Drawable? {
        return if (drawable == null || navigationIconTint == null) drawable else {
            DrawableCompat.wrap(drawable.mutate()).also {
                DrawableCompat.setTintList(it, navigationIconTint)
                val tintMode = navigationIconTintMode
                if (tintMode != null) {
                    DrawableCompat.setTintMode(it, tintMode)
                }
            }
        }
    }

    private fun isOverrideAttributes(view: View?): Boolean {
        if (view == null || !contains(view)) {
            return false
        }

        val lp = view.layoutParams ?: return false

        return lp is LayoutParams && lp.isOverrideAttributes
    }

    class LayoutParams : Toolbar.LayoutParams {

        var isOverrideAttributes: Boolean = false

        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.TitleToolbar_Layout)
            isOverrideAttributes = ta.getBoolean(R.styleable.TitleToolbar_Layout_layout_overrideAttributes, false)
            ta.recycle()
        }

        constructor(width: Int, height: Int) : super(width, height)
        constructor(width: Int, height: Int, gravity: Int) : super(width, height, gravity)
        constructor(gravity: Int) : super(gravity)
        constructor(source: Toolbar.LayoutParams) : super(source)
        constructor(source: ActionBar.LayoutParams) : super(source)
        constructor(source: MarginLayoutParams) : super(source)
        constructor(source: ViewGroup.LayoutParams) : super(source)
        constructor(source: LayoutParams) : super(source) {
            isOverrideAttributes = source.isOverrideAttributes
        }

    }

}