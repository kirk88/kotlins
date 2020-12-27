package com.easy.kotlins.viewmodel

import androidx.activity.ComponentActivity
import androidx.annotation.CallSuper
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.easy.kotlins.event.Event
import com.easy.kotlins.event.EventObservableView
import com.easy.kotlins.event.LiveEventDelegate
import com.easy.kotlins.helper.weak
import com.easy.kotlins.http.OkFaker
import com.easy.kotlins.http.OkFakerScope
import com.easy.kotlins.http.SimpleOkFakerScope

/**
 * Create by LiZhanPing on 2020/8/24
 */

open class NiceViewModel : ViewModel(), OkFakerScope by SimpleOkFakerScope() {

    private val liveEventDelegate = LiveEventDelegate()
    var event: Event by liveEventDelegate


    fun get(action: OkFaker.() -> Unit): OkFaker {
        return OkFaker.get(action).also { add(it) }
    }


    fun post(action: OkFaker.() -> Unit): OkFaker {
        return OkFaker.post(action).also { add(it) }
    }

    fun observeEvent(owner: LifecycleOwner, observer: (event: Event) -> Unit) {
        liveEventDelegate.observe(owner, EventObserver(observer))
    }

    fun observeEvent(owner: EventObservableView) {
        liveEventDelegate.observe(owner, EventViewObserver(owner))
    }

    @CallSuper
    override fun onCleared() {
        cancelAll()
    }


    private class EventObserver(private val observer: (event: Event) -> Unit) : Observer<Event> {

        override fun onChanged(event: Event?) {
            if (event != null) observer(event)
        }

    }

    private class EventViewObserver(owner: EventObservableView) : Observer<Event> {

        private val owner: EventObservableView? by weak { owner }

        override fun onChanged(event: Event?) {
            if (event != null) owner?.onEventChanged(event)
        }

    }
}

class StatefulViewModel(val handle: SavedStateHandle) : NiceViewModel()

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