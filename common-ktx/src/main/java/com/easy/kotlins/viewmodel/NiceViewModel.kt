package com.easy.kotlins.viewmodel

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.annotation.CallSuper
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.easy.kotlins.event.Event
import com.easy.kotlins.event.EventObservableView
import com.easy.kotlins.event.EventProxy
import com.easy.kotlins.http.OkFaker
import com.easy.kotlins.http.OkFakerScope
import com.easy.kotlins.http.SimpleOkFakerScope

/**
 * Create by LiZhanPing on 2020/8/24
 */

interface ViewModelController : OkFakerScope {
    var event: Event

    fun get(action: OkFaker.() -> Unit): OkFaker

    fun post(action: OkFaker.() -> Unit): OkFaker

    fun observeEvent(owner: LifecycleOwner, observer: (event: Event) -> Unit)

    fun observeEvent(owner: EventObservableView)
}

private class SimpleViewModelController : ViewModelController,
    OkFakerScope by SimpleOkFakerScope() {

    private val liveEventProxy = EventProxy()
    override var event: Event by liveEventProxy

    override fun get(action: OkFaker.() -> Unit): OkFaker {
        return OkFaker.get(action).also { add(it) }
    }

    override fun post(action: OkFaker.() -> Unit): OkFaker {
        return OkFaker.post(action).also { add(it) }
    }

    override fun observeEvent(owner: LifecycleOwner, observer: (event: Event) -> Unit) {
        liveEventProxy.observe(owner) {
            if (it != null) observer(it)
        }
    }

    override fun observeEvent(owner: EventObservableView) {
        liveEventProxy.observe(owner) {
            if (it != null) owner.onEventChanged(it)
        }
    }
}

open class NiceViewModel : ViewModel(), ViewModelController by SimpleViewModelController() {
    @CallSuper
    override fun onCleared() {
        cancelAll()
    }
}

open class NiceAndroidViewModel(application: Application) : AndroidViewModel(application),
    ViewModelController by SimpleViewModelController() {

    @CallSuper
    override fun onCleared() {
        cancelAll()
    }

}

open class StatefulViewModel(val state: SavedStateHandle) : NiceViewModel()

open class StatefulAndroidViewModel(application: Application, val state: SavedStateHandle) :
    NiceAndroidViewModel(application)

@MainThread
inline fun <reified VM : ViewModel> Fragment.viewModel(
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
): VM {
    val factoryPromise = factoryProducer ?: {
        defaultViewModelProviderFactory
    }
    return ViewModelProvider(viewModelStore, factoryPromise()).get(VM::class.java)
}

@MainThread
inline fun <reified VM : ViewModel> Fragment.activityViewModel(
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
): VM {
    val factoryPromise = factoryProducer ?: {
        defaultViewModelProviderFactory
    }
    return ViewModelProvider( requireActivity().viewModelStore, factoryPromise()).get(VM::class.java)
}

@MainThread
inline fun <reified VM : ViewModel> ComponentActivity.viewModel(
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
): VM {
    val factoryPromise = factoryProducer ?: {
        defaultViewModelProviderFactory
    }
    return ViewModelProvider(viewModelStore, factoryPromise()).get(VM::class.java)
}