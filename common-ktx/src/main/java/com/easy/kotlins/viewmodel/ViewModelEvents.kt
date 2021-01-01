package com.easy.kotlins.viewmodel

import com.easy.kotlins.event.EventObservableView

/**
 * Create by LiZhanPing on 2020/9/16
 */
object ViewModelEvents {

    fun observe(owner: EventObservableView) {
        when {
            owner !is ViewModelOwner<*> ||
            owner.viewModel !is ViewModelController -> throw IllegalArgumentException("Non-support observe event owner ${owner.javaClass.name}")
            else -> (owner.viewModel as ViewModelController).observeEvent(owner)
        }
    }
}