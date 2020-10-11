package com.easy.kotlins.app

import android.os.Bundle
import com.easy.kotlins.event.Event
import com.easy.kotlins.event.EventObservableView
import com.easy.kotlins.event.Status
import com.easy.kotlins.helper.toast
import com.easy.kotlins.viewmodel.ViewModelEvents
import com.easy.kotlins.widget.LoadingView
import com.easy.kotlins.widget.RefreshView

/**
 * Create by LiZhanPing on 2020/9/18
 */
abstract class NiceEventActivity(layoutID: Int) : NiceActivity(layoutID), EventObservableView {

    open val refreshView: RefreshView? = null

    open val loadingView: LoadingView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        ViewModelEvents.observe(this)
        super.onCreate(savedInstanceState)
    }

    final override fun onEventChanged(event: Event) {
        if (onViewModelEventChanged(event)) return

        when (event.what) {
            Status.SHOW_PROGRESS.code -> loadingView?.showProgress(
                event.getString("message") ?: "正在加载"
            )
            Status.HIDE_PROGRESS.code -> loadingView?.dismissProgress()
            Status.REFRESH_COMPLETE.code -> refreshView?.finishRefresh()
            Status.LOADMORE_COMPLETE.code -> refreshView?.finishLoadMore()
            Status.LOADMORE_COMPLETE_NO_MORE.code -> refreshView?.finishLoadMore(false)
            Status.REFRESH_FAILURE.code -> refreshView?.refreshFailed()
            Status.LOADMORE_FAILURE.code -> refreshView?.loadMoreFailed()
        }

        event.message?.let { toast(it) }
    }

    open fun onViewModelEventChanged(event: Event): Boolean {
        return false
    }
}