@file:Suppress("unused")

package com.nice.kotlins.app

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.lifecycle.ViewModel
import com.nice.kotlins.event.Event
import com.nice.kotlins.event.EventLifecycleObserver
import com.nice.kotlins.event.Status
import com.nice.kotlins.event.getOrDefault
import com.nice.kotlins.viewmodel.ViewModelController
import com.nice.kotlins.viewmodel.ViewModelEventDispatcher
import com.nice.kotlins.viewmodel.ViewModelEvents
import com.nice.kotlins.viewmodel.ViewModelOwner
import com.nice.kotlins.widget.LoaderView
import com.nice.kotlins.widget.ProgressView
import com.nice.kotlins.widget.RefreshView
import com.nice.kotlins.widget.TipView

abstract class NiceViewModelActivity<VM>(@LayoutRes contentLayoutId: Int = 0) :
        NiceActivity(contentLayoutId),
        EventLifecycleObserver,
        ViewModelEventDispatcher,
        ViewModelOwner<VM> where VM : ViewModel, VM : ViewModelController {

    open val loaderView: LoaderView? = null

    open val refreshView: RefreshView? = null

    open val progressView: ProgressView? = null

    open val tipView: TipView? = null

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
            Status.SHOW_PROGRESS -> progressView?.show(event.message)
            Status.DISMISS_PROGRESS -> progressView?.dismiss()
            Status.REFRESH_COMPLETE -> refreshView?.finishRefresh(event.getOrDefault("state", 0))
            Status.LOADMORE_COMPLETE -> refreshView?.finishLoadMore(event.getOrDefault("state", 0))
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
                val intent = event.intent ?: return true
                val callback = event.resultCallback
                if (callback == null) {
                    startActivity(intent)
                } else {
                    activityForResultLauncher.launch(intent, callback)
                }
            }
            Status.ACTIVITY_RESULT -> {
                setResult(event.resultCode, event.intent)
                finish()
            }
            else -> event.message?.let { tipView?.show(it) }
        }
        return true
    }

}