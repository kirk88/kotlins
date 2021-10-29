package com.nice.common.viewmodel

interface ViewModelMessageDispatcher {

    fun onInterceptViewModelMessage(message: Message): Boolean

    fun dispatchViewModelMessage(message: Message): Boolean

    fun onViewModelMessage(message: Message): Boolean

}