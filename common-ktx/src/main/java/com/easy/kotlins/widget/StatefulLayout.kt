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
import com.easy.kotlins.widget.StatefulView.Companion.TYPE_CONTENT_VIEW
import com.easy.kotlins.widget.StatefulView.Companion.TYPE_EMPTY_VIEW
import com.easy.kotlins.widget.StatefulView.Companion.TYPE_ERROR_VIEW
import com.easy.kotlins.widget.StatefulView.Companion.TYPE_LOADING_VIEW

class StatefulLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.statefulLayoutStyle
) : FrameLayout(context, attrs, defStyleAttr), StatefulView {

    private val views = mutableMapOf<Int, View>()

    private var viewType: Int = 0
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

    private var defaultShowType: Int

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

    init {
        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.StatefulLayout,
            defStyleAttr,
            R.style.StatefulLayout_Style
        )
        emptyLayoutId = a.getResourceId(R.styleable.StatefulLayout_emptyLayout, NO_RESOURCE_ID)
        loadingLayoutId = a.getResourceId(R.styleable.StatefulLayout_loadingLayout, NO_RESOURCE_ID)
        errorLayoutId = a.getResourceId(R.styleable.StatefulLayout_errorLayout, NO_RESOURCE_ID)
        emptyImage = a.getDrawable(R.styleable.StatefulLayout_emptyImage)
        emptyText = a.getText(R.styleable.StatefulLayout_emptyText)
        emptyTextColor = a.getColor(R.styleable.StatefulLayout_emptyTextColor, NO_COLOR)
        emptyTextAppearance =
            a.getResourceId(R.styleable.StatefulLayout_emptyTextAppearance, NO_RESOURCE_ID)
        emptyButtonText = a.getText(R.styleable.StatefulLayout_emptyButtonText)
        emptyButtonTextColor = a.getColor(R.styleable.StatefulLayout_emptyButtonTextColor, NO_COLOR)
        emptyButtonTextAppearance =
            a.getResourceId(R.styleable.StatefulLayout_emptyButtonTextAppearance, NO_RESOURCE_ID)
        emptyButtonBackground = a.getDrawable(R.styleable.StatefulLayout_emptyButtonBackground)
        if (emptyButtonBackground == null) {
            val color = a.getColor(R.styleable.StatefulLayout_emptyButtonBackground, NO_COLOR)
            if (color != NO_COLOR) {
                emptyButtonBackground = ColorDrawable(color)
            }
        }
        emptyButtonVisible = a.getBoolean(R.styleable.StatefulLayout_emptyButtonVisible, false)

        errorImage = a.getDrawable(R.styleable.StatefulLayout_errorImage)
        errorText = a.getText(R.styleable.StatefulLayout_errorText)
        errorTextColor = a.getColor(R.styleable.StatefulLayout_errorTextColor, NO_COLOR)
        errorTextAppearance =
            a.getResourceId(R.styleable.StatefulLayout_errorTextAppearance, NO_RESOURCE_ID)
        errorButtonText = a.getText(R.styleable.StatefulLayout_errorButtonText)
        errorButtonTextColor = a.getColor(R.styleable.StatefulLayout_errorButtonTextColor, NO_COLOR)
        errorButtonTextAppearance =
            a.getResourceId(R.styleable.StatefulLayout_errorButtonTextAppearance, NO_RESOURCE_ID)
        errorButtonBackground = a.getDrawable(R.styleable.StatefulLayout_errorButtonBackground)
        if (errorButtonBackground == null) {
            val color = a.getColor(R.styleable.StatefulLayout_errorButtonBackground, NO_COLOR)
            if (color != NO_COLOR) {
                errorButtonBackground = ColorDrawable(color)
            }
        }
        errorButtonVisible = a.getBoolean(R.styleable.StatefulLayout_errorButtonVisible, true)

        loadingProgressColor = a.getColor(R.styleable.StatefulLayout_loadingProgressColor, NO_COLOR)
        loadingProgressDrawable = a.getDrawable(R.styleable.StatefulLayout_loadingProgressDrawable)
        loadingText = a.getText(R.styleable.StatefulLayout_loadingText)
        loadingTextColor = a.getColor(R.styleable.StatefulLayout_loadingTextColor, NO_COLOR)
        loadingTextAppearance =
            a.getResourceId(R.styleable.StatefulLayout_loadingTextAppearance, NO_RESOURCE_ID)

        defaultShowType = a.getInt(R.styleable.StatefulLayout_defaultShow, TYPE_LOADING_VIEW)
        a.recycle()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        check(childCount <= 1) { "DynamicLayout can host only one direct child in layout" }

        if (childCount == 1) {
            setContentView(getChildAt(0))
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        show(defaultShowType)
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

    override fun setContentView(layoutResId: Int): StatefulView {
        setView(TYPE_CONTENT_VIEW, layoutResId, true)
        return this
    }

    override fun setContentView(view: View): StatefulView {
        setView(TYPE_CONTENT_VIEW, view, true)
        return this
    }

    override fun setLoadingView(layoutResId: Int): StatefulView {
        setView(TYPE_LOADING_VIEW, layoutResId)
        return this
    }

    override fun setLoadingView(view: View): StatefulView {
        setView(TYPE_LOADING_VIEW, view)
        return this
    }

    override fun setEmptyView(layoutResId: Int): StatefulView {
        setView(TYPE_EMPTY_VIEW, layoutResId)
        return this
    }

    override fun setEmptyView(view: View): StatefulLayout {
        setView(TYPE_EMPTY_VIEW, view)
        return this
    }

    override fun setErrorView(layoutResId: Int): StatefulView {
        setView(TYPE_ERROR_VIEW, layoutResId)
        return this
    }

    override fun setErrorView(view: View): StatefulView {
        setView(TYPE_ERROR_VIEW, view)
        return this
    }

    override fun setDefaultView(viewType: Int): StatefulView {
        defaultShowType = viewType
        return this
    }

    override fun setEmptyImage(drawable: Drawable?): StatefulView {
        emptyImage = drawable
        findViewById<ImageView>(R.id.empty_image)?.setImageDrawable(drawable)
        return this
    }

    override fun setEmptyImage(drawableId: Int): StatefulView {
        return setEmptyImage(ContextCompat.getDrawable(context, drawableId))
    }

    override fun setEmptyText(text: CharSequence): StatefulView {
        emptyText = text
        findViewById<TextView>(R.id.empty_text)?.text = text
        return this
    }

    override fun setEmptyText(textId: Int): StatefulView {
        return setEmptyText(context.getText(textId))
    }

    override fun setEmptyButtonText(text: CharSequence): StatefulView {
        emptyButtonText = text
        findViewById<TextView>(R.id.empty_button)?.let {
            it.text = text
            it.visibility = if (text.isBlank()) GONE else VISIBLE
        }
        return this
    }

    override fun setEmptyButtonText(textId: Int): StatefulView {
        return setEmptyButtonText(context.getText(textId))
    }

    override fun setEmptyButtonVisible(visible: Boolean): StatefulView {
        emptyButtonVisible = visible
        findViewById<TextView>(R.id.empty_button)?.apply {
            visibility = if (visible) VISIBLE else GONE
        }
        return this
    }

    override fun setEmptyActionListener(listener: OnActionListener): StatefulView {
        emptyActionListener = listener
        return this
    }

    override fun setLoadingText(text: CharSequence): StatefulView {
        loadingText = text
        findViewById<TextView>(R.id.loading_text)?.text = text
        return this
    }

    override fun setLoadingText(textId: Int): StatefulView {
        return setLoadingText(context.getText(textId))
    }

    override fun setErrorImage(drawable: Drawable?): StatefulView {
        errorImage = drawable
        findViewById<ImageView>(R.id.error_image)?.setImageDrawable(drawable)
        return this
    }

    override fun setErrorImage(drawableId: Int): StatefulView {
        return setErrorImage(ContextCompat.getDrawable(context, drawableId))
    }

    override fun setErrorText(text: CharSequence): StatefulView {
        emptyText = text
        findViewById<TextView>(R.id.error_text)?.text = text
        return this
    }

    override fun setErrorText(textId: Int): StatefulView {
        return setEmptyText(context.getText(textId))
    }

    override fun setErrorButtonText(text: CharSequence): StatefulView {
        errorButtonText = text
        findViewById<TextView>(R.id.error_button)?.let {
            it.text = text
            it.visibility = if (text.isBlank()) GONE else VISIBLE
        }
        return this
    }

    override fun setErrorButtonText(textId: Int): StatefulView {
        return setErrorButtonText(context.getText(textId))
    }

    override fun setErrorButtonVisible(visible: Boolean): StatefulView {
        errorButtonVisible = visible
        findViewById<TextView>(R.id.error_button)?.apply {
            visibility = if (visible) VISIBLE else GONE
        }
        return this
    }

    override fun setErrorActionListener(listener: OnActionListener): StatefulView {
        errorActionListener = listener
        return this
    }

    override fun attachTo(adapter: RecyclerView.Adapter<*>): StatefulView {
        if (dataObservers == null) {
            dataObservers = mutableMapOf()
        }
        dataObservers!!.getOrPut(adapter) {
            AdapterDataObserver(adapter) { this }
        }.register()
        return this
    }

    override fun detachTo(adapter: RecyclerView.Adapter<*>): StatefulView {
        dataObservers?.get(adapter)?.unregister()
        return this
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        dataObservers?.forEach {
            it.value.unregister()
        }
    }

    private fun show(viewType: Int) {
        if (this.viewType == viewType) return

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
                    if (emptyTextAppearance != NO_RESOURCE_ID) {
                        TextViewCompat.setTextAppearance(it, emptyTextAppearance)
                    }
                    if (emptyTextColor != NO_COLOR) {
                        it.setTextColor(emptyTextColor)
                    }
                }
                emptyButton?.let {
                    it.text = emptyButtonText
                    it.background = emptyButtonBackground
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
                    it.text = errorButtonText
                    it.background = errorButtonBackground
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
        statefulLayout: () -> StatefulLayout
    ) : RecyclerView.AdapterDataObserver() {

        private val layout: StatefulLayout? by weak(statefulLayout)

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
        fun wrap(activity: Activity): StatefulLayout {
            return wrap(
                (activity.findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0)
            )
        }

        fun wrap(fragment: Fragment): StatefulLayout {
            return wrap(requireNotNull(fragment.view))
        }

        fun wrap(view: View): StatefulLayout {
            val parent = view.parent as ViewGroup
            val params = view.layoutParams
            val index = parent.indexOfChild(view)
            parent.removeView(view)
            val layout = StatefulLayout(view.context)
            layout.setContentView(view)
            parent.addView(layout, index, params)
            return layout
        }

        private const val NO_RESOURCE_ID = -1
        private const val NO_COLOR = -1
    }

}