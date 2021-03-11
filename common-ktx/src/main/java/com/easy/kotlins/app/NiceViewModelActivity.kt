@file:Suppress("unused")

package com.easy.kotlins.app

import android.os.Bundle
import android.view.MotionEvent
import androidx.annotation.LayoutRes
import androidx.lifecycle.ViewModel
import com.easy.kotlins.event.Event
import com.easy.kotlins.event.EventLifecycleObserver
import com.easy.kotlins.event.Status
import com.easy.kotlins.helper.toast
import com.easy.kotlins.viewmodel.ViewModelController
import com.easy.kotlins.viewmodel.ViewModelEventDispatcher
import com.easy.kotlins.viewmodel.ViewModelEvents
import com.easy.kotlins.viewmodel.ViewModelOwner
import com.easy.kotlins.widget.StatefulView
import com.easy.kotlins.widget.ProgressView
import com.easy.kotlins.widget.RefreshView

/**
 * Create by LiZhanPing on 2020/9/18
 */
abstract class NiceViewModelActivity<VM>(@LayoutRes layoutResId: Int = 0) :
    NiceActivity(layoutResId),
    EventLifecycleObserver,
    ViewModelEventDispatcher,
    ViewModelOwner<VM> where VM : ViewModel, VM : ViewModelController {

    open val refreshView: RefreshView? = null

    open val statefulView: StatefulView? = null

    open val progressView: ProgressView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        ViewModelEvents.observeOnActivity(this)
        super.onCreate(savedInstanceState)
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

        var dispatched = false
        for (fragment in supportFragmentManager.fragments) {
            if (!fragment.isAdded || fragment !is ViewModelEventDispatcher) continue

            if (fragment.dispatchViewModelEvent(event)) {
                dispatched = true
            }
        }

        if (dispatched) {
            return true
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
            Status.SHOW_LOADING -> statefulView?.apply {
                if (event.message != null) setLoadingText(event.message)
                showLoading()
            }
            Status.SHOW_EMPTY -> statefulView?.apply {
                if (event.message != null) setEmptyText(event.message)
                showEmpty()
            }
            Status.SHOW_ERROR -> statefulView?.apply {
                if (event.message != null) setErrorText(event.message)
                showError()
            }
            Status.SHOW_CONTENT -> statefulView?.showContent()
            else -> event.message?.let { toast(it) }
        }

        val intent = event.getIntent() ?: return false
        when {
            intent.component == null -> {
                setResult(RESULT_OK, intent)
                finish()
            }
            event.what == Status.NONE -> startActivity(intent)
            else -> startActivityForResult(intent, event.what)
        }
        return true
    }

}