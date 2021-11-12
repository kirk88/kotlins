@file:Suppress("UNUSED")

package com.nice.atomic

interface KAtomic<V> {

    var value: V

    fun lazySet(newValue: V)

    fun getAndSet(newValue: V): V

    fun compareAndSet(expect: V, update: V): Boolean

    fun weakCompareAndSetPlain(expect: V, update: V): Boolean

    fun getAndUpdate(operation: UnaryOperator<V>): V {
        var prev: V
        var next: V
        do {
            prev = value
            next = operation(prev)
        } while (!compareAndSet(prev, next))
        return prev
    }

    fun updateAndGet(operation: UnaryOperator<V>): V {
        var prev: V
        var next: V
        do {
            prev = value
            next = operation(prev)
        } while (!compareAndSet(prev, next))
        return next
    }

    fun getAndAccumulate(newValue: V, operation: BinaryOperator<V>): V {
        var prev: V
        var next: V
        do {
            prev = value
            next = operation(prev, newValue)
        } while (!compareAndSet(prev, next))
        return prev
    }

    fun accumulateAndGet(newValue: V, operation: BinaryOperator<V>): V {
        var prev: V
        var next: V
        do {
            prev = value
            next = operation(prev, newValue)
        } while (!compareAndSet(prev, next))
        return next
    }

}

abstract class KAtomicNumber<V : Number> : Number(), KAtomic<V> {

    abstract fun getAndAdd(delta: V): V

    abstract fun getAndIncrement(): V

    abstract fun getAndDecrement(): V


    abstract fun incrementAndGet(): V

    abstract fun decrementAndGet(): V

    abstract fun addAndGet(delta: V): V

}