@file:Suppress("unused")

package com.nice.kotlins.app

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.lifecycle.ViewModel
import com.nice.kotlins.event.Event
import com.nice.kotlins.event.EventLifecycleObserver
import com.nice.kotlins.event.Status
import com.nice.kotlins.helper.toast
import com.nice.kotlins.viewmodel.ViewModelController
import com.nice.kotlins.viewmodel.ViewModelEventDispatcher
import com.nice.kotlins.viewmodel.ViewModelEvents
import com.nice.kotlins.viewmodel.ViewModelOwner
import com.nice.kotlins.widget.LoaderView
import com.nice.kotlins.widget.ProgressView
import com.nice.kotlins.widget.RefreshView

abstract class NiceViewModelActivity<VM>(@LayoutRes layoutResId: Int = 0) :
    NiceActivity(layoutResId),
    EventLifecycleObserver,
    ViewModelEventDispatcher,
    ViewModelOwner<VM> where VM : ViewModel, VM : ViewModelController {

    open val refreshView: RefreshView? = null

    open val loaderView: LoaderView? = null

    open val progressView: ProgressView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewModelEvents.observeOnActivity(this)
    }

    final override fun onEventChanged(event: Event) {
        dispatchViewModelEvent(event)
    }

    override fun onInterceptViewModelEvent(event: Event): Boolean {
        return false
    }

    override fun dispatchViewModelEvent(event: Event): Boolean {
        if (onInterceptViewModelEvent(event)) {
            return true
        }

        for (fragment in supportFragmentManager.fragments) {
            if (!fragment.isAdded || fragment !is ViewModelEventDispatcher) continue

            if (fragment.dispatchViewModelEvent(event)) {
                return true
            }
        }

        return onViewModelEvent(event)
    }

    override fun onViewModelEvent(event: Event): Boolean {
        when (event.what) {
            Status.SHOW_PROGRESS -> progressView?.showProgress(event.message)
            Status.DISMISS_PROGRESS -> progressView?.dismissProgress()
            Status.REFRESH_SUCCESS -> refreshView?.finishRefresh()
            Status.LOADMORE_SUCCESS -> refreshView?.finishLoadMore()
            Status.LOADMORE_SUCCESS_NO_MORE -> refreshView?.finishLoadMore(false)
            Status.REFRESH_FAILURE -> refreshView?.refreshFailed()
            Status.LOADMORE_FAILURE -> refreshView?.loadMoreFailed()
            Status.SHOW_LOADING -> loaderView?.apply {
                if (event.message != null) setLoadingText(event.message)
                showLoading()
            }
            Status.SHOW_EMPTY -> loaderView?.apply {
                if (event.message != null) setEmptyText(event.message)
                showEmpty()
            }
            Status.SHOW_ERROR -> loaderView?.apply {
                if (event.message != null) setErrorText(event.message)
                showError()
            }
            Status.SHOW_CONTENT -> loaderView?.showContent()
            Status.ACTIVITY_FINISH -> finish()
            Status.ACTIVITY_START -> {
                val intent = event.intent ?: return false
                val callback = event.resultCallback
                if (callback == null) {
                    startActivity(intent)
                } else {
                    registerForActivityResult(
                        ActivityResultContracts.StartActivityForResult(),
                        callback
                    ).launch(intent)
                }
            }
            Status.ACTIVITY_RESULT -> {
                setResult(event.resultCode, event.intent)
                finish()
            }
            else -> event.message?.let { toast(it) }
        }
        return true
    }

}