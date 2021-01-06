package com.easy.kotlins.viewmodel

import androidx.lifecycle.ViewModel
import com.easy.kotlins.event.EventObserver

/**
 * Create by LiZhanPing on 2020/9/16
 */
object ViewModelEvents {

    fun <T, VM> observe(owner: T) where VM : ViewModel, VM : ViewModelController, T : ViewModelOwner<VM>, T : EventObserver {
        owner.viewModel.observeEvent(owner)
    }
}