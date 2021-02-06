package com.easy.kotlins.viewmodel

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.easy.kotlins.event.EventLifecycleObserver

/**
 * Create by LiZhanPing on 2020/9/16
 */
object ViewModelEvents {

    fun <T, VM> observeOnActivity(owner: T) where T : FragmentActivity, T : EventLifecycleObserver, T : ViewModelOwner<VM>, VM : ViewModel, VM : ViewModelController {
        owner.viewModel.addEventObserver(owner)
    }

    fun <T, VM> observeOnFragment(owner: T) where T : Fragment, T : EventLifecycleObserver, T : ViewModelOwner<VM>, VM : ViewModel, VM : ViewModelController {
        val activity = owner.activity
            ?: throw IllegalArgumentException("Fragment $owner was not attach to activity")
        if (activity is EventLifecycleObserver) {
            return
        }

        if (owner.parentFragment is EventLifecycleObserver) {
            return
        }

        owner.viewModel.addEventObserver(owner)
    }
}