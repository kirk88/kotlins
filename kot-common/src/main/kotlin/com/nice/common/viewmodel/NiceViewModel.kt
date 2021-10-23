@file:Suppress("UNUSED")

package com.nice.common.viewmodel

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.*

interface ViewModelController {

    var message: Message

    fun observeMessage(owner: LifecycleOwner, observer: MessageObserver)

    fun observeMessage(observer: LifecycleMessageObserver)

}

private class DefaultViewModelController : ViewModelController {

    private val messageDelegate = MessageDelegate()
    override var message: Message by messageDelegate

    override fun observeMessage(owner: LifecycleOwner, observer: MessageObserver) {
        messageDelegate.observe(owner, observer)
    }

    override fun observeMessage(observer: LifecycleMessageObserver) {
        messageDelegate.observe(observer)
    }

}

open class NiceViewModel : ViewModel(), ViewModelController by DefaultViewModelController()

open class NiceAndroidViewModel(application: Application) : AndroidViewModel(application),
    ViewModelController by DefaultViewModelController()

open class SavedStateViewModel(val state: SavedStateHandle) : NiceViewModel()

open class SavedStateAndroidViewModel(application: Application, val state: SavedStateHandle) :
    NiceAndroidViewModel(application)

@MainThread
inline fun <reified VM : ViewModel> Fragment.viewModel(
    factoryProducer: ViewModelProvider.Factory? = null
): VM {
    return ViewModelProvider(
        viewModelStore,
        factoryProducer ?: defaultViewModelProviderFactory
    ).get(VM::class.java)
}

@MainThread
inline fun <reified VM : ViewModel> Fragment.activityViewModel(
    factoryProducer: ViewModelProvider.Factory? = null
): VM {
    return ViewModelProvider(
        requireActivity().viewModelStore,
        factoryProducer ?: requireActivity().defaultViewModelProviderFactory
    ).get(VM::class.java)
}

@MainThread
inline fun <reified VM : ViewModel> ComponentActivity.viewModel(
    factoryProducer: ViewModelProvider.Factory? = null
): VM {
    return ViewModelProvider(
        viewModelStore,
        factoryProducer ?: defaultViewModelProviderFactory
    ).get(VM::class.java)
}