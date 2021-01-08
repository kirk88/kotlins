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

    fun setEmptyClickListener(listener: View.OnClickListener?): LoadingView

    fun setLoadingText(text: CharSequence): LoadingView

    fun setLoadingText(@StringRes textId: Int): LoadingView

    fun setErrorImage(drawable: Drawable?): LoadingView

    fun setErrorImage(@DrawableRes drawableId: Int): LoadingView

    fun setErrorText(text: CharSequence): LoadingView

    fun setErrorText(@StringRes textId: Int): LoadingView

    fun setErrorButtonText(text: CharSequence): LoadingView

    fun setErrorButtonText(@StringRes textId: Int): LoadingView

    fun setErrorClickListener(listener: View.OnClickListener?): LoadingView

    fun attachTo(adapter: RecyclerView.Adapter<*>)
}