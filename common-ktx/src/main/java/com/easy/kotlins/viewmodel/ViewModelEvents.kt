package com.easy.kotlins.viewmodel

import androidx.lifecycle.ViewModel
import com.easy.kotlins.event.EventObserver

/**
 * Create by LiZhanPing on 2020/9/16
 */
object ViewModelEvents {

    fun <T> observe(owner: T) where T : ViewModelOwner<*>, T : EventObserver {
        val model = owner.viewModel
        if (model is ViewModelController) {
            model.observeEvent(owner)
        }
    }
}