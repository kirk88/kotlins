package com.nice.atomic

import java.util.concurrent.atomic.AtomicReference

fun <V> atomic(initial: V): KAtomicReference<V> = KAtomicReference(initial)

class KAtomicReference<V> internal constructor(initial: V) : KAtomic<V> {

    private val delegate = AtomicReference(initial)

    override var value: V
        get() = delegate.get()
        set(value) {
            delegate.set(value)
        }

    override fun lazySet(newValue: V) = delegate.lazySet(newValue)
    override fun getAndSet(newValue: V): V = delegate.getAndSet(newValue)
    override fun compareAndSet(expect: V, update: V): Boolean = delegate.compareAndSet(expect, update)
    override fun weakCompareAndSetPlain(expect: V, update: V): Boolean = delegate.weakCompareAndSetPlain(expect, update)

}