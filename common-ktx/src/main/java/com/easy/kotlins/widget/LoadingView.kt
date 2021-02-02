package com.easy.kotlins.widget

import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView

interface LoadingView {

    fun showLoading()

    fun showEmpty()

    fun showError()

    fun showContent()

    fun setContentView(@LayoutRes layoutResId: Int): LoadingView

    fun setContentView(view: View): LoadingView

    fun setLoadingView(@LayoutRes layoutResId: Int): LoadingView

    fun setLoadingView(view: View): LoadingView

    fun setEmptyView(@LayoutRes layoutResId: Int): LoadingView

    fun setEmptyView(view: View): LoadingView

    fun setErrorView(@LayoutRes layoutResId: Int): LoadingView

    fun setErrorView(view: View): LoadingView

    fun setEmptyImage(drawable: Drawable?): LoadingView

    fun setEmptyImage(@DrawableRes drawableId: Int): LoadingView

    fun setEmptyText(text: CharSequence): LoadingView

    fun setEmptyText(@StringRes textId: Int): LoadingView

    fun setEmptyButtonText(text: CharSequence): LoadingView

    fun setEmptyButtonText(@StringRes textId: Int): LoadingView

    fun setEmptyButtonVisible(visible: Boolean): LoadingView

    fun setEmptyActionListener(listener: OnActionListener): LoadingView

    fun setLoadingText(text: CharSequence): LoadingView

    fun setLoadingText(@StringRes textId: Int): LoadingView

    fun setErrorImage(drawable: Drawable?): LoadingView

    fun setErrorImage(@DrawableRes drawableId: Int): LoadingView

    fun setErrorText(text: CharSequence): LoadingView

    fun setErrorText(@StringRes textId: Int): LoadingView

    fun setErrorButtonText(text: CharSequence): LoadingView

    fun setErrorButtonText(@StringRes textId: Int): LoadingView

    fun setErrorButtonVisible(visible: Boolean): LoadingView

    fun setErrorActionListener(listener: OnActionListener): LoadingView

    fun attachTo(adapter: RecyclerView.Adapter<*>): LoadingView

    fun detachTo(adapter: RecyclerView.Adapter<*>): LoadingView
}

fun interface OnActionListener {
    fun onAction(view: LoadingView)
}