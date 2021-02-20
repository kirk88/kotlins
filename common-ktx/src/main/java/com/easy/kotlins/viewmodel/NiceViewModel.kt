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
interface ViewModelController : OkFakerScope, ViewModelHttpRequester {

    var event: Event

    fun addEventObserver(owner: LifecycleOwner, observer: EventObserver)

    fun addEventObserver(observer: EventLifecycleObserver)

}

interface ViewModelHttpRequester : OkFakerScope {

    fun <T> get(block: OkFaker.Builder<T>.() -> Unit): OkFaker<T> {
        return OkFaker.get(block).build().also { add(it) }
    }

    fun <T> post(block: OkFaker.Builder<T>.() -> Unit): OkFaker<T> {
        return OkFaker.post(block).build().also { add(it) }
    }

    fun <T> delete(block: OkFaker.Builder<T>.() -> Unit): OkFaker<T> {
        return OkFaker.delete(block).build().also { add(it) }
    }

    fun <T> put(block: OkFaker.Builder<T>.() -> Unit): OkFaker<T> {
        return OkFaker.put(block).build().also { add(it) }
    }

    fun <T> head(block: OkFaker.Builder<T>.() -> Unit): OkFaker<T> {
        return OkFaker.head(block).build().also { add(it) }
    }

    fun <T> patch(block: OkFaker.Builder<T>.() -> Unit): OkFaker<T> {
        return OkFaker.patch(block).build().also { add(it) }
    }

}

private class DefaultViewModelController : ViewModelController,
    OkFakerScope by DefaultOkFakerScope() {

    private val eventDelegate = EventDelegate()
    override var event: Event by eventDelegate

    override fun addEventObserver(owner: LifecycleOwner, observer: EventObserver) {
        eventDelegate.addEventObserver(owner, observer)
    }

    override fun addEventObserver(observer: EventLifecycleObserver) {
        eventDelegate.addEventObserver(observer)
    }

}

open class NiceViewModel : ViewModel(), ViewModelController by DefaultViewModelController() {

    @CallSuper
    override fun onCleared() {
        clear()
    }

}

open class NiceAndroidViewModel(application: Application) : AndroidViewModel(application),
    ViewModelController by DefaultViewModelController() {

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