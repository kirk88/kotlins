package com.easy.kotlins.app

import android.os.Bundle
import com.easy.kotlins.event.Event
import com.easy.kotlins.event.EventObservableView
import com.easy.kotlins.event.MultipleEvent
import com.easy.kotlins.event.Status
import com.easy.kotlins.helper.toast
import com.easy.kotlins.viewmodel.ViewModelEvents
import com.easy.kotlins.widget.LoadingView
import com.easy.kotlins.widget.ProgressView
import com.easy.kotlins.widget.RefreshView

/**
 * Create by LiZhanPing on 2020/9/18
 */
abstract class NiceEventActivity(layoutResId: Int) : NiceActivity(layoutResId),
    EventObservableView {

    open val refreshView: RefreshView? = null

    open val progressView: ProgressView? = null

    open val loadingView: LoadingView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        ViewModelEvents.observe(this)
        super.onCreate(savedInstanceState)
    }

    final override fun onEventChanged(event: Event) {
        if (onViewModelEventChanged(event)) return

        if (event is MultipleEvent) {
            event.events.forEach { callEvent(it) }
        } else {
            callEvent(event)
        }
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
            } ?: event.message?.let { toast(it) }
            Status.SHOW_EMPTY -> loadingView?.apply {
                if (event.message != null) setEmptyText(event.message)
                showEmpty()
            } ?: event.message?.let { toast(it) }
            Status.SHOW_ERROR -> loadingView?.apply {
                if (event.message != null) setErrorText(event.message)
                showError()
            } ?: event.message?.let { toast(it) }
            Status.SHOW_CONTENT -> loadingView?.showContent()
            else -> event.message?.let { toast(it) }
        }

        val intent = event.getIntent() ?: return
        startActivity(intent)
    }

    open fun onViewModelEventChanged(event: Event): Boolean {
        return false
    }
}