package com.easy.kotlins.app

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.easy.kotlins.viewmodel.ViewModelEvents
import com.easy.kotlins.event.Event
import com.easy.kotlins.event.EventObservableView
import com.easy.kotlins.event.Status
import com.easy.kotlins.helper.toast
import com.easy.kotlins.widget.LoadingView
import com.easy.kotlins.widget.RefreshView

/**
 * Create by LiZhanPing on 2020/9/18
 */
abstract class NiceEventFragment(layoutID: Int) : NiceFragment(layoutID), EventObservableView {

    open val refreshView: RefreshView? = null

    open val loadingView: LoadingView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ViewModelEvents.observe(this)
        super.onViewCreated(view, savedInstanceState)
    }

    final override fun onEventChanged(event: Event) {
        if (onViewModelEventChanged(event)) return

        when (event.what) {
            Status.SHOW_PROGRESS -> loadingView?.showProgress(event.message)
            Status.DISMISS_PROGRESS -> loadingView?.dismissProgress()
            Status.REFRESH_COMPLETE -> refreshView?.finishRefresh()
            Status.LOADMORE_COMPLETE -> refreshView?.finishLoadMore()
            Status.LOADMORE_COMPLETE_NO_MORE -> refreshView?.finishLoadMore(false)
            Status.REFRESH_FAILURE -> refreshView?.refreshFailed()
            Status.LOADMORE_FAILURE -> refreshView?.loadMoreFailed()
            else -> event.message?.let { toast(it) }
        }
    }

    open fun onViewModelEventChanged(event: Event): Boolean {
        return false
    }
}