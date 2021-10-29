@file:Suppress("UNUSED")

package com.nice.common.app

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.lifecycle.ViewModel
import com.nice.common.viewmodel.*
import com.nice.common.widget.InfiniteView
import com.nice.common.widget.ProgressView
import com.nice.common.widget.StatefulView
import com.nice.common.widget.TipView

abstract class NiceViewModelActivity<VM>(@LayoutRes contentLayoutId: Int = 0) :
    NiceActivity(contentLayoutId),
    LifecycleMessageObserver,
    ViewModelMessageDispatcher,
    ViewModelOwner<VM> where VM : ViewModel, VM : ViewModelController {

    open val statefulView: StatefulView? = null

    open val infiniteView: InfiniteView? = null

    open val progressView: ProgressView? = null

    open val tipView: TipView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewModelMessages.observeOnActivity(this)
    }

    final override fun onMessageChanged(message: Message) {
        dispatchViewModelMessage(message)
    }

    override fun onInterceptViewModelMessage(message: Message): Boolean {
        return false
    }

    override fun dispatchViewModelMessage(message: Message): Boolean {
        if (onInterceptViewModelMessage(message)) {
            return true
        }

        for (fragment in supportFragmentManager.fragments) {
            if (fragment is ViewModelMessageDispatcher
                && fragment is ViewModelOwner<*>
                && fragment.viewModel !== viewModel
                && fragment.dispatchViewModelMessage(message)
            ) {
                return true
            }
        }

        return onViewModelMessage(message)
    }

    override fun onViewModelMessage(message: Message): Boolean {
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
            is Message.Tip -> tipView?.show(message.text)
            else -> return false
        }
        return true
    }

}