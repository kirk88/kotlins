@file:Suppress("unused")

package com.nice.kotlins.event

import android.annotation.SuppressLint
import android.os.Looper
import androidx.annotation.MainThread
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.internal.SafeIterableMap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

/**
 * LiveEventData is a data holder class that can be observed within a given lifecycle.
 * This means that an [Observer] can be added in a pair with a [LifecycleOwner], and
 * this observer will be notified about modifications of the wrapped data only if the paired
 * LifecycleOwner is in active state. LifecycleOwner is considered as active, if its state is
 * [Lifecycle.State.STARTED] or [Lifecycle.State.RESUMED]. An observer added via
 * [.observeForever] is considered as always active and thus will be always notified
 * about modifications. For those observers, you should manually call
 * [.removeObserver].
 *
 *
 *
 * An observer added with a Lifecycle will be automatically removed if the corresponding
 * Lifecycle moves to [Lifecycle.State.DESTROYED] state. This is especially useful for
 * activities and fragments where they can safely observe LiveEventData and not worry about leaks:
 * they will be instantly unsubscribed when they are destroyed.
 *
 *
 *
 * In addition, LiveEventData has [LiveEvent.onActive] and
 * [LiveEvent.onInactive] methods
 * to get notified when number of active [Observer]s change between 0 and 1.
 * This allows LiveEventData to release any heavy resources when it does not have any Observers that
 * are actively observing.
 *
 *
 * This class is designed for sharing data between different modules in your application
 * in a decoupled fashion.
 *
 * @param <T> The type of data held by this instance
</T> */
@SuppressLint("RestrictedApi")
open class LiveEvent<T> {
    private val observers: SafeIterableMap<Observer<in T>, ObserverWrapper> =
        SafeIterableMap()

    // how many observers are in active state
    private var activeCount = 0

    @Volatile
    private var data: Any?
    private var version: Int
    private var dispatchingValue = false
    private var dispatchInvalidated = false

    constructor() {
        data = NOT_SET
        version = START_VERSION
    }

    constructor(value: T) {
        data = value
        version = START_VERSION + 1
    }

    private fun considerNotify(observer: ObserverWrapper) {
        if (!observer.active) {
            return
        }
        // Check latest state b4 dispatch. Maybe it changed state but we didn't get the event yet.
        //
        // we still first check observer.active to keep it as the entrance for events. So even if
        // the observer moved to an active state, if we've not received that event, we better not
        // notify for a more predictable notification order.
        if (!observer.shouldBeActive()) {
            observer.activeStateChanged(false)
            return
        }
        if (observer.lastVersion >= version) {
            return
        }
        observer.lastVersion = version
        @Suppress("UNCHECKED_CAST")
        observer.observer.onChanged(data as T)
    }

    private fun dispatchingValue(initiator: ObserverWrapper?) {
        if (dispatchingValue) {
            dispatchInvalidated = true
            return
        }
        dispatchingValue = true
        do {
            dispatchInvalidated = false
            if (initiator != null) {
                considerNotify(initiator)
            } else {
                @Suppress("INACCESSIBLE_TYPE")
                val iterator: Iterator<Map.Entry<Observer<in T>, ObserverWrapper>> =
                    observers.iteratorWithAdditions()
                while (iterator.hasNext()) {
                    considerNotify(iterator.next().value)
                    if (dispatchInvalidated) {
                        break
                    }
                }
            }
        } while (dispatchInvalidated)
        dispatchingValue = false
    }

    @MainThread
    private fun observe(owner: LifecycleOwner, observer: Observer<in T>, isSticky: Boolean) {
        if (owner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
            // ignore
            return
        }
        val wrapper = LifecycleBoundObserver(owner, observer, isSticky)
        val existing: ObserverWrapper? = observers.putIfAbsent(observer, wrapper)
        require(!(existing != null && !existing.isAttachedTo(owner))) { "Cannot add the same observer" + " with different lifecycles" }
        if (existing != null) {
            return
        }
        owner.lifecycle.addObserver(wrapper)
    }

    @MainThread
    private fun observeActive(owner: LifecycleOwner, observer: Observer<in T>, isSticky: Boolean) {
        if (owner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
            // ignore
            return
        }
        val wrapper = LifecycleActiveObserver(owner, observer, isSticky)
        val existing: ObserverWrapper? = observers.putIfAbsent(observer, wrapper)
        require(!(existing != null && !existing.isAttachedTo(owner))) { "Cannot add the same observer" + " with different lifecycles" }
        if (existing != null) {
            return
        }
        owner.lifecycle.addObserver(wrapper)
    }

    @MainThread
    private fun observeForever(observer: Observer<in T>, isSticky: Boolean) {
        val wrapper = AlwaysActiveObserver(observer, isSticky)
        val existing: ObserverWrapper? = observers.putIfAbsent(observer, wrapper)
        require(existing !is LifecycleBoundObserver) { "Cannot add the same observer" + " with different lifecycles" }
        if (existing != null) {
            return
        }
        wrapper.activeStateChanged(true)
    }

    /**
     * Adds the given observer to the observers list within the lifespan of the given
     * owner. The events are dispatched on the main thread. If LiveEventData already has data
     * set, it will be delivered to the observer.
     *
     *
     * The observer will only receive events if the owner is in [Lifecycle.State.STARTED]
     * or [Lifecycle.State.RESUMED] state (active).
     *
     *
     * If the owner moves to the [Lifecycle.State.DESTROYED] state, the observer will
     * automatically be removed.
     *
     *
     * When data changes while the `owner` is not active, it will not receive any updates.
     * If it becomes active again, it will receive the last available data automatically.
     *
     *
     * LiveEventData keeps a strong reference to the observer and the owner as long as the
     * given LifecycleOwner is not destroyed. When it is destroyed, LiveEventData removes references
     * to
     * the observer &amp; the owner.
     *
     *
     * If the given owner is already in [Lifecycle.State.DESTROYED] state, LiveEventData
     * ignores the call.
     *
     *
     * If the given owner, observer tuple is already in the list, the call is ignored.
     * If the observer is already in the list with another owner, LiveEventData throws an
     * [IllegalArgumentException].
     *
     * @param owner    The LifecycleOwner which controls the observer
     * @param observer The observer that will receive the events
     */
    @MainThread
    open fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        observe(owner, observer, false)
    }

    @MainThread
    open fun observeSticky(owner: LifecycleOwner, observer: Observer<in T>) {
        observe(owner, observer, true)
    }

    /**
     * Adds the given observer to the observers list within the lifespan of the given
     * owner. The events are dispatched on the main thread. If LiveEventData already has data
     * set, it will be delivered to the observer.
     *
     *
     * The observer will only receive events if the owner is in [Lifecycle.State.CREATED]
     * or [Lifecycle.State.STARTED] or [Lifecycle.State.RESUMED] state (active).
     *
     *
     * If the owner moves to the [Lifecycle.State.DESTROYED] state, the observer will
     * automatically be removed.
     *
     *
     * When data changes while the `owner` is not active, it will not receive any updates.
     * If it becomes active again, it will receive the last available data automatically.
     *
     *
     * LiveEventData keeps a strong reference to the observer and the owner as long as the
     * given LifecycleOwner is not destroyed. When it is destroyed, LiveEventData removes references
     * to
     * the observer &amp; the owner.
     *
     *
     * If the given owner is already in [Lifecycle.State.DESTROYED] state, LiveEventData
     * ignores the call.
     *
     *
     * If the given owner, observer tuple is already in the list, the call is ignored.
     * If the observer is already in the list with another owner, LiveEventData throws an
     * [IllegalArgumentException].
     *
     * @param owner    The LifecycleOwner which controls the observer
     * @param observer The observer that will receive the events
     */
    @MainThread
    open fun observeActive(owner: LifecycleOwner, observer: Observer<in T>) {
        observeActive(owner, observer, false)
    }

    @MainThread
    open fun observeActiveSticky(owner: LifecycleOwner, observer: Observer<in T>) {
        observeActive(owner, observer, true)
    }

    /**
     * Adds the given observer to the observers list. This call is similar to
     * [LiveEvent.observe] with a LifecycleOwner, which
     * is always active. This means that the given observer will receive all events and will never
     * be automatically removed. You should manually call [.removeObserver] to stop
     * observing this LiveEventData.
     * While LiveEventData has one of such observers, it will be considered
     * as active.
     *
     *
     * If the observer was already added with an owner to this LiveEventData, LiveEventData throws an
     * [IllegalArgumentException].
     *
     * @param observer The observer that will receive the events
     */
    @MainThread
    open fun observeForever(observer: Observer<in T>) {
        observeForever(observer, false)
    }

    @MainThread
    open fun observeForeverSticky(observer: Observer<in T>) {
        observeForever(observer, true)
    }

    /**
     * Removes the given observer from the observers list.
     *
     * @param observer The Observer to receive events.
     */
    @MainThread
    fun removeObserver(observer: Observer<in T>) {
        assertMainThread("removeObserver")
        val removed: ObserverWrapper = observers.remove(observer)
            ?: return
        removed.detachObserver()
        removed.activeStateChanged(false)
    }

    /**
     * Removes all observers that are tied to the given [LifecycleOwner].
     *
     * @param owner The `LifecycleOwner` scope for the observers to be removed.
     */
    @MainThread
    fun removeObservers(owner: LifecycleOwner) {
        assertMainThread("removeObservers")
        for ((key, value1) in observers) {
            if (value1.isAttachedTo(owner)) {
                removeObserver(key)
            }
        }
    }

    /**
     * Posts a task to a main thread to set the given value. So if you have a following code
     * executed in the main thread:
     *
     * <pre class="prettyprint">
     * liveData.postValue("a");
     * liveData.setValue("b");
    </pre> *
     *
     *
     * The value "b" would be set at first and later the main thread would override it with
     * the value "a".
     *
     *
     * If you called this method multiple times before a main thread executed a posted task, only
     * the last value would be dispatched.
     *
     * @param value The new value
     */
    protected open fun postValue(value: T) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            setValue(value)
        } else {
            ArchTaskExecutor.getInstance().postToMainThread(PostValueRunnable(value))
        }
    }// noinspection unchecked

    /**
     * Returns the current value.
     * Note that calling this method on a background thread does not guarantee that the latest
     * value set will be received.
     *
     * @return the current value
     */
    fun getValue(): T? {
        @Suppress("UNCHECKED_CAST")
        return if (data !== NOT_SET) {
            data as T
        } else null
    }

    /**
     * Sets the value. If there are active observers, the value will be dispatched to them.
     *
     *
     * This method must be called from the main thread. If you need set a value from a background
     * thread, you can use [.postValue]
     *
     * @param value The new value
     */
    @MainThread
    protected open fun setValue(value: T?) {
        assertMainThread("setValue")
        version++
        data = value
        dispatchingValue(null)
    }

    /**
     * Called when the number of active observers change to 1 from 0.
     *
     *
     * This callback can be used to know that this LiveEventData is being used thus should be kept
     * up to date.
     */
    protected fun onActive() {}

    /**
     * Called when the number of active observers change from 1 to 0.
     *
     *
     * This does not mean that there are no observers left, there may still be observers but their
     * lifecycle states aren't [Lifecycle.State.STARTED] or [Lifecycle.State.RESUMED]
     * (like an Activity in the back stack).
     *
     *
     * You can check if there are observers via [.hasObservers].
     */
    protected fun onInactive() {}

    /**
     * Returns true if this LiveEventData has observers.
     *
     * @return true if this LiveEventData has observers
     */
    fun hasObservers(): Boolean {
        return observers.size() > 0
    }

    /**
     * Returns true if this LiveEventData has active observers.
     *
     * @return true if this LiveEventData has active observers
     */
    fun hasActiveObservers(): Boolean {
        return activeCount > 0
    }

    private inner class LifecycleActiveObserver(
        owner: LifecycleOwner,
        observer: Observer<in T>,
        isSticky: Boolean
    ) : LifecycleBoundObserver(owner, observer, isSticky) {
        override fun shouldBeActive(): Boolean {
            return owner.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)
        }
    }

    private open inner class LifecycleBoundObserver(
        val owner: LifecycleOwner,
        observer: Observer<in T>,
        isSticky: Boolean
    ) : ObserverWrapper(observer, isSticky), LifecycleEventObserver {
        override fun shouldBeActive(): Boolean {
            return owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        }

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (owner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
                removeObserver(observer)
                return
            }
            activeStateChanged(shouldBeActive())
        }

        override fun isAttachedTo(owner: LifecycleOwner): Boolean {
            return this.owner === owner
        }

        override fun detachObserver() {
            owner.lifecycle.removeObserver(this)
        }
    }

    private abstract inner class ObserverWrapper(
        val observer: Observer<in T>,
        isSticky: Boolean
    ) {
        var active = false
        var lastVersion: Int = if (isSticky) START_VERSION else version

        abstract fun shouldBeActive(): Boolean
        open fun isAttachedTo(owner: LifecycleOwner): Boolean {
            return false
        }

        open fun detachObserver() {}

        fun activeStateChanged(newActive: Boolean) {
            if (newActive == active) {
                return
            }
            // immediately set active state, so we'd never dispatch anything to inactive
            // owner
            active = newActive
            val wasInactive = activeCount == 0
            activeCount += if (active) 1 else -1
            if (wasInactive && active) {
                onActive()
            }
            if (activeCount == 0 && !active) {
                onInactive()
            }
            if (active) {
                dispatchingValue(this)
            }
        }

    }

    private inner class AlwaysActiveObserver(
        observer: Observer<in T>,
        isSticky: Boolean
    ) : ObserverWrapper(observer, isSticky) {
        override fun shouldBeActive(): Boolean {
            return true
        }
    }

    private inner class PostValueRunnable(private val newValue: T?) : Runnable {
        override fun run() {
            setValue(newValue)
        }
    }

    companion object {
        const val START_VERSION = -1
        private val NOT_SET = Any()
        private fun assertMainThread(methodName: String) {
            check(ArchTaskExecutor.getInstance().isMainThread) { "Cannot invoke $methodName on a background thread" }
        }
    }
}