@file:Suppress("unused")

package com.nice.kotlins.widget

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.IntDef
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView

interface LoaderView {

    fun showLoading()

    fun showEmpty()

    fun showError()

    fun showContent()

    fun setContentView(@LayoutRes layoutResId: Int): LoaderView

    fun setContentView(view: View): LoaderView

    fun setLoadingView(@LayoutRes layoutResId: Int): LoaderView

    fun setLoadingView(view: View): LoaderView

    fun setEmptyView(@LayoutRes layoutResId: Int): LoaderView

    fun setEmptyView(view: View): LoaderView

    fun setErrorView(@LayoutRes layoutResId: Int): LoaderView

    fun setErrorView(view: View): LoaderView

    fun setDefaultView(@ViewType viewType: Int): LoaderView

    fun setEmptyImage(drawable: Drawable?): LoaderView

    fun setEmptyImage(@DrawableRes drawableId: Int): LoaderView

    fun setEmptyText(text: CharSequence): LoaderView

    fun setEmptyText(@StringRes textId: Int): LoaderView

    fun setEmptyButtonText(text: CharSequence): LoaderView

    fun setEmptyButtonText(@StringRes textId: Int): LoaderView

    fun setEmptyButtonVisible(visible: Boolean): LoaderView

    fun setOnEmptyActionListener(listener: OnActionListener): LoaderView

    fun setLoadingText(text: CharSequence): LoaderView

    fun setLoadingText(@StringRes textId: Int): LoaderView

    fun setErrorImage(drawable: Drawable?): LoaderView

    fun setErrorImage(@DrawableRes drawableId: Int): LoaderView

    fun setErrorText(text: CharSequence): LoaderView

    fun setErrorText(@StringRes textId: Int): LoaderView

    fun setErrorButtonText(text: CharSequence): LoaderView

    fun setErrorButtonText(@StringRes textId: Int): LoaderView

    fun setErrorButtonVisible(visible: Boolean): LoaderView

    fun setOnErrorActionListener(listener: OnActionListener): LoaderView

    fun attachTo(adapter: RecyclerView.Adapter<*>): LoaderView

    fun detachTo(adapter: RecyclerView.Adapter<*>): LoaderView

    @IntDef(
        TYPE_CONTENT_VIEW,
        TYPE_EMPTY_VIEW,
        TYPE_LOADING_VIEW,
        TYPE_ERROR_VIEW
    )
    annotation class ViewType

    companion object {
        const val TYPE_CONTENT_VIEW = 0x001
        const val TYPE_EMPTY_VIEW = 0x002
        const val TYPE_LOADING_VIEW = 0x003
        const val TYPE_ERROR_VIEW = 0x004

        fun wrap(activity: Activity): LoaderView {
            return wrap(
                (activity.findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0)
            )
        }

        fun wrap(fragment: Fragment): LoaderView {
            return wrap(requireNotNull(fragment.view))
        }

        fun wrap(view: View): LoaderView {
            val parent = view.parent as ViewGroup
            val params = view.layoutParams
            val index = parent.indexOfChild(view)
            parent.removeView(view)
            val layout = LoaderLayout(view.context)
            layout.setContentView(view)
            parent.addView(layout, index, params)
            return layout
        }
    }

    fun interface OnActionListener {
        fun onAction(view: LoaderView)
    }
}