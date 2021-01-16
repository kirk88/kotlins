@file:Suppress("unused")

package com.easy.kotlins.viewmodel

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.annotation.CallSuper
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.easy.kotlins.event.Event
import com.easy.kotlins.event.EventObservableOwner
import com.easy.kotlins.event.EventProxy
import com.easy.kotlins.http.OkFaker
import com.easy.kotlins.http.OkManagerScope
import com.easy.kotlins.http.SimpleOkManagerScope

/**
 * Create by LiZhanPing on 2020/8/24
 */

interface ViewModelController : OkManagerScope {
    var event: Event

    fun <T> get(action: OkFaker<T>.() -> Unit): OkFaker<T>

    fun <T> post(action: OkFaker<T>.() -> Unit): OkFaker<T>

    fun observeEvent(owner: LifecycleOwner, observer: (event: Event) -> Unit)

    fun observeEvent(owner: EventObservableOwner)
}

interface ViewModelEventObservableOwner : EventObservableOwner {

    fun onInterceptViewModelEvent(event: Event): Boolean

    fun dispatchViewModelEvent(event: Event): Boolean

    fun onViewModelEvent(event: Event)

}

private class SimpleViewModelController : ViewModelController,
    OkManagerScope by SimpleOkManagerScope() {

    private val eventProxy = EventProxy()
    override var event: Event by eventProxy

    override fun <T> get(action: OkFaker<T>.() -> Unit): OkFaker<T> {
        return OkFaker.get(action).also { add(it) }
    }

    override fun <T> post(action: OkFaker<T>.() -> Unit): OkFaker<T> {
        return OkFaker.post(action).also { add(it) }
    }

    override fun observeEvent(owner: LifecycleOwner, observer: (event: Event) -> Unit) {
        eventProxy.observe(owner, observer)
    }

    override fun observeEvent(owner: EventObservableOwner) {
        eventProxy.observe(owner)
    }

}

open class NiceViewModel : ViewModel(), ViewModelController by SimpleViewModelController() {

    @CallSuper
    override fun onCleared() {
        clear()
    }

}

open class NiceAndroidViewModel(application: Application) : AndroidViewModel(application),
    ViewModelController by SimpleViewModelController() {

    @CallSuper
    override fun onCleared() {
        clear()
    }

}

open class StatefulViewModel(val state: SavedStateHandle) : NiceViewModel()

open class StatefulAndroidViewModel(application: Application, val state: SavedStateHandle) :
    NiceAndroidViewModel(application)

@MainThread
inline fun <reified VM : ViewModel> Fragment.viewModel(
    factoryProducer: ViewModelProvider.Factory? = null
): VM {
    return ViewModelProvider(
        viewModelStore, factoryProducer
            ?: defaultViewModelProviderFactory
    ).get(VM::class.java)
}

@MainThread
inline fun <reified VM : ViewModel> Fragment.activityViewModel(
    factoryProducer: ViewModelProvider.Factory? = null
): VM {
    return ViewModelProvider(
        requireActivity().viewModelStore, factoryProducer
            ?: requireActivity().defaultViewModelProviderFactory
    ).get(VM::class.java)
}

@MainThread
inline fun <reified VM : ViewModel> ComponentActivity.viewModel(
    factoryProducer: ViewModelProvider.Factory? = null
): VM {
    return ViewModelProvider(
        viewModelStore, factoryProducer
            ?: defaultViewModelProviderFactory
    ).get(VM::class.java)
}