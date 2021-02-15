@file:Suppress("unused")

package com.easy.kotlins.viewmodel

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.annotation.CallSuper
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.easy.kotlins.event.Event
import com.easy.kotlins.event.EventDelegate
import com.easy.kotlins.event.EventLifecycleObserver
import com.easy.kotlins.event.EventObserver
import com.easy.kotlins.http.DefaultOkFakerScope
import com.easy.kotlins.http.OkFaker
import com.easy.kotlins.http.OkFakerScope

/**
 * Create by LiZhanPing on 2020/8/24
 */

interface ViewModelController : OkFakerScope {
    var event: Event

    fun <T> get(block: OkFaker.Builder<T>.() -> Unit): OkFaker<T>

    fun <T> post(block: OkFaker.Builder<T>.() -> Unit): OkFaker<T>

    fun addEventObserver(owner: LifecycleOwner, observer: EventObserver)

    fun addEventObserver(observer: EventLifecycleObserver)
}

interface ViewModelEventDispatcher {

    fun onInterceptViewModelEvent(event: Event): Boolean

    fun dispatchViewModelEvent(event: Event): Boolean

    fun onViewModelEvent(event: Event): Boolean

}

private class SimpleViewModelController : ViewModelController,
    OkFakerScope by DefaultOkFakerScope() {

    private val eventDelegate = EventDelegate()
    override var event: Event by eventDelegate

    override fun <T> get(block: OkFaker.Builder<T>.() -> Unit): OkFaker<T> {
        return OkFaker.get(block).build().also { add(it) }
    }

    override fun <T> post(block: OkFaker.Builder<T>.() -> Unit): OkFaker<T> {
        return OkFaker.post(block).build().also { add(it) }
    }

    override fun addEventObserver(owner: LifecycleOwner, observer: EventObserver) {
        eventDelegate.addEventObserver(owner, observer)
    }

    override fun addEventObserver(observer: EventLifecycleObserver) {
        eventDelegate.addEventObserver(observer)
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