package com.easy.kotlins.viewmodel

import androidx.lifecycle.ViewModel
import com.easy.kotlins.event.EventObservableOwner

/**
 * Create by LiZhanPing on 2020/9/16
 */
object ViewModelEvents {

    fun <T, VM> observe(owner: T) where VM : ViewModel, VM : ViewModelController, T : ViewModelOwner<VM>, T : EventObservableOwner {
        owner.viewModel.addEventObserver(owner)
    }
}