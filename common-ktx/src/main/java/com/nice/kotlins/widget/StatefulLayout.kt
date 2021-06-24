@file:Suppress("unused")

package com.nice.kotlins.widget

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.contains
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.nice.kotlins.R
import com.nice.kotlins.helper.*
import com.nice.kotlins.widget.StatefulView.Companion.TYPE_CONTENT_VIEW
import com.nice.kotlins.widget.StatefulView.Companion.TYPE_EMPTY_VIEW
import com.nice.kotlins.widget.StatefulView.Companion.TYPE_ERROR_VIEW
import com.nice.kotlins.widget.StatefulView.Companion.TYPE_LOADING_VIEW

class StatefulLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.statefulLayoutStyle
) : FrameLayout(context, attrs, defStyleAttr), StatefulView {

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
    private var viewType: Int = NO_TYPE

    private val views: MutableMap<Int, View> = mutableMapOf()
    private var dataObservers: MutableMap<RecyclerView.Adapter<*>, AdapterDataObserver>? = null
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private var errorActionListener: StatefulView.OnActionListener? = null
    private val errorButtonClickListener = OnClickListener {
        if (errorActionListener != null) {
            errorActionListener!!.onAction(this)
        }
    }

    private var emptyActionListener: StatefulView.OnActionListener? = null
    private val emptyButtonClickListener = OnClickListener {
        if (emptyActionListener != null) {
            emptyActionListener!!.onAction(this)
        }
    }

    init {
        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.StatefulLayout,
            defStyleAttr,
            R.style.StatefulLayout_Style
        )
        emptyLayoutId = a.getResourceId(R.styleable.StatefulLayout_emptyLayout, NO_VALUE)
        loadingLayoutId = a.getResourceId(R.styleable.StatefulLayout_loadingLayout, NO_VALUE)
        errorLayoutId = a.getResourceId(R.styleable.StatefulLayout_errorLayout, NO_VALUE)
        emptyImage = a.getDrawable(R.styleable.StatefulLayout_emptyImage)
        emptyText = a.getText(R.styleable.StatefulLayout_emptyText)
        emptyTextColor = a.getColor(R.styleable.StatefulLayout_emptyTextColor, NO_VALUE)
        emptyTextAppearance =
            a.getResourceId(R.styleable.StatefulLayout_emptyTextAppearance, NO_VALUE)
        emptyButtonText = a.getText(R.styleable.StatefulLayout_emptyButtonText)
        emptyButtonTextColor = a.getColor(R.styleable.StatefulLayout_emptyButtonTextColor, NO_VALUE)
        emptyButtonTextAppearance =
            a.getResourceId(R.styleable.StatefulLayout_emptyButtonTextAppearance, NO_VALUE)
        emptyButtonBackground = a.getDrawable(R.styleable.StatefulLayout_emptyButtonBackground)
        if (emptyButtonBackground == null) {
            val color = a.getColor(R.styleable.StatefulLayout_emptyButtonBackground, NO_VALUE)
            if (color != NO_VALUE) {
                emptyButtonBackground = ColorDrawable(color)
            }
        }
        emptyButtonVisible = a.getBoolean(R.styleable.StatefulLayout_emptyButtonVisible, false)

        errorImage = a.getDrawable(R.styleable.StatefulLayout_errorImage)
        errorText = a.getText(R.styleable.StatefulLayout_errorText)
        errorTextColor = a.getColor(R.styleable.StatefulLayout_errorTextColor, NO_VALUE)
        errorTextAppearance =
            a.getResourceId(R.styleable.StatefulLayout_errorTextAppearance, NO_VALUE)
        errorButtonText = a.getText(R.styleable.StatefulLayout_errorButtonText)
        errorButtonTextColor = a.getColor(R.styleable.StatefulLayout_errorButtonTextColor, NO_VALUE)
        errorButtonTextAppearance =
            a.getResourceId(R.styleable.StatefulLayout_errorButtonTextAppearance, NO_VALUE)
        errorButtonBackground = a.getDrawable(R.styleable.StatefulLayout_errorButtonBackground)
        if (errorButtonBackground == null) {
            val color = a.getColor(R.styleable.StatefulLayout_errorButtonBackground, NO_VALUE)
            if (color != NO_VALUE) {
                errorButtonBackground = ColorDrawable(color)
            }
        }
        errorButtonVisible = a.getBoolean(R.styleable.StatefulLayout_errorButtonVisible, true)

        loadingProgressColor = a.getColor(R.styleable.StatefulLayout_loadingProgressColor, NO_VALUE)
        loadingProgressDrawable = a.getDrawable(R.styleable.StatefulLayout_loadingProgressDrawable)
        loadingText = a.getText(R.styleable.StatefulLayout_loadingText)
        loadingTextColor = a.getColor(R.styleable.StatefulLayout_loadingTextColor, NO_VALUE)
        loadingTextAppearance =
            a.getResourceId(R.styleable.StatefulLayout_loadingTextAppearance, NO_VALUE)

        defaultViewType = a.getInt(R.styleable.StatefulLayout_defaultShow, TYPE_LOADING_VIEW)
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

        showImmediately(defaultViewType, false)
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
        setView(layoutResId, TYPE_CONTENT_VIEW)
        return this
    }

    override fun setContentView(view: View): StatefulView {
        setView(view, TYPE_CONTENT_VIEW)
        return this
    }

    override fun setLoadingView(layoutResId: Int): StatefulView {
        setView(layoutResId, TYPE_LOADING_VIEW)
        return this
    }

    override fun setLoadingView(view: View): StatefulView {
        setView(view, TYPE_LOADING_VIEW)
        return this
    }

    override fun setEmptyView(layoutResId: Int): StatefulView {
        setView(layoutResId, TYPE_EMPTY_VIEW)
        return this
    }

    override fun setEmptyView(view: View): StatefulLayout {
        setView(view, TYPE_EMPTY_VIEW)
        return this
    }

    override fun setErrorView(layoutResId: Int): StatefulView {
        setView(layoutResId, TYPE_ERROR_VIEW)
        return this
    }

    override fun setErrorView(view: View): StatefulView {
        setView(view, TYPE_ERROR_VIEW)
        return this
    }

    override fun setDefaultView(viewType: Int): StatefulView {
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
        findViewById<TextView>(R.id.empty_button)?.text = text
        return this
    }

    override fun setEmptyButtonText(textId: Int): StatefulView {
        return setEmptyButtonText(context.getText(textId))
    }

    override fun setEmptyButtonVisible(visible: Boolean): StatefulView {
        emptyButtonVisible = visible
        findViewById<TextView>(R.id.empty_button)?.let {
            it.visibility = if (visible) VISIBLE else GONE
        }
        return this
    }

    override fun setOnEmptyActionListener(listener: StatefulView.OnActionListener): StatefulView {
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
        findViewById<TextView>(R.id.error_button)?.text = text
        return this
    }

    override fun setErrorButtonText(textId: Int): StatefulView {
        return setErrorButtonText(context.getText(textId))
    }

    override fun setErrorButtonVisible(visible: Boolean): StatefulView {
        errorButtonVisible = visible
        findViewById<TextView>(R.id.error_button)?.let {
            it.visibility = if (visible) VISIBLE else GONE
        }
        return this
    }

    override fun setOnErrorActionListener(listener: StatefulView.OnActionListener): StatefulView {
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
        if (dataObservers != null) {
            dataObservers!!.forEachValue {
                it.unregister()
            }
            dataObservers!!.clear()
            dataObservers = null
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState)
        ss.viewType = viewType
        return ss
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)
        setDefaultView(ss.viewType)
    }

    private fun show(viewType: Int) {
        post(ShowRunnable(viewType))
    }

    private fun showImmediately(viewType: Int, animate: Boolean = true) {
        if (viewType == NO_TYPE || this.viewType == viewType) {
            return
        }

        this.viewType = viewType

        for ((type, view) in views) {
            if (type == viewType) continue

            view.invisible(animate)
        }

        views.getOrElse(viewType) {
            when (viewType) {
                TYPE_EMPTY_VIEW -> setView(emptyLayoutId, TYPE_EMPTY_VIEW)
                TYPE_LOADING_VIEW -> setView(loadingLayoutId, TYPE_LOADING_VIEW)
                TYPE_ERROR_VIEW -> setView(errorLayoutId, TYPE_ERROR_VIEW)
                else -> null
            }
        }?.visible(animate)
    }

    private fun setView(layoutResId: Int, viewType: Int): View {
        return inflater.inflate(layoutResId, this, false).also {
            setView(it, viewType)
        }
    }

    private fun setView(view: View, viewType: Int) {
        view.visibility = if (this.viewType == viewType) VISIBLE else INVISIBLE
        when (viewType) {
            TYPE_LOADING_VIEW -> {
                view.findViewById<ProgressBar>(R.id.loading_progress)?.apply {
                    if (loadingProgressDrawable != null) {
                        indeterminateDrawable = loadingProgressDrawable
                    } else if (Build.VERSION.SDK_INT >= 21 && loadingProgressColor != NO_VALUE) {
                        indeterminateTintMode = PorterDuff.Mode.SRC_ATOP
                        indeterminateTintList = ColorStateList.valueOf(loadingProgressColor)
                    }
                }
                view.findViewById<TextView>(R.id.loading_text)?.apply {
                    text = loadingText
                    if (loadingTextAppearance != NO_VALUE) {
                        textAppearance = loadingTextAppearance
                    }
                    if (loadingTextColor != NO_VALUE) {
                        textColor = loadingTextColor
                    }
                    visibility = if (loadingText.isNullOrBlank()) GONE else VISIBLE
                }
            }
            TYPE_EMPTY_VIEW -> {
                view.findViewById<ImageView>(R.id.empty_image)?.setImageDrawable(this.emptyImage)
                view.findViewById<TextView>(R.id.empty_text)?.apply {
                    text = emptyText
                    if (emptyTextAppearance != NO_VALUE) {
                        textAppearance = emptyTextAppearance
                    }
                    if (emptyTextColor != NO_VALUE) {
                        textColor = emptyTextColor
                    }
                }
                view.findViewById<Button>(R.id.empty_button)?.apply {
                    text = emptyButtonText
                    background = emptyButtonBackground
                    if (emptyButtonTextAppearance != NO_VALUE) {
                        textAppearance = emptyButtonTextAppearance
                    }
                    if (emptyButtonTextColor != NO_VALUE) {
                        setTextColor(emptyButtonTextColor)
                    }
                    visibility = if (emptyButtonVisible) VISIBLE else GONE
                    setOnClickListener(emptyButtonClickListener)
                }
            }
            TYPE_ERROR_VIEW -> {
                view.findViewById<ImageView>(R.id.error_image)?.setImageDrawable(this.errorImage)
                view.findViewById<TextView>(R.id.error_text)?.apply {
                    text = errorText
                    if (errorTextAppearance != NO_VALUE) {
                        textAppearance = errorTextAppearance
                    }
                    if (errorTextColor != NO_VALUE) {
                        textColor = errorTextColor
                    }
                }
                view.findViewById<Button>(R.id.error_button)?.apply {
                    text = errorButtonText
                    background = errorButtonBackground
                    if (errorButtonTextAppearance != NO_VALUE) {
                        textAppearance = errorButtonTextAppearance
                    }
                    if (errorButtonTextColor != NO_VALUE) {
                        textColor = errorButtonTextColor
                    }
                    visibility = if (errorButtonVisible) VISIBLE else GONE
                    setOnClickListener(errorButtonClickListener)
                }
            }
        }

        val existingView = views.put(viewType, view)
        if (existingView != null) {
            removeView(existingView)
        }

        if (contains(view)) {
            return
        }

        val parent = view.parent
        if (parent is ViewGroup) {
            parent.removeView(view)
        }

        if (viewType == TYPE_CONTENT_VIEW) {
            val lp = view.layoutParams ?: LayoutParams(-1, -1)
            addView(view, 0, generateLayoutParams(lp))
        } else {
            addView(view)
        }
    }

    private inner class ShowRunnable(private val newViewType: Int) : Runnable {
        override fun run() {
            showImmediately(newViewType)
        }
    }

    private class SavedState : BaseSavedState {
        var viewType = NO_VALUE

        constructor(superState: Parcelable?) : super(superState)

        private constructor(parcel: Parcel) : super(parcel) {
            viewType = parcel.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeValue(viewType)
        }

        override fun toString(): String {
            return ("LoaderLayout.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " viewType=" + viewType + "}")
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }

    private class AdapterDataObserver(
        val adapter: RecyclerView.Adapter<*>,
        loaderLayout: () -> StatefulLayout
    ) : RecyclerView.AdapterDataObserver() {

        private val layout: StatefulLayout? by weak(loaderLayout)

        private var registered: Boolean = false

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
            check(!registered) { "Already registered" }

            adapter.registerAdapterDataObserver(this)
            registered = true
        }

        fun unregister() {
            if (!registered) {
                return
            }

            adapter.unregisterAdapterDataObserver(this)
            registered = false
        }

    }

    companion object {

        private const val NO_VALUE = Int.MIN_VALUE
        private const val NO_TYPE = -1

        fun wrap(activity: Activity): StatefulView {
            return wrap(
                (activity.findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0)
            )
        }

        fun wrap(fragment: Fragment): StatefulView {
            return wrap(fragment.requireView())
        }

        fun wrap(view: View): StatefulView {
            val parent = view.parent as ViewGroup
            val params = view.layoutParams
            val index = parent.indexOfChild(view)
            parent.removeView(view)
            val layout = StatefulLayout(view.context)
            layout.setContentView(view)
            parent.addView(layout, index, params)
            return layout
        }

    }

}