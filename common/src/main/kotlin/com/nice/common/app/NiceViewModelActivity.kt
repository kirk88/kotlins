@file:Suppress("unused")

package com.nice.common.app

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.lifecycle.ViewModel
import com.nice.common.event.EventLifecycleObserver
import com.nice.common.event.Message
import com.nice.common.helper.toast
import com.nice.common.viewmodel.ViewModelController
import com.nice.common.viewmodel.ViewModelEventDispatcher
import com.nice.common.viewmodel.ViewModelEvents
import com.nice.common.viewmodel.ViewModelOwner
import com.nice.common.widget.InfiniteView
import com.nice.common.widget.ProgressView
import com.nice.common.widget.StatefulView
import com.nice.common.widget.TipView

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

    final override fun onEventChanged(message: Message) {
        dispatchViewModelEvent(message)
    }

    override fun onInterceptViewModelEvent(message: Message): Boolean {
        return false
    }

    override fun dispatchViewModelEvent(message: Message): Boolean {
        if (onInterceptViewModelEvent(message)) {
            return true
        }

        for (fragment in supportFragmentManager.fragments) {
            if (!fragment.isAdded || fragment !is ViewModelEventDispatcher) continue

            if (fragment.dispatchViewModelEvent(message)) {
                return true
            }
        }

        return onViewModelEvent(message)
    }

    override fun onViewModelEvent(message: Message): Boolean {
        when (message) {
            is Message.ShowProgress -> progressView?.show(message.text)
            is Message.DismissProgress -> progressView?.dismiss()
            is Message.RefreshState -> infiniteView?.setRefreshState(message.state)
            is Message.LoadMoreState -> infiniteView?.setLoadMoreState(message.state)
            is Message.ShowLoading -> statefulView?.apply {
                if (message.text != null) setLoadingText(message.text)
                showLoading()
            }
            is Message.ShowEmpty -> statefulView?.apply {
                if (message.text != null) setEmptyText(message.text)
                showEmpty()
            }
            is Message.ShowError -> statefulView?.apply {
                if (message.text != null) setErrorText(message.text)
                showError()
            }
            is Message.ShowContent -> statefulView?.showContent()
            is Message.FinishActivity -> finish()
            is Message.StartActivity -> {
                startActivity(message.intent)
            }
            is Message.StartActivityForResult -> {
                activityForResultLauncher.launch(message.intent, message.callback)
            }
            is Message.SetActivityResult -> {
                setResult(message.resultCode, message.data)
                finish()
            }
            is Message.ShowToast -> toast(message.text, message.duration)
            else -> return false
        }
        return true
    }

}