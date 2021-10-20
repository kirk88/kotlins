package com.nice.atomic

import java.util.concurrent.atomic.AtomicBoolean

fun atomic(initial: Boolean): KAtomicBoolean = KAtomicBoolean(initial)

class KAtomicBoolean internal constructor(initial: Boolean) : KAtomic<Boolean> {

    private val delegate = AtomicBoolean(initial)

    override var value: Boolean
        get() = delegate.get()
        set(value) {
            delegate.set(value)
        }

    override fun lazySet(newValue: Boolean) = delegate.lazySet(newValue)
    override fun getAndSet(newValue: Boolean): Boolean = delegate.getAndSet(newValue)
    override fun compareAndSet(expect: Boolean, update: Boolean): Boolean = delegate.compareAndSet(expect, update)
    override fun weakCompareAndSet(expect: Boolean, update: Boolean): Boolean = delegate.weakCompareAndSet(expect, update)

    override fun toString(): String = delegate.toString()

}