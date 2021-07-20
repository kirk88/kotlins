@file:Suppress("unused")

package com.nice.common.app

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.lifecycle.ViewModel
import com.nice.common.event.Event
import com.nice.common.event.EventCode
import com.nice.common.event.EventLifecycleObserver
import com.nice.common.event.getOrDefault
import com.nice.common.viewmodel.ViewModelController
import com.nice.common.viewmodel.ViewModelEventDispatcher
import com.nice.common.viewmodel.ViewModelEvents
import com.nice.common.viewmodel.ViewModelOwner
import com.nice.common.widget.*

abstract class NiceViewModelActivity<VM>(@LayoutRes contentLayoutId: Int = 0) :
    NiceActivity(contentLayoutId),
    EventLifecycleObserver,
    ViewModelEventDispatcher,
    ViewModelOwner<VM> where VM : ViewModel, VM : ViewModelController {

    open val statefulView: StatefulView? = null

    open val infiniteView: InfiniteView? = null

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
        when (event.code) {
            EventCode.SHOW_PROGRESS -> progressView?.show(event.message)
            EventCode.DISMISS_PROGRESS -> progressView?.dismiss()
            EventCode.REFRESH_STATE -> infiniteView?.setRefreshState(
                event.getOrDefault("state", InfiniteState.STATE_IDLE)
            )
            EventCode.LOADMORE_STATE -> infiniteView?.setLoadMoreState(
                event.getOrDefault("state", InfiniteState.STATE_IDLE)
            )
            EventCode.SHOW_LOADING -> statefulView?.apply {
                if (event.message != null) setLoadingText(event.message)
                showLoading()
            }
            EventCode.SHOW_EMPTY -> statefulView?.apply {
                if (event.message != null) setEmptyText(event.message)
                showEmpty()
            }
            EventCode.SHOW_ERROR -> statefulView?.apply {
                if (event.message != null) setErrorText(event.message)
                showError()
            }
            EventCode.SHOW_CONTENT -> statefulView?.showContent()
            EventCode.ACTIVITY_FINISH -> finish()
            EventCode.ACTIVITY_START -> {
                val intent = event.intent ?: return true
                val callback = event.resultCallback
                if (callback == null) {
                    startActivity(intent)
                } else {
                    activityForResultLauncher.launch(intent, callback)
                }
            }
            EventCode.ACTIVITY_RESULT -> {
                setResult(event.resultCode, event.intent)
                finish()
            }
            else -> event.message?.let { tipView?.show(it) }
        }
        return true
    }

}