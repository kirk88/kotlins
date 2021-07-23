@file:Suppress("unused")

package com.nice.common.viewmodel

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.nice.common.event.Event
import com.nice.common.event.EventDelegate
import com.nice.common.event.EventLifecycleObserver
import com.nice.common.event.EventObserver

interface ViewModelController {

    var event: Event

    fun addEventObserver(owner: LifecycleOwner, observer: EventObserver)

    fun addEventObserver(observer: EventLifecycleObserver)

}

private class DefaultViewModelController : ViewModelController {

    private val eventDelegate = EventDelegate()
    override var event: Event by eventDelegate

    override fun addEventObserver(owner: LifecycleOwner, observer: EventObserver) {
        eventDelegate.addObserver(owner, observer)
    }

    override fun addEventObserver(observer: EventLifecycleObserver) {
        eventDelegate.addObserver(observer)
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