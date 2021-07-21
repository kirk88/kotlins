package com.nice.atomic

import java.util.concurrent.atomic.AtomicReferenceArray

fun <E> atomicArrayOf(array: Array<E>): KAtomicReferenceArray<E> = KAtomicReferenceArray(array)
fun <E> atomicArrayOfNulls(size: Int): KAtomicReferenceArray<E?> = KAtomicReferenceArray(size)

class KAtomicReferenceArray<E> : KAtomicArray<E> {

    private val delegate: AtomicReferenceArray<E>

    internal constructor(array: Array<E>) {
        delegate = AtomicReferenceArray(array)
    }

    internal constructor(size: Int) {
        delegate = AtomicReferenceArray(size)
    }

    override val size: Int
        get() = delegate.length()

    override fun get(index: Int): E = delegate.get(index)
    override fun set(index: Int, newValue: E) = delegate.set(index, newValue)
    override fun lazySet(index: Int, newValue: E) = delegate.lazySet(index, newValue)
    override fun getAndSet(index: Int, newValue: E): E = delegate.getAndSet(index, newValue)
    override fun compareAndSet(index: Int, expect: E, update: E): Boolean = delegate.compareAndSet(index, expect, update)
    override fun weakCompareAndSet(index: Int, expect: E, update: E): Boolean = delegate.weakCompareAndSet(index, expect, update)
    override fun getAndUpdate(index: Int, operation: UnaryOperator<E>): E = delegate.getAndUpdate(index, operation)
    override fun updateAndGet(index: Int, operation: UnaryOperator<E>): E = delegate.updateAndGet(index, operation)
    override fun getAndAccumulate(index: Int, newValue: E, operation: BinaryOperator<E>): E = delegate.getAndAccumulate(index, newValue, operation)
    override fun accumulateAndGet(index: Int, newValue: E, operation: BinaryOperator<E>): E = delegate.accumulateAndGet(index, newValue, operation)

    override fun toString(): String = delegate.toString()

}