package com.easy.kotlins.widget

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.annotation.IntDef
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.easy.kotlins.R
import com.easy.kotlins.helper.weak

class DynamicLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.dynamicLayoutStyle
) : FrameLayout(context, attrs, defStyleAttr), LoadingView {
    @IntDef(TYPE_CONTENT_VIEW, TYPE_EMPTY_VIEW, TYPE_LOADING_VIEW, TYPE_ERROR_VIEW)
    private annotation class ViewType

    private val views = mutableMapOf<Int, View>()

    @ViewType
    private var viewType = 0
    private var emptyLayoutId: Int
    private var loadingLayoutId: Int
    private var errorLayoutId: Int

    private var emptyImage: Drawable?
    private var emptyText: CharSequence
    private var emptyTextColor: Int
    private var emptyTextAppearance: Int
    private var emptyButtonBackground: Drawable?
    private var emptyButtonText: CharSequence?
    private var emptyButtonTextColor: Int
    private var emptyButtonTextAppearance: Int
    private var emptyButtonVisible: Boolean

    private var errorImage: Drawable?
    private var errorText: CharSequence
    private var errorTextColor: Int
    private var errorTextAppearance: Int
    private var errorButtonBackground: Drawable?
    private var errorButtonText: CharSequence?
    private var errorButtonTextColor: Int
    private var errorButtonTextAppearance: Int
    private var errorButtonVisible: Boolean

    private var loadingProgressColor: Int
    private var loadingProgressDrawable: Drawable?
    private var loadingText: CharSequence
    private var loadingTextColor: Int
    private var loadingTextAppearance: Int

    private val defaultShowType: Int

    private var errorActionListener: OnActionListener? = null
    private val errorButtonClickListener = OnClickListener { _ ->
        if (errorActionListener != null) {
            errorActionListener!!.onAction(this)
        }
    }

    private var emptyActionListener: OnActionListener? = null
    private val emptyButtonClickListener = OnClickListener { _ ->
        if (emptyActionListener != null) {
            emptyActionListener!!.onAction(this)
        }
    }

    private var dataObservers: MutableMap<RecyclerView.Adapter<*>, AdapterDataObserver>? = null

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onFinishInflate() {
        super.onFinishInflate()
        check(childCount <= 1) { "DynamicLayout can host only one direct child in layout" }

        if (childCount == 1) {
            addView(TYPE_CONTENT_VIEW, getChildAt(0))
        }

        if(!isInEditMode){
            show(defaultShowType)
        }
    }

    override fun showLoading() {
        show(TYPE_LOADING_VIEW)
    }

    override fun showEmpty() {
        show(TYPE_EMPTY_VIEW)
    }

    override fun showError() {
        show(TYPE_ERROR_VIEW)
    }

    override fun showContent() {
        show(TYPE_CONTENT_VIEW)
    }

    override fun setContentView(layoutResId: Int): LoadingView {
        addView(TYPE_CONTENT_VIEW, layoutResId, viewType == TYPE_CONTENT_VIEW)
        return this
    }

    override fun setContentView(view: View): LoadingView {
        addView(TYPE_CONTENT_VIEW, view, viewType == TYPE_CONTENT_VIEW)
        return this
    }

    override fun setLoadingView(@LayoutRes layoutResId: Int): LoadingView {
        addView(TYPE_LOADING_VIEW, layoutResId, viewType == TYPE_LOADING_VIEW)
        return this
    }

    override fun setLoadingView(view: View): LoadingView {
        addView(TYPE_LOADING_VIEW, view, viewType == TYPE_LOADING_VIEW)
        return this
    }

    override fun setEmptyView(@LayoutRes layoutResId: Int): LoadingView {
        addView(TYPE_EMPTY_VIEW, layoutResId, viewType == TYPE_EMPTY_VIEW)
        return this
    }

    override fun setEmptyView(view: View): DynamicLayout {
        addView(TYPE_EMPTY_VIEW, view, viewType == TYPE_EMPTY_VIEW)
        return this
    }

    override fun setErrorView(@LayoutRes layoutResId: Int): LoadingView {
        addView(TYPE_ERROR_VIEW, layoutResId, viewType == TYPE_ERROR_VIEW)
        return this
    }

    override fun setErrorView(view: View): LoadingView {
        addView(TYPE_ERROR_VIEW, view, viewType == TYPE_ERROR_VIEW)
        return this
    }

    override fun setEmptyImage(drawable: Drawable?): LoadingView {
        emptyImage = drawable
        findViewById<ImageView>(R.id.empty_image)?.setImageDrawable(drawable)
        return this
    }

    override fun setEmptyImage(@DrawableRes drawableId: Int): LoadingView {
        return setEmptyImage(ContextCompat.getDrawable(context, drawableId))
    }

    override fun setEmptyText(text: CharSequence): LoadingView {
        emptyText = text
        findViewById<TextView>(R.id.empty_text)?.text = text
        return this
    }

    override fun setEmptyText(@StringRes textId: Int): LoadingView {
        return setEmptyText(context.getText(textId))
    }

    override fun setEmptyButtonText(text: CharSequence): LoadingView {
        emptyButtonText = text
        findViewById<TextView>(R.id.empty_button)?.let {
            it.text = text
            it.visibility = if (text.isBlank()) GONE else VISIBLE
        }
        return this
    }

    override fun setEmptyButtonText(@StringRes textId: Int): LoadingView {
        return setEmptyButtonText(context.getText(textId))
    }

    override fun setEmptyButtonVisible(visible: Boolean): LoadingView {
        emptyButtonVisible = visible
        findViewById<TextView>(R.id.empty_button)?.apply {
            visibility = if (visible) VISIBLE else GONE
        }
        return this
    }

    override fun setEmptyActionListener(listener: OnActionListener): LoadingView {
        emptyActionListener = listener
        return this
    }

    override fun setLoadingText(text: CharSequence): LoadingView {
        loadingText = text
        findViewById<TextView>(R.id.loading_text)?.text = text
        return this
    }

    override fun setLoadingText(@StringRes textId: Int): LoadingView {
        return setLoadingText(context.getText(textId))
    }

    override fun setErrorImage(drawable: Drawable?): LoadingView {
        errorImage = drawable
        findViewById<ImageView>(R.id.error_image)?.setImageDrawable(drawable)
        return this
    }

    override fun setErrorImage(@DrawableRes drawableId: Int): LoadingView {
        return setErrorImage(ContextCompat.getDrawable(context, drawableId))
    }

    override fun setErrorText(text: CharSequence): LoadingView {
        emptyText = text
        findViewById<TextView>(R.id.error_text)?.text = text
        return this
    }

    override fun setErrorText(@StringRes textId: Int): LoadingView {
        return setEmptyText(context.getText(textId))
    }

    override fun setErrorButtonText(text: CharSequence): LoadingView {
        errorButtonText = text
        findViewById<TextView>(R.id.error_button)?.let {
            it.text = text
            it.visibility = if (text.isBlank()) GONE else VISIBLE
        }
        return this
    }

    override fun setErrorButtonText(@StringRes textId: Int): LoadingView {
        return setErrorButtonText(context.getText(textId))
    }

    override fun setErrorButtonVisible(visible: Boolean): LoadingView {
        errorButtonVisible = visible
        findViewById<TextView>(R.id.error_button)?.apply {
            visibility = if (visible) VISIBLE else GONE
        }
        return this
    }

    override fun setErrorActionListener(listener: OnActionListener): LoadingView {
        errorActionListener = listener
        return this
    }

    override fun attachTo(adapter: RecyclerView.Adapter<*>): LoadingView {
        if (dataObservers == null) {
            dataObservers = mutableMapOf()
        }
        dataObservers!!.getOrPut(adapter) {
            AdapterDataObserver(adapter) { this }
        }.register()
        return this
    }

    override fun detachTo(adapter: RecyclerView.Adapter<*>): LoadingView {
        dataObservers?.get(adapter)?.unregister()
        return this
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        dataObservers?.forEach {
            it.value.unregister()
        }
    }

    private fun show(@ViewType viewType: Int) {
        if (this.viewType == viewType) return

        this.viewType = viewType

        for ((type, view) in views) {
            if (type == viewType) continue

            view.visibility = INVISIBLE
        }

        views.getOrElse(viewType) {
            when (viewType) {
                TYPE_EMPTY_VIEW -> addView(TYPE_EMPTY_VIEW, emptyLayoutId, true)
                TYPE_LOADING_VIEW -> addView(TYPE_LOADING_VIEW, loadingLayoutId, true)
                TYPE_ERROR_VIEW -> addView(TYPE_ERROR_VIEW, errorLayoutId, true)
                TYPE_CONTENT_VIEW -> error("Content view must not be null")
                else -> error("Can not find view by key: $viewType")
            }
        }.visibility = VISIBLE
    }

    private fun addView(
        @ViewType viewType: Int,
        @LayoutRes layoutResId: Int,
        attach: Boolean = false
    ): View {
        return addView(viewType, inflater.inflate(layoutResId, this, false), attach)
    }

    private fun addView(@ViewType viewType: Int, view: View, attach: Boolean = false): View {
        views[viewType] = view.also {
            it.visibility = if (viewType == viewType) VISIBLE else INVISIBLE
        }
        when (viewType) {
            TYPE_EMPTY_VIEW -> {
                val imageView = view.findViewById<ImageView>(R.id.empty_image)
                val textView = view.findViewById<TextView>(R.id.empty_text)
                val emptyButton = view.findViewById<Button>(R.id.empty_button)
                imageView?.setImageDrawable(this.emptyImage)
                textView?.let {
                    it.text = emptyText
                    if (emptyTextAppearance != NO_RESOURCE_ID) {
                        TextViewCompat.setTextAppearance(it, emptyTextAppearance)
                    }
                    if (emptyTextColor != NO_COLOR) {
                        it.setTextColor(emptyTextColor)
                    }
                }
                emptyButton?.let {
                    it.background = emptyButtonBackground
                    it.text = emptyButtonText
                    if (emptyButtonTextAppearance != NO_RESOURCE_ID) {
                        TextViewCompat.setTextAppearance(it, emptyButtonTextAppearance)
                    }
                    if (emptyButtonTextColor != NO_COLOR) {
                        it.setTextColor(emptyButtonTextColor)
                    }
                    it.setOnClickListener(emptyButtonClickListener)
                    it.visibility = if (emptyButtonVisible) VISIBLE else GONE
                }
            }
            TYPE_LOADING_VIEW -> {
                val progressBar = view.findViewById<ProgressBar>(R.id.loading_progress)
                val textView = view.findViewById<TextView>(R.id.loading_text)
                progressBar?.let {
                    if (loadingProgressDrawable != null) {
                        it.indeterminateDrawable = loadingProgressDrawable
                    } else if (Build.VERSION.SDK_INT >= 21 && loadingProgressColor != NO_COLOR) {
                        it.indeterminateTintMode = PorterDuff.Mode.SRC_ATOP
                        it.indeterminateTintList =
                            ColorStateList.valueOf(loadingProgressColor)
                    }
                }
                textView?.let {
                    it.text = loadingText
                    if (loadingTextAppearance != NO_RESOURCE_ID) {
                        TextViewCompat.setTextAppearance(it, loadingTextAppearance)
                    }
                    if (loadingTextColor != NO_COLOR) {
                        it.setTextColor(loadingTextColor)
                    }
                }
            }
            TYPE_ERROR_VIEW -> {
                val imageView = view.findViewById<ImageView>(R.id.error_image)
                val textView = view.findViewById<TextView>(R.id.error_text)
                val errorButton = view.findViewById<Button>(R.id.error_button)
                imageView?.setImageDrawable(this.errorImage)
                textView?.let {
                    it.text = errorText
                    if (errorTextAppearance != NO_RESOURCE_ID) {
                        TextViewCompat.setTextAppearance(it, errorTextAppearance)
                    }
                    if (errorTextColor != NO_COLOR) {
                        it.setTextColor(errorTextColor)
                    }
                }
                errorButton?.let {
                    it.background = errorButtonBackground
                    it.text = errorButtonText
                    if (errorButtonTextAppearance != NO_RESOURCE_ID) {
                        TextViewCompat.setTextAppearance(it, errorButtonTextAppearance)
                    }
                    if (errorButtonTextColor != NO_COLOR) {
                        it.setTextColor(errorButtonTextColor)
                    }
                    it.setOnClickListener(errorButtonClickListener)
                    it.visibility = if (errorButtonVisible) VISIBLE else GONE
                }
            }
        }

        if (attach && indexOfChild(view) < 0) {
            if (viewType == TYPE_CONTENT_VIEW) {
                addView(view, 0, view.layoutParams ?: LayoutParams(-1, -1))
            } else {
                addView(view)
            }
        }

        return view
    }

    private class AdapterDataObserver(
        internal val adapter: RecyclerView.Adapter<*>,
        dynamicLayout: () -> DynamicLayout
    ) : RecyclerView.AdapterDataObserver() {

        private var registed: Boolean = false

        private val layout: DynamicLayout? by weak(dynamicLayout)

        override fun onChanged() {
            if (adapter.itemCount == 0) {
                layout?.showEmpty()
            } else {
                layout?.showContent()
            }
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            if (adapter.itemCount > 0) {
                layout?.showContent()
            }
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            if (adapter.itemCount == 0) {
                layout?.showEmpty()
            }
        }

        fun register() {
            if (registed) {
                return
            }

            adapter.registerAdapterDataObserver(this)
            registed = true
        }

        fun unregister() {
            if (!registed) {
                return
            }

            adapter.unregisterAdapterDataObserver(this)
            registed = false
        }
    }

    companion object {
        fun wrap(activity: Activity): DynamicLayout {
            return wrap(
                (activity.findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(
                    0
                )
            )
        }

        fun wrap(fragment: Fragment): DynamicLayout {
            return wrap(requireNotNull(fragment.view))
        }

        fun wrap(view: View): DynamicLayout {
            val parent = view.parent as ViewGroup
            check(parent !is ViewPager) { "parent view can not be ViewPager" }
            val lp = view.layoutParams
            val index = parent.indexOfChild(view)
            parent.removeView(view)
            val layout = DynamicLayout(view.context)
            layout.setContentView(view)
            parent.addView(layout, index, lp)
            return layout
        }

        private const val TYPE_CONTENT_VIEW = 0x001
        private const val TYPE_EMPTY_VIEW = 0x002
        private const val TYPE_LOADING_VIEW = 0x003
        private const val TYPE_ERROR_VIEW = 0x004

        private const val NO_RESOURCE_ID = -1
        private const val NO_COLOR = -1
    }

    init {
        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.DynamicLayout,
            defStyleAttr,
            R.style.DynamicLayout_Style
        )
        emptyLayoutId = a.getResourceId(R.styleable.DynamicLayout_emptyLayout, NO_RESOURCE_ID)
        loadingLayoutId = a.getResourceId(R.styleable.DynamicLayout_loadingLayout, NO_RESOURCE_ID)
        errorLayoutId = a.getResourceId(R.styleable.DynamicLayout_errorLayout, NO_RESOURCE_ID)
        emptyImage = a.getDrawable(R.styleable.DynamicLayout_emptyImage)
        emptyText = a.getText(R.styleable.DynamicLayout_emptyText)
        emptyTextColor = a.getColor(R.styleable.DynamicLayout_emptyTextColor, NO_COLOR)
        emptyTextAppearance =
            a.getResourceId(R.styleable.DynamicLayout_emptyTextAppearance, NO_RESOURCE_ID)
        emptyButtonText = a.getText(R.styleable.DynamicLayout_emptyButtonText)
        emptyButtonTextColor = a.getColor(R.styleable.DynamicLayout_emptyButtonTextColor, NO_COLOR)
        emptyButtonTextAppearance =
            a.getResourceId(R.styleable.DynamicLayout_emptyButtonTextAppearance, NO_RESOURCE_ID)
        emptyButtonBackground = a.getDrawable(R.styleable.DynamicLayout_emptyButtonBackground)
        if (emptyButtonBackground == null) {
            val color = a.getColor(R.styleable.DynamicLayout_emptyButtonBackground, NO_COLOR)
            if (color != NO_COLOR) {
                emptyButtonBackground = ColorDrawable(color)
            }
        }
        emptyButtonVisible = a.getBoolean(R.styleable.DynamicLayout_emptyButtonVisible, true)

        errorImage = a.getDrawable(R.styleable.DynamicLayout_errorImage)
        errorText = a.getText(R.styleable.DynamicLayout_errorText)
        errorTextColor = a.getColor(R.styleable.DynamicLayout_errorTextColor, NO_COLOR)
        errorTextAppearance =
            a.getResourceId(R.styleable.DynamicLayout_errorTextAppearance, NO_RESOURCE_ID)
        errorButtonText = a.getText(R.styleable.DynamicLayout_errorButtonText)
        errorButtonTextColor = a.getColor(R.styleable.DynamicLayout_errorButtonTextColor, NO_COLOR)
        errorButtonTextAppearance =
            a.getResourceId(R.styleable.DynamicLayout_errorButtonTextAppearance, NO_RESOURCE_ID)
        errorButtonBackground = a.getDrawable(R.styleable.DynamicLayout_errorButtonBackground)
        if (errorButtonBackground == null) {
            val color = a.getColor(R.styleable.DynamicLayout_errorButtonBackground, NO_COLOR)
            if (color != NO_COLOR) {
                errorButtonBackground = ColorDrawable(color)
            }
        }
        errorButtonVisible = a.getBoolean(R.styleable.DynamicLayout_errorButtonVisible, true)


        loadingProgressColor = a.getColor(R.styleable.DynamicLayout_loadingProgressColor, NO_COLOR)
        loadingProgressDrawable = a.getDrawable(R.styleable.DynamicLayout_loadingProgressDrawable)
        loadingText = a.getText(R.styleable.DynamicLayout_loadingText)
        loadingTextColor = a.getColor(R.styleable.DynamicLayout_loadingTextColor, NO_COLOR)
        loadingTextAppearance =
            a.getResourceId(R.styleable.DynamicLayout_loadingTextAppearance, NO_RESOURCE_ID)

        defaultShowType = a.getInt(R.styleable.DynamicLayout_defaultShow, TYPE_LOADING_VIEW)
        a.recycle()
    }
}