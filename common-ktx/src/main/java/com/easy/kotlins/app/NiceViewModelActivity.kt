package com.easy.kotlins.app

import android.os.Bundle
import androidx.lifecycle.ViewModel
import com.easy.kotlins.event.Event
import com.easy.kotlins.event.Status
import com.easy.kotlins.helper.toast
import com.easy.kotlins.viewmodel.ViewModelController
import com.easy.kotlins.viewmodel.ViewModelEventObservableOwner
import com.easy.kotlins.viewmodel.ViewModelEvents
import com.easy.kotlins.viewmodel.ViewModelOwner
import com.easy.kotlins.widget.LoadingView
import com.easy.kotlins.widget.ProgressView
import com.easy.kotlins.widget.RefreshView

/**
 * Create by LiZhanPing on 2020/9/18
 */
abstract class NiceViewModelActivity<VM>(layoutResId: Int) : NiceActivity(layoutResId),
    ViewModelEventObservableOwner,
    ViewModelOwner<VM> where VM : ViewModel, VM : ViewModelController {

    open val refreshView: RefreshView? = null

    open val loadingView: LoadingView? = null

    open val progressView: ProgressView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        ViewModelEvents.observe(this)
        super.onCreate(savedInstanceState)
    }

    final override fun onEventChanged(event: Event) {
        for (fragment in supportFragmentManager.fragments) {
            if ((fragment as? ViewModelEventObservableOwner)?.onInterceptViewModelEvent(event) == true) {
                return
            }
        }

        if (onViewModelEvent(event)) {
            return
        }

        callEvent(event)
    }

    override fun onInterceptViewModelEvent(event: Event): Boolean {
        return false
    }

    override fun onViewModelEvent(event: Event): Boolean {
        return false
    }

    private fun callEvent(event: Event) {
        when (event.what) {
            Status.SHOW_PROGRESS -> progressView?.showProgress(event.message)
            Status.DISMISS_PROGRESS -> progressView?.dismissProgress()
            Status.REFRESH_COMPLETE -> refreshView?.finishRefresh()
            Status.LOADMORE_COMPLETE -> refreshView?.finishLoadMore()
            Status.LOADMORE_COMPLETE_NO_MORE -> refreshView?.finishLoadMore(false)
            Status.REFRESH_FAILURE -> refreshView?.refreshFailed()
            Status.LOADMORE_FAILURE -> refreshView?.loadMoreFailed()
            Status.SHOW_LOADING -> loadingView?.apply {
                if (event.message != null) setLoadingText(event.message)
                showLoading()
            }
            Status.SHOW_EMPTY -> loadingView?.apply {
                if (event.message != null) setEmptyText(event.message)
                showEmpty()
            }
            Status.SHOW_ERROR -> loadingView?.apply {
                if (event.message != null) setErrorText(event.message)
                showError()
            }
            Status.SHOW_CONTENT -> loadingView?.showContent()
            else -> event.message?.let { toast(it) }
        }

        val intent = event.getIntent() ?: return
        if (event.what == Status.NONE) {
            startActivity(intent)
        } else {
            startActivityForResult(intent, event.what)
        }
    }
}