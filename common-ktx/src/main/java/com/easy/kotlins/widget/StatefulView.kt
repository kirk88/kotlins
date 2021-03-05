package com.easy.kotlins.widget

import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView

interface StatefulView {

    fun showLoading()

    fun showEmpty()

    fun showError()

    fun showContent()

    fun setContentView(@LayoutRes layoutResId: Int): StatefulView

    fun setContentView(view: View): StatefulView

    fun setLoadingView(@LayoutRes layoutResId: Int): StatefulView

    fun setLoadingView(view: View): StatefulView

    fun setEmptyView(@LayoutRes layoutResId: Int): StatefulView

    fun setEmptyView(view: View): StatefulView

    fun setErrorView(@LayoutRes layoutResId: Int): StatefulView

    fun setErrorView(view: View): StatefulView

    fun setEmptyImage(drawable: Drawable?): StatefulView

    fun setEmptyImage(@DrawableRes drawableId: Int): StatefulView

    fun setEmptyText(text: CharSequence): StatefulView

    fun setEmptyText(@StringRes textId: Int): StatefulView

    fun setEmptyButtonText(text: CharSequence): StatefulView

    fun setEmptyButtonText(@StringRes textId: Int): StatefulView

    fun setEmptyButtonVisible(visible: Boolean): StatefulView

    fun setEmptyActionListener(listener: OnActionListener): StatefulView

    fun setLoadingText(text: CharSequence): StatefulView

    fun setLoadingText(@StringRes textId: Int): StatefulView

    fun setErrorImage(drawable: Drawable?): StatefulView

    fun setErrorImage(@DrawableRes drawableId: Int): StatefulView

    fun setErrorText(text: CharSequence): StatefulView

    fun setErrorText(@StringRes textId: Int): StatefulView

    fun setErrorButtonText(text: CharSequence): StatefulView

    fun setErrorButtonText(@StringRes textId: Int): StatefulView

    fun setErrorButtonVisible(visible: Boolean): StatefulView

    fun setErrorActionListener(listener: OnActionListener): StatefulView

    fun attachTo(adapter: RecyclerView.Adapter<*>): StatefulView

    fun detachTo(adapter: RecyclerView.Adapter<*>): StatefulView
}

fun interface OnActionListener {
    fun onAction(view: StatefulView)
}