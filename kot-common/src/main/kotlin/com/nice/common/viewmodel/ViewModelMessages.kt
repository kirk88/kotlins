package com.nice.common.viewmodel

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel

interface ViewModelMessageDispatcher {

    fun onInterceptViewModelMessage(message: Message): Boolean

    fun dispatchViewModelMessage(message: Message): Boolean

    fun onViewModelMessage(message: Message): Boolean

}

object ViewModelMessages {

    fun <T, VM> observeOnActivity(owner: T) where T : FragmentActivity, T : LifecycleMessageObserver, T : ViewModelOwner<VM>, VM : ViewModel, VM : ViewModelController {
        owner.viewModel.observeMessage(owner)
    }

    fun <T, VM> observeOnFragment(owner: T) where T : Fragment, T : LifecycleMessageObserver, T : ViewModelOwner<VM>, VM : ViewModel, VM : ViewModelController {
        val activity = requireNotNull(owner.activity) {
            "Fragment $owner was not attach to an activity"
        }
        if (activity is LifecycleMessageObserver
                && activity is ViewModelOwner<*>
                && activity.viewModel == owner.viewModel
        ) {
            return
        }

        val fragment = owner.parentFragment
        if (fragment is LifecycleMessageObserver
                && fragment is ViewModelOwner<*>
                && fragment.viewModel == owner.viewModel
        ) {
            return
        }

        owner.viewModel.observeMessage(owner)
    }

}