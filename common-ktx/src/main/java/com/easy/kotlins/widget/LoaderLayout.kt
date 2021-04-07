@file:Suppress("unused")

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
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.easy.kotlins.R
import com.easy.kotlins.helper.weak
import com.easy.kotlins.widget.LoaderView.Companion.TYPE_CONTENT_VIEW
import com.easy.kotlins.widget.LoaderView.Companion.TYPE_EMPTY_VIEW
import com.easy.kotlins.widget.LoaderView.Companion.TYPE_ERROR_VIEW
import com.easy.kotlins.widget.LoaderView.Companion.TYPE_LOADING_VIEW

class LoaderLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.LoaderLayoutStyle
) : FrameLayout(context, attrs, defStyleAttr), LoaderView {

    private var emptyLayoutId: Int
    private var loadingLayoutId: Int
    private var errorLayoutId: Int

    private var emptyImage: Drawable?
    private var emptyText: CharSequence?
    private var emptyTextColor: Int
    private var emptyTextAppearance: Int
    private var emptyButtonBackground: Drawable?
    private var emptyButtonText: CharSequence?
    private var emptyButtonTextColor: Int
    private var emptyButtonTextAppearance: Int
    private var emptyButtonVisible: Boolean

    private var errorImage: Drawable?
    private var errorText: CharSequence?
    private var errorTextColor: Int
    private var errorTextAppearance: Int
    private var errorButtonBackground: Drawable?
    private var errorButtonText: CharSequence?
    private var errorButtonTextColor: Int
    private var errorButtonTextAppearance: Int
    private var errorButtonVisible: Boolean

    private var loadingProgressColor: Int
    private var loadingProgressDrawable: Drawable?
    private var loadingText: CharSequence?
    private var loadingTextColor: Int
    private var loadingTextAppearance: Int

    private var defaultViewType: Int

    private var errorActionListener: LoaderView.OnActionListener? = null
    private val errorButtonClickListener = OnClickListener { _ ->
        if (errorActionListener != null) {
            errorActionListener!!.onAction(this)
        }
    }

    private var emptyActionListener: LoaderView.OnActionListener? = null
    private val emptyButtonClickListener = OnClickListener { _ ->
        if (emptyActionListener != null) {
            emptyActionListener!!.onAction(this)
        }
    }

    private var dataObservers: MutableMap<RecyclerView.Adapter<*>, AdapterDataObserver>? = null

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private val views = mutableMapOf<Int, View>()

    private val viewTypeLock = Any()
    private var viewType: Int = NO_TYPE
    private var pendingViewType = NO_TYPE
    private var isViewTypeChanged = false
    private val showRunnable = Runnable {
        var newViewType: Int
        synchronized(viewTypeLock) {
            newViewType = pendingViewType
            pendingViewType = NO_TYPE
        }
        showImmediately(newViewType)
    }

    init {
        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.LoaderLayout,
            defStyleAttr,
            R.style.LoaderLayout_Style
        )
        emptyLayoutId = a.getResourceId(R.styleable.LoaderLayout_emptyLayout, NO_VALUE)
        loadingLayoutId = a.getResourceId(R.styleable.LoaderLayout_loadingLayout, NO_VALUE)
        errorLayoutId = a.getResourceId(R.styleable.LoaderLayout_errorLayout, NO_VALUE)
        emptyImage = a.getDrawable(R.styleable.LoaderLayout_emptyImage)
        emptyText = a.getText(R.styleable.LoaderLayout_emptyText)
        emptyTextColor = a.getColor(R.styleable.LoaderLayout_emptyTextColor, NO_VALUE)
        emptyTextAppearance =
            a.getResourceId(R.styleable.LoaderLayout_emptyTextAppearance, NO_VALUE)
        emptyButtonText = a.getText(R.styleable.LoaderLayout_emptyButtonText)
        emptyButtonTextColor = a.getColor(R.styleable.LoaderLayout_emptyButtonTextColor, NO_VALUE)
        emptyButtonTextAppearance =
            a.getResourceId(R.styleable.LoaderLayout_emptyButtonTextAppearance, NO_VALUE)
        emptyButtonBackground = a.getDrawable(R.styleable.LoaderLayout_emptyButtonBackground)
        if (emptyButtonBackground == null) {
            val color = a.getColor(R.styleable.LoaderLayout_emptyButtonBackground, NO_VALUE)
            if (color != NO_VALUE) {
                emptyButtonBackground = ColorDrawable(color)
            }
        }
        emptyButtonVisible = a.getBoolean(R.styleable.LoaderLayout_emptyButtonVisible, false)

        errorImage = a.getDrawable(R.styleable.LoaderLayout_errorImage)
        errorText = a.getText(R.styleable.LoaderLayout_errorText)
        errorTextColor = a.getColor(R.styleable.LoaderLayout_errorTextColor, NO_VALUE)
        errorTextAppearance =
            a.getResourceId(R.styleable.LoaderLayout_errorTextAppearance, NO_VALUE)
        errorButtonText = a.getText(R.styleable.LoaderLayout_errorButtonText)
        errorButtonTextColor = a.getColor(R.styleable.LoaderLayout_errorButtonTextColor, NO_VALUE)
        errorButtonTextAppearance =
            a.getResourceId(R.styleable.LoaderLayout_errorButtonTextAppearance, NO_VALUE)
        errorButtonBackground = a.getDrawable(R.styleable.LoaderLayout_errorButtonBackground)
        if (errorButtonBackground == null) {
            val color = a.getColor(R.styleable.LoaderLayout_errorButtonBackground, NO_VALUE)
            if (color != NO_VALUE) {
                errorButtonBackground = ColorDrawable(color)
            }
        }
        errorButtonVisible = a.getBoolean(R.styleable.LoaderLayout_errorButtonVisible, true)

        loadingProgressColor = a.getColor(R.styleable.LoaderLayout_loadingProgressColor, NO_VALUE)
        loadingProgressDrawable = a.getDrawable(R.styleable.LoaderLayout_loadingProgressDrawable)
        loadingText = a.getText(R.styleable.LoaderLayout_loadingText)
        loadingTextColor = a.getColor(R.styleable.LoaderLayout_loadingTextColor, NO_VALUE)
        loadingTextAppearance =
            a.getResourceId(R.styleable.LoaderLayout_loadingTextAppearance, NO_VALUE)

        defaultViewType = a.getInt(R.styleable.LoaderLayout_defaultShow, TYPE_LOADING_VIEW)
        a.recycle()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        check(childCount <= 1) {
            "DynamicLayout can host only one direct child in layout"
        }

        if (childCount == 1) {
            setContentView(getChildAt(0))
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        show(defaultViewType)
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

    override fun setContentView(layoutResId: Int): LoaderView {
        setView(TYPE_CONTENT_VIEW, layoutResId, true)
        return this
    }

    override fun setContentView(view: View): LoaderView {
        setView(TYPE_CONTENT_VIEW, view, true)
        return this
    }

    override fun setLoadingView(layoutResId: Int): LoaderView {
        setView(TYPE_LOADING_VIEW, layoutResId)
        return this
    }

    override fun setLoadingView(view: View): LoaderView {
        setView(TYPE_LOADING_VIEW, view)
        return this
    }

    override fun setEmptyView(layoutResId: Int): LoaderView {
        setView(TYPE_EMPTY_VIEW, layoutResId)
        return this
    }

    override fun setEmptyView(view: View): LoaderLayout {
        setView(TYPE_EMPTY_VIEW, view)
        return this
    }

    override fun setErrorView(layoutResId: Int): LoaderView {
        setView(TYPE_ERROR_VIEW, layoutResId)
        return this
    }

    override fun setErrorView(view: View): LoaderView {
        setView(TYPE_ERROR_VIEW, view)
        return this
    }

    override fun setDefaultView(viewType: Int): LoaderView {
        check(
            viewType == TYPE_CONTENT_VIEW
                    || viewType == TYPE_LOADING_VIEW
                    || viewType == TYPE_EMPTY_VIEW
                    || viewType == TYPE_ERROR_VIEW
        ) {
            "Non-supported view type: $viewType"
        }
        defaultViewType = viewType
        return this
    }

    override fun setEmptyImage(drawable: Drawable?): LoaderView {
        emptyImage = drawable
        findViewById<ImageView>(R.id.empty_image)?.setImageDrawable(drawable)
        return this
    }

    override fun setEmptyImage(drawableId: Int): LoaderView {
        return setEmptyImage(ContextCompat.getDrawable(context, drawableId))
    }

    override fun setEmptyText(text: CharSequence): LoaderView {
        emptyText = text
        findViewById<TextView>(R.id.empty_text)?.text = text
        return this
    }

    override fun setEmptyText(textId: Int): LoaderView {
        return setEmptyText(context.getText(textId))
    }

    override fun setEmptyButtonText(text: CharSequence): LoaderView {
        emptyButtonText = text
        findViewById<TextView>(R.id.empty_button)?.let {
            it.text = text
            it.visibility = if (text.isBlank()) GONE else VISIBLE
        }
        return this
    }

    override fun setEmptyButtonText(textId: Int): LoaderView {
        return setEmptyButtonText(context.getText(textId))
    }

    override fun setEmptyButtonVisible(visible: Boolean): LoaderView {
        emptyButtonVisible = visible
        findViewById<TextView>(R.id.empty_button)?.let {
            it.visibility = if (visible) VISIBLE else GONE
        }
        return this
    }

    override fun setOnEmptyActionListener(listener: LoaderView.OnActionListener): LoaderView {
        emptyActionListener = listener
        return this
    }

    override fun setLoadingText(text: CharSequence): LoaderView {
        loadingText = text
        findViewById<TextView>(R.id.loading_text)?.text = text
        return this
    }

    override fun setLoadingText(textId: Int): LoaderView {
        return setLoadingText(context.getText(textId))
    }

    override fun setErrorImage(drawable: Drawable?): LoaderView {
        errorImage = drawable
        findViewById<ImageView>(R.id.error_image)?.setImageDrawable(drawable)
        return this
    }

    override fun setErrorImage(drawableId: Int): LoaderView {
        return setErrorImage(ContextCompat.getDrawable(context, drawableId))
    }

    override fun setErrorText(text: CharSequence): LoaderView {
        emptyText = text
        findViewById<TextView>(R.id.error_text)?.text = text
        return this
    }

    override fun setErrorText(textId: Int): LoaderView {
        return setEmptyText(context.getText(textId))
    }

    override fun setErrorButtonText(text: CharSequence): LoaderView {
        errorButtonText = text
        findViewById<TextView>(R.id.error_button)?.let {
            it.text = text
            it.visibility = if (text.isBlank()) GONE else VISIBLE
        }
        return this
    }

    override fun setErrorButtonText(textId: Int): LoaderView {
        return setErrorButtonText(context.getText(textId))
    }

    override fun setErrorButtonVisible(visible: Boolean): LoaderView {
        errorButtonVisible = visible
        findViewById<TextView>(R.id.error_button)?.let {
            it.visibility = if (visible) VISIBLE else GONE
        }
        return this
    }

    override fun setOnErrorActionListener(listener: LoaderView.OnActionListener): LoaderView {
        errorActionListener = listener
        return this
    }

    override fun attachTo(adapter: RecyclerView.Adapter<*>): LoaderView {
        if (dataObservers == null) {
            dataObservers = mutableMapOf()
        }
        dataObservers!!.getOrPut(adapter) {
            AdapterDataObserver(adapter) { this }
        }.register()
        return this
    }

    override fun detachTo(adapter: RecyclerView.Adapter<*>): LoaderView {
        dataObservers?.get(adapter)?.unregister()
        return this
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        dataObservers?.forEach {
            it.value.unregister()
        }
    }

    private fun showImmediately(viewType: Int) {
        if (viewType == NO_TYPE || this.viewType == viewType) {
            return
        }

        this.viewType = viewType

        for ((type, view) in views) {
            if (type == viewType) continue

            view.visibility = INVISIBLE
        }

        views.getOrElse(viewType) {
            when (viewType) {
                TYPE_EMPTY_VIEW -> setView(TYPE_EMPTY_VIEW, emptyLayoutId, true)
                TYPE_LOADING_VIEW -> setView(TYPE_LOADING_VIEW, loadingLayoutId, true)
                TYPE_ERROR_VIEW -> setView(TYPE_ERROR_VIEW, errorLayoutId, true)
                else -> null
            }
        }?.visibility = VISIBLE
    }

    private fun show(viewType: Int) {
        var postTask: Boolean
        synchronized(viewTypeLock) {
            postTask = viewType == NO_TYPE
            pendingViewType = viewType
        }
        if (!postTask) {
            return
        }
        post(showRunnable)
    }

    private fun setView(
        viewType: Int,
        layoutResId: Int,
        preventAddView: Boolean = false
    ): View {
        return setView(viewType, inflater.inflate(layoutResId, this, false), preventAddView)
    }

    private fun setView(viewType: Int, view: View, preventAddView: Boolean = false): View {
        when (viewType) {
            TYPE_LOADING_VIEW -> {
                val progressBar = view.findViewById<ProgressBar>(R.id.loading_progress)
                val textView = view.findViewById<TextView>(R.id.loading_text)
                progressBar?.let {
                    if (loadingProgressDrawable != null) {
                        it.indeterminateDrawable = loadingProgressDrawable
                    } else if (Build.VERSION.SDK_INT >= 21 && loadingProgressColor != NO_VALUE) {
                        it.indeterminateTintMode = PorterDuff.Mode.SRC_ATOP
                        it.indeterminateTintList =
                            ColorStateList.valueOf(loadingProgressColor)
                    }
                }
                textView?.let {
                    it.text = loadingText
                    if (loadingTextAppearance != NO_VALUE) {
                        TextViewCompat.setTextAppearance(it, loadingTextAppearance)
                    }
                    if (loadingTextColor != NO_VALUE) {
                        it.setTextColor(loadingTextColor)
                    }
                    it.visibility = if (loadingText.isNullOrBlank()) GONE else VISIBLE
                }
            }
            TYPE_EMPTY_VIEW -> {
                val imageView = view.findViewById<ImageView>(R.id.empty_image)
                val textView = view.findViewById<TextView>(R.id.empty_text)
                val emptyButton = view.findViewById<Button>(R.id.empty_button)
                imageView?.setImageDrawable(this.emptyImage)
                textView?.let {
                    it.text = emptyText
                    if (emptyTextAppearance != NO_VALUE) {
                        TextViewCompat.setTextAppearance(it, emptyTextAppearance)
                    }
                    if (emptyTextColor != NO_VALUE) {
                        it.setTextColor(emptyTextColor)
                    }
                }
                emptyButton?.let {
                    it.text = emptyButtonText
                    it.background = emptyButtonBackground
                    if (emptyButtonTextAppearance != NO_VALUE) {
                        TextViewCompat.setTextAppearance(it, emptyButtonTextAppearance)
                    }
                    if (emptyButtonTextColor != NO_VALUE) {
                        it.setTextColor(emptyButtonTextColor)
                    }
                    it.setOnClickListener(emptyButtonClickListener)
                    it.visibility = if (emptyButtonVisible) VISIBLE else GONE
                }
            }
            TYPE_ERROR_VIEW -> {
                val imageView = view.findViewById<ImageView>(R.id.error_image)
                val textView = view.findViewById<TextView>(R.id.error_text)
                val errorButton = view.findViewById<Button>(R.id.error_button)
                imageView?.setImageDrawable(this.errorImage)
                textView?.let {
                    it.text = errorText
                    if (errorTextAppearance != NO_VALUE) {
                        TextViewCompat.setTextAppearance(it, errorTextAppearance)
                    }
                    if (errorTextColor != NO_VALUE) {
                        it.setTextColor(errorTextColor)
                    }
                }
                errorButton?.let {
                    it.text = errorButtonText
                    it.background = errorButtonBackground
                    if (errorButtonTextAppearance != NO_VALUE) {
                        TextViewCompat.setTextAppearance(it, errorButtonTextAppearance)
                    }
                    if (errorButtonTextColor != NO_VALUE) {
                        it.setTextColor(errorButtonTextColor)
                    }
                    it.setOnClickListener(errorButtonClickListener)
                    it.visibility = if (errorButtonVisible) VISIBLE else GONE
                }
            }
        }

        val existingView = views.put(viewType, view)
        if (indexOfChild(existingView) > -1) {
            removeView(existingView)
        }

        view.visibility = if (this.viewType == viewType) VISIBLE else INVISIBLE

        if (!preventAddView || indexOfChild(view) > -1) {
            return view
        }

        if (viewType == TYPE_CONTENT_VIEW) {
            val params = view.layoutParams
            addView(view, 0, if (params is MarginLayoutParams) {
                MarginLayoutParams(params).apply {
                    width = LayoutParams.MATCH_PARENT
                    height = LayoutParams.MATCH_PARENT
                }
            } else {
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            })
        } else {
            addView(view)
        }

        return view
    }

    private class AdapterDataObserver(
        val adapter: RecyclerView.Adapter<*>,
        loaderLayout: () -> LoaderLayout
    ) : RecyclerView.AdapterDataObserver() {

        private val layout: LoaderLayout? by weak(loaderLayout)

        private var registed: Boolean = false

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
        fun wrap(activity: Activity): LoaderLayout {
            return wrap(
                (activity.findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0)
            )
        }

        fun wrap(fragment: Fragment): LoaderLayout {
            return wrap(requireNotNull(fragment.view))
        }

        fun wrap(view: View): LoaderLayout {
            val parent = view.parent as ViewGroup
            val params = view.layoutParams
            val index = parent.indexOfChild(view)
            parent.removeView(view)
            val layout = LoaderLayout(view.context)
            layout.setContentView(view)
            parent.addView(layout, index, params)
            return layout
        }

        private const val NO_VALUE = -1
        private const val NO_TYPE = 0
    }

}