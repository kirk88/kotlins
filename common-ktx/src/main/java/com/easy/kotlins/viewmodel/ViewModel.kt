package com.easy.kotlins.viewmodel

import androidx.activity.ComponentActivity
import androidx.annotation.CallSuper
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.easy.kotlins.event.Event
import com.easy.kotlins.event.EventObservableView
import com.easy.kotlins.event.LiveEventProxy
import com.easy.kotlins.event.SingleLiveEvent
import com.easy.kotlins.http.OkFaker
import com.easy.kotlins.http.OkFakerScope
import com.easy.kotlins.http.SimpleOkFakerScope

/**
 * Create by LiZhanPing on 2020/8/24
 */

open class ViewModel : ViewModel(), OkFakerScope by SimpleOkFakerScope() {

    private val liveEventProxy = LiveEventProxy { SingleLiveEvent() }
    var event: Event? by liveEventProxy


    fun get(action: OkFaker.() -> Unit): OkFaker {
        val faker = OkFaker.get(action)
        return faker.tag?.let {
            add(it, faker)
        } ?: add(javaClass.simpleName, faker)
    }


    fun post(action: OkFaker.() -> Unit): OkFaker {
        val faker = OkFaker.post(action)
        return faker.tag?.let {
            add(it, faker)
        } ?: add(javaClass.simpleName, faker)
    }

    fun observeEvent(owner: LifecycleOwner, observer: (event: Event) -> Unit) {
        liveEventProxy.observe(owner) {
            if (it != null) {
                observer(it)
            }
        }
    }

    fun observeEvent(owner: EventObservableView) {
        liveEventProxy.observe(owner) {
            if (it != null) {
                owner.onEventChanged(it)
            }
        }
    }

    @CallSuper
    override fun onCleared() {
        clear()
    }
}

class StateViewModel(val handle: SavedStateHandle) : ViewModel()

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