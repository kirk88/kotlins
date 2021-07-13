package com.nice.kotlins.viewmodel

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.nice.kotlins.event.Event
import com.nice.kotlins.event.EventLifecycleObserver

interface ViewModelEventDispatcher {

    fun onInterceptViewModelEvent(event: Event): Boolean

    fun dispatchViewModelEvent(event: Event): Boolean

    fun onViewModelEvent(event: Event): Boolean

}

object ViewModelEvents {

    fun <T, VM> observeOnActivity(owner: T) where T : FragmentActivity, T : EventLifecycleObserver, T : ViewModelOwner<VM>, VM : ViewModel, VM : ViewModelController {
        owner.viewModel.addEventObserver(owner)
    }

    fun <T, VM> observeOnFragment(owner: T) where T : Fragment, T : EventLifecycleObserver, T : ViewModelOwner<VM>, VM : ViewModel, VM : ViewModelController {
        val activity = requireNotNull(owner.activity) {
            "Fragment $owner was not attach to an activity"
        }
        if (activity is EventLifecycleObserver
                && activity is ViewModelOwner<*>
                && activity.viewModel == owner.viewModel
        ) {
            return
        }

        val fragment = owner.parentFragment
        if (fragment is EventLifecycleObserver
                && fragment is ViewModelOwner<*>
                && fragment.viewModel == owner.viewModel
        ) {
            return
        }

        owner.viewModel.addEventObserver(owner)
    }

}