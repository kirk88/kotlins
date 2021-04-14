@file:Suppress("unused", "RestrictedApi")

package com.nice.kotlins.widget

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.Log
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
import androidx.appcompat.widget.TintTypedArray
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.MarginLayoutParamsCompat
import androidx.core.view.contains
import androidx.core.widget.TextViewCompat
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.nice.kotlins.R
import com.nice.kotlins.helper.Internals.NO_GETTER
import com.nice.kotlins.helper.Internals.NO_GETTER_MESSAGE
import com.nice.kotlins.helper.appCompatActivity
import com.nice.kotlins.helper.isGone

class TitleBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.titleBarStyle
) : AppBarLayout(
    context,
    attrs,
    defStyleAttr
) {
    @IntDef(value = [SHOW_BOTTOM_DIVIDER_IF_NEED, SHOW_BOTTOM_DIVIDER_ALWAYS, SHOW_BOTTOM_DIVIDER_NEVER])
    @Retention(AnnotationRetention.SOURCE)
    annotation class BottomDividerMode

    private val toolbar: TitleToolbar
    private var actionBar: ActionBar? = null

    private var showBottomDivider: Int = 0
    private var bottomDividerHeight: Int = 0
    private var bottomDividerColor: Int = 0
    private val bottomDividerDrawable: ColorDrawable by lazy { ColorDrawable() }

    fun setPopupTheme(@StyleRes theme: Int) {
        toolbar.popupTheme = theme
    }

    fun getPopupTheme(): Int {
        return toolbar.popupTheme
    }

    fun setTitle(title: CharSequence?) {
        if (actionBar != null) {
            actionBar!!.title = title
        } else {
            toolbar.title = title
        }
    }

    fun setTitle(@StringRes resId: Int) {
        setTitle(context.getText(resId))
    }

    fun getTitle(): CharSequence? {
        return toolbar.title
    }

    fun setTitleTextColor(color: ColorStateList?) {
        if (color == null) {
            return
        }

        toolbar.setTitleTextColor(color)
    }

    fun setTitleTextColor(@ColorInt color: Int) {
        setTitleTextColor(ColorStateList.valueOf(color))
    }

    fun setTitleTextAppearance(@StyleRes resId: Int) {
        toolbar.setTitleTextAppearance(context, resId)
    }

    fun setSubtitle(subtitle: CharSequence?) {
        if (actionBar != null) {
            actionBar!!.subtitle = subtitle
        } else {
            toolbar.subtitle = subtitle
        }
    }

    fun getSubtitle(): CharSequence? {
        return toolbar.subtitle
    }

    fun setSubtitle(@StringRes resId: Int) {
        setSubtitle(context.getText(resId))
    }

    fun setSubtitleTextColor(color: ColorStateList?) {
        if (color == null) {
            return
        }

        toolbar.setSubtitleTextColor(color)
    }

    fun setSubtitleTextColor(@ColorInt color: Int) {
        setSubtitleTextColor(ColorStateList.valueOf(color))
    }

    fun setSubtitleTextAppearance(@StyleRes resId: Int) {
        toolbar.setSubtitleTextAppearance(context, resId)
    }

    fun setNavigationIcon(icon: Drawable?) {
        toolbar.navigationIcon = icon
    }

    fun getNavigationIcon(): Drawable? {
        return toolbar.navigationIcon
    }

    fun setNavigationIcon(@DrawableRes resId: Int) {
        setNavigationIcon(ContextCompat.getDrawable(context, resId))
    }

    fun setNavigationIconTintList(color: ColorStateList?) {
        toolbar.setNavigationIconTintList(color)
    }

    fun setNavigationIconTint(@ColorInt color: Int) {
        setNavigationIconTintList(ColorStateList.valueOf(color))
    }

    fun getNavigationIconTintList(): ColorStateList? {
        return toolbar.getNavigationIconTintList()
    }

    fun setNavigationIconTintMode(mode: PorterDuff.Mode?) {
        toolbar.setNavigationIconTintMode(mode)
    }

    fun getNavigationIconTintMode(): PorterDuff.Mode? {
        return toolbar.getNavigationIconTintMode()
    }

    fun setNavigationContentDescription(description: CharSequence?) {
        toolbar.navigationContentDescription = description
    }

    fun setNavigationContentDescription(@StringRes resId: Int) {
        setNavigationContentDescription(context.getText(resId))
    }

    fun getNavigationContentDescription(): CharSequence? {
        return toolbar.navigationContentDescription
    }

    fun setNavigationOnClickListener(clickListener: OnClickListener?) {
        toolbar.setNavigationOnClickListener(clickListener)
    }

    fun setDisplayShowTitleEnabled(enabled: Boolean) {
        if (actionBar != null) {
            actionBar!!.setDisplayShowTitleEnabled(enabled)
        } else {
            toolbar.setDisplayShowTitleEnabled(enabled)
        }
    }

    fun isDisplayShowTitleEnabled(): Boolean {
        if (actionBar != null) {
            return (actionBar!!.displayOptions and ActionBar.DISPLAY_SHOW_TITLE) != 0
        }
        return toolbar.isDisplayShowTitleEnabled()
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
        toolbar.setOnTitleClickListener(listener)
    }

    fun setOnSubtitleClickListener(listener: OnClickListener?) {
        toolbar.setOnSubtitleClickListener(listener)
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

    private fun isShowBottomDivider(): Boolean {
        return (showBottomDivider == SHOW_BOTTOM_DIVIDER_ALWAYS
                || showBottomDivider == SHOW_BOTTOM_DIVIDER_IF_NEED && Build.VERSION.SDK_INT < 21)
    }

    companion object {

        const val SHOW_BOTTOM_DIVIDER_IF_NEED = 1
        const val SHOW_BOTTOM_DIVIDER_ALWAYS = 2
        const val SHOW_BOTTOM_DIVIDER_NEVER = 3

        private fun getSupportActionBar(
            activity: AppCompatActivity,
            toolbar: TitleToolbar,
            showHome: Boolean,
            showHomeAsUp: Boolean
        ): ActionBar {
            activity.setSupportActionBar(toolbar)
            val actionBar = activity.supportActionBar!!
            actionBar.setDisplayShowTitleEnabled(toolbar.isDisplayShowTitleEnabled())
            actionBar.setDisplayShowHomeEnabled(showHome)
            actionBar.setDisplayHomeAsUpEnabled(showHomeAsUp)
            return actionBar
        }

    }

    init {
        val ta = TintTypedArray.obtainStyledAttributes(
            context,
            attrs, R.styleable.TitleBar,
            defStyleAttr, R.style.Widget_Design_TitleBar
        )

        if (ta.hasValue(R.styleable.TitleBar_customLayout)) {
            inflate(context, ta.getResourceId(R.styleable.TitleBar_customLayout, 0), this)
        } else {
            inflate(context, R.layout.abc_title_toolbar, this)
        }

        toolbar = findViewById<View>(R.id.toolbar).let {
            check(it is TitleToolbar) {
                "The Toolbar not a TitleToolbar"
            }
            it
        }

        toolbar.setDisplayShowTitleEnabled(
            ta.getBoolean(
                R.styleable.TitleBar_displayShowTitleEnabled,
                true
            )
        )

        if (ta.getBoolean(R.styleable.TitleBar_provideSupportActionBar, false)) {
            val showHome = ta.getBoolean(R.styleable.TitleBar_displayShowHomeEnabled, false)
            val showHomeAsUp = ta.getBoolean(R.styleable.TitleBar_displayShowHomeAsUpEnabled, false)
            actionBar = context.appCompatActivity?.let {
                getSupportActionBar(
                    it,
                    toolbar,
                    showHome,
                    showHomeAsUp
                )
            }
        }

        val titleText = ta.getText(R.styleable.TitleBar_title)
        if (!titleText.isNullOrEmpty()) {
            setTitle(titleText)
        }

        val subtitleText = ta.getText(R.styleable.TitleBar_subtitle)
        if (!subtitleText.isNullOrEmpty()) {
            setSubtitle(subtitleText)
        }

        if (ta.hasValue(R.styleable.TitleBar_titleTextAppearance)) {
            setTitleTextAppearance(ta.getResourceId(R.styleable.TitleBar_titleTextAppearance, 0))
        }

        if (ta.hasValue(R.styleable.TitleBar_subtitleTextAppearance)) {
            setSubtitleTextAppearance(
                ta.getResourceId(
                    R.styleable.TitleBar_subtitleTextAppearance,
                    0
                )
            )
        }

        if (ta.hasValue(R.styleable.TitleBar_titleTextColor)) {
            setTitleTextColor(ta.getColorStateList(R.styleable.TitleBar_titleTextColor))
        }

        if (ta.hasValue(R.styleable.TitleBar_subtitleTextColor)) {
            setSubtitleTextColor(ta.getColorStateList(R.styleable.TitleBar_subtitleTextColor))
        }

        if (ta.hasValue(R.styleable.TitleBar_navigationIconTint)) {
            setNavigationIconTintList(ta.getColorStateList(R.styleable.TitleBar_navigationIconTint))
        }

        if (ta.hasValue(R.styleable.TitleBar_navigationIconTintMode)) {
            setNavigationIconTintMode(
                DrawableUtils.parseTintMode(
                    ta.getInt(R.styleable.TitleToolbar_navigationIconTintMode, 0),
                    PorterDuff.Mode.SRC_ATOP
                )
            )
        }

        val navIcon = ta.getDrawable(R.styleable.TitleBar_navigationIcon)
        if (navIcon != null) {
            setNavigationIcon(navIcon)
        }

        val navDesc = ta.getText(R.styleable.TitleBar_navigationContentDescription)
        if (!navDesc.isNullOrEmpty()) {
            setNavigationContentDescription(navDesc)
        }

        if (ta.hasValue(R.styleable.TitleBar_popupTheme)) {
            setPopupTheme(ta.getResourceId(R.styleable.TitleBar_popupTheme, 0))
        }

        if (ta.hasValue(R.styleable.TitleBar_menu)) {
            inflateMenu(ta.getResourceId(R.styleable.TitleBar_menu, 0))
        }

        bottomDividerHeight =
            ta.getDimensionPixelSize(R.styleable.TitleBar_bottomDividerHeight, 1)
        bottomDividerColor = ta.getColor(R.styleable.TitleBar_bottomDividerColor, Color.GRAY)
        showBottomDivider =
            ta.getInt(R.styleable.TitleBar_showBottomDivider, SHOW_BOTTOM_DIVIDER_IF_NEED)

        ta.recycle()
    }

}

var TitleBar.title: CharSequence?
    get() = getTitle()
    set(value) {
        setTitle(value)
    }

var TitleBar.subtitle: CharSequence?
    get() = getSubtitle()
    set(value) {
        setSubtitle(value)
    }

var TitleBar.titleTextColor: Int
    @Deprecated(NO_GETTER_MESSAGE) get() = NO_GETTER
    set(value) {
        setTitleTextColor(value)
    }

var TitleBar.subtitleTextColor: Int
    @Deprecated(NO_GETTER_MESSAGE) get() = NO_GETTER
    set(value) {
        setSubtitleTextColor(value)
    }

var TitleBar.titleTextAppearance: Int
    @Deprecated(NO_GETTER_MESSAGE) get() = NO_GETTER
    set(value) {
        setTitleTextAppearance(value)
    }

var TitleBar.subtitleTextAppearance: Int
    @Deprecated(NO_GETTER_MESSAGE) get() = NO_GETTER
    set(value) {
        setSubtitleTextAppearance(value)
    }

var TitleBar.navigationIcon: Drawable?
    get() = getNavigationIcon()
    set(value) {
        setNavigationIcon(value)
    }

var TitleBar.navigationIconTint: Int
    @Deprecated(NO_GETTER_MESSAGE) get() = NO_GETTER
    set(value) {
        setNavigationIconTint(value)
    }

var TitleBar.navigationIconTintList: ColorStateList?
    get() = getNavigationIconTintList()
    set(value) {
        setNavigationIconTintList(value)
    }

var TitleBar.navigationIconTintMode: PorterDuff.Mode?
    get() = getNavigationIconTintMode()
    set(value) {
        setNavigationIconTintMode(value)
    }

var TitleBar.navigationContentDescription: CharSequence?
    get() = getNavigationContentDescription()
    set(value) {
        setNavigationContentDescription(value)
    }

var TitleBar.isDisplayShowTitleEnabled: Boolean
    get() = isDisplayShowTitleEnabled()
    set(value) {
        setDisplayShowTitleEnabled(value)
    }

class TitleToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.titleToolbarStyle
) : Toolbar(
    context,
    attrs,
    defStyleAttr
) {

    private var titleText: CharSequence? = null
    private var titleTextAppearance: Int = 0
    private var titleTextColor: ColorStateList? = null

    private var subtitleText: CharSequence? = null
    private var subtitleTextAppearance: Int = 0
    private var subtitleTextColor: ColorStateList? = null

    private var titleTextView: TextView? = null
    private var subtitleTextView: TextView? = null

    private var navigationIcon: Drawable? = null
    private var navigationIconTint: ColorStateList? = null
    private var navigationIconTintMode: PorterDuff.Mode? = null
    private var navigationContentDescription: CharSequence? = null

    private var navigationButtonView: ImageButton? = null
    private var navigationWidth: Int = 0

    private var displayShowTitleEnabled: Boolean = true

    private var toolbarLayout: CollapsingToolbarLayout? = null

    override fun setTitle(title: CharSequence?) {
        titleText = title

        if (!isInLayout || !displayShowTitleEnabled) {
            return
        }

        if (toolbarLayout != null) {
            toolbarLayout!!.title = title
            Log.e("TAGTAG", "title: $title")
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

    override fun getTitle(): CharSequence? {
        return titleText
    }

    override fun setTitleTextAppearance(context: Context, resId: Int) {
        titleTextAppearance = resId

        if (!isInLayout) {
            return
        }

        val titleView = titleTextView
        if (titleView != null) {
            if (!isCustomView(titleView)) {
                TextViewCompat.setTextAppearance(titleView, resId)
            }
        } else {
            super.setTitleTextAppearance(context, resId)
        }
    }

    override fun setTitleTextColor(color: ColorStateList) {
        titleTextColor = color

        if (!isInLayout) {
            return
        }

        val titleView = titleTextView
        if (titleView != null) {
            if (!isCustomView(titleView)) {
                titleView.setTextColor(color)
            }
        } else {
            super.setTitleTextColor(color)
        }
    }

    override fun setSubtitle(subtitle: CharSequence?) {
        subtitleText = subtitle

        if (!isInLayout || !displayShowTitleEnabled) {
            return
        }

        val subtitleView = subtitleTextView
        if (subtitleView != null) {
            subtitleView.isGone = subtitle.isNullOrEmpty()
            subtitleView.text = subtitle
        } else {
            super.setSubtitle(subtitle)
        }
    }

    override fun setSubtitleTextAppearance(context: Context, resId: Int) {
        subtitleTextAppearance = resId

        if (!isInLayout) {
            return
        }

        val subtitleView = subtitleTextView
        if (subtitleView != null) {
            if (!isCustomView(subtitleView)) {
                TextViewCompat.setTextAppearance(subtitleView, resId)
            }
        } else {
            super.setSubtitleTextAppearance(context, resId)
        }
    }

    override fun setSubtitleTextColor(color: ColorStateList) {
        subtitleTextColor = color

        if (!isInLayout) {
            return
        }

        val subtitleView = subtitleTextView
        if (subtitleView != null) {
            if (!isCustomView(subtitleView)) {
                subtitleView.setTextColor(color)
            }
        } else {
            super.setSubtitleTextColor(color)
        }
    }

    fun setDisplayShowTitleEnabled(enabled: Boolean) {
        if (displayShowTitleEnabled == enabled) {
            return
        }

        displayShowTitleEnabled = enabled

        if (enabled) {
            title = titleText
            subtitle = subtitleText
        } else {
            title = null
            subtitle = null
        }
    }

    fun isDisplayShowTitleEnabled(): Boolean {
        return displayShowTitleEnabled
    }

    override fun setNavigationIcon(icon: Drawable?) {
        navigationIcon = icon

        if (!isInLayout) {
            return
        }

        val navButtonView = navigationButtonView
        if (navButtonView != null) {
            navButtonView.isGone = icon == null
            navButtonView.setImageDrawable(
                if (isCustomView(navButtonView)) icon
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

        if (!isCustomView(navigationButtonView)) {
            val navIcon = getNavigationIcon()
            if (navIcon != null) {
                setNavigationIcon(navIcon)
            }
        }
    }

    fun setNavigationIconTint(@ColorInt color: Int) {
        setNavigationIconTintList(ColorStateList.valueOf(color))
    }

    fun getNavigationIconTintList(): ColorStateList? {
        return navigationIconTint
    }

    fun setNavigationIconTintMode(mode: PorterDuff.Mode?) {
        navigationIconTintMode = mode

        if (!isCustomView(navigationButtonView)) {
            val navIcon = getNavigationIcon()
            if (navIcon != null) {
                setNavigationIcon(navIcon)
            }
        }
    }

    fun getNavigationIconTintMode(): PorterDuff.Mode? {
        return navigationIconTintMode
    }

    override fun setNavigationContentDescription(description: CharSequence?) {
        navigationContentDescription = description

        if (!isInLayout) {
            return
        }

        val navButtonView = navigationButtonView
        if (navButtonView != null) {
            if (!isCustomView(navButtonView)) {
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

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)

        ensureToolbarLayout()
    }

    fun setOnTitleClickListener(listener: OnClickListener?) {
        val titleView = titleTextView ?: tryGetSystemTitleView("mTitleTextView")
        titleView?.setOnClickListener(listener)
    }

    fun setOnSubtitleClickListener(listener: OnClickListener?) {
        val subtitleView = subtitleTextView ?: tryGetSystemTitleView("mSubtitleTextView")
        subtitleView?.setOnClickListener(listener)
    }

    private fun ensureToolbarLayout() {
        if (toolbarLayout != null) {
            return
        }

        toolbarLayout = parent as? CollapsingToolbarLayout

        if (!titleText.isNullOrEmpty()) {
            toolbarLayout?.title = titleText
        }
    }

    private fun ensureTitleTextView() {
        if (titleTextView != null) {
            return
        }

        val titleView = findViewById<View>(R.id.title) ?: return
        check(titleView is TextView) {
            "The Title View not a TextView"
        }

        titleTextView = titleView
    }

    private fun ensureSubtitleTextView() {
        if (subtitleTextView != null) {
            return
        }

        val subtitleView = findViewById<View>(R.id.subtitle) ?: return
        check(subtitleView is TextView) {
            "The Subtitle View not a TextView"
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
            "The Navigation View not a ImageButton"
        }

        navigationButtonView = buttonView
    }

    private fun maybeTintNavigationIcon(drawable: Drawable?): Drawable? {
        return if (drawable == null) drawable else {
            DrawableCompat.wrap(drawable.mutate()).also {
                DrawableCompat.setTintList(it, navigationIconTint)
                val tintMode = navigationIconTintMode
                if (tintMode != null) {
                    DrawableCompat.setTintMode(it, tintMode)
                }
            }
        }
    }

    private fun isCustomView(view: View?): Boolean {
        if (view == null || !contains(view)) {
            return false
        }

        val lp = view.layoutParams ?: return false

        return lp is LayoutParams && lp.isCustom
    }

    class LayoutParams : Toolbar.LayoutParams {

        internal var isCustom: Boolean = false

        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.TitleToolbar_Layout)
            isCustom =
                ta.getBoolean(R.styleable.TitleToolbar_Layout_layout_custom, false)
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
            isCustom = source.isCustom
        }

    }

    init {
        val ta = TintTypedArray.obtainStyledAttributes(
            getContext(),
            attrs, R.styleable.TitleToolbar,
            defStyleAttr, R.style.Widget_AppCompat_TitleToolbar
        )

        displayShowTitleEnabled =
            ta.getBoolean(R.styleable.TitleToolbar_displayShowTitleEnabled, true)
        navigationIconTint = ta.getColorStateList(R.styleable.TitleToolbar_navigationIconTint)
        navigationIconTintMode = DrawableUtils.parseTintMode(
            ta.getInt(R.styleable.TitleToolbar_navigationIconTintMode, 0),
            PorterDuff.Mode.SRC_ATOP
        )

        ta.recycle()
    }

}

var TitleToolbar.titleTextColor: Int
    @Deprecated(NO_GETTER_MESSAGE) get() = NO_GETTER
    set(value) {
        setTitleTextColor(value)
    }

var TitleToolbar.subtitleTextColor: Int
    @Deprecated(NO_GETTER_MESSAGE) get() = NO_GETTER
    set(value) {
        setSubtitleTextColor(value)
    }

var TitleToolbar.titleTextAppearance: Int
    @Deprecated(NO_GETTER_MESSAGE) get() = NO_GETTER
    set(value) {
        setTitleTextAppearance(context, value)
    }

var TitleToolbar.subtitleTextAppearance: Int
    @Deprecated(NO_GETTER_MESSAGE) get() = NO_GETTER
    set(value) {
        setSubtitleTextAppearance(context, value)
    }

var TitleToolbar.navigationIconTint: Int
    @Deprecated(NO_GETTER_MESSAGE) get() = NO_GETTER
    set(value) {
        setNavigationIconTint(value)
    }

var TitleToolbar.navigationIconTintList: ColorStateList?
    get() = getNavigationIconTintList()
    set(value) {
        setNavigationIconTintList(value)
    }

var TitleToolbar.navigationIconTintMode: PorterDuff.Mode?
    get() = getNavigationIconTintMode()
    set(value) {
        setNavigationIconTintMode(value)
    }

var TitleToolbar.displayShowTitleEnabled: Boolean
    get() = isDisplayShowTitleEnabled()
    set(value) {
        setDisplayShowTitleEnabled(value)
    }