package com.easy.kotlins.widget

import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes

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

    fun setLoadingText(text: CharSequence): LoadingView

    fun setLoadingText(@StringRes textId: Int): LoadingView

    fun setErrorImage(drawable: Drawable?): LoadingView

    fun setErrorImage(@DrawableRes drawableId: Int): LoadingView

    fun setErrorText(text: CharSequence): LoadingView

    fun setErrorText(@StringRes textId: Int): LoadingView

    fun setRetryButtonText(text: CharSequence): LoadingView

    fun setRetryButtonText(@StringRes textId: Int): LoadingView

    fun setRetryListener(listener: View.OnClickListener?): LoadingView
}