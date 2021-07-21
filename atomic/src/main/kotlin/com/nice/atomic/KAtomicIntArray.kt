package com.nice.atomic

import java.util.concurrent.atomic.AtomicIntegerArray

fun atomicIntArrayOf(array: IntArray): KAtomicIntArray = KAtomicIntArray(array)
fun atomicIntArrayOf(size: Int): KAtomicIntArray = KAtomicIntArray(size)

class KAtomicIntArray : KAtomicNumberArray<Int> {

    private val delegate: AtomicIntegerArray

    internal constructor(array: IntArray) {
        delegate = AtomicIntegerArray(array)
    }

    internal constructor(size: Int) {
        delegate = AtomicIntegerArray(size)
    }

    override val size: Int
        get() = delegate.length()

    override fun get(index: Int): Int = delegate.get(index)
    override fun set(index: Int, newValue: Int) = delegate.set(index, newValue)
    override fun lazySet(index: Int, newValue: Int) = delegate.lazySet(index, newValue)
    override fun getAndSet(index: Int, newValue: Int): Int = delegate.getAndSet(index, newValue)
    override fun compareAndSet(index: Int, expect: Int, update: Int): Boolean = delegate.compareAndSet(index, expect, update)
    override fun weakCompareAndSet(index: Int, expect: Int, update: Int): Boolean = delegate.weakCompareAndSet(index, expect, update)
    override fun getAndIncrement(index: Int): Int = delegate.getAndIncrement(index)
    override fun getAndDecrement(index: Int): Int = delegate.getAndDecrement(index)
    override fun getAndAdd(index: Int, delta: Int): Int = delegate.getAndAdd(index, delta)
    override fun incrementAndGet(index: Int): Int = delegate.incrementAndGet(index)
    override fun decrementAndGet(index: Int): Int = delegate.decrementAndGet(index)
    override fun addAndGet(index: Int, delta: Int): Int = delegate.addAndGet(index, delta)
    override fun getAndUpdate(index: Int, operation: UnaryOperator<Int>): Int = delegate.getAndUpdate(index, operation)
    override fun updateAndGet(index: Int, operation: UnaryOperator<Int>): Int = delegate.updateAndGet(index, operation)
    override fun getAndAccumulate(index: Int, newValue: Int, operation: BinaryOperator<Int>): Int = delegate.getAndAccumulate(index, newValue, operation)
    override fun accumulateAndGet(index: Int, newValue: Int, operation: BinaryOperator<Int>): Int = delegate.accumulateAndGet(index, newValue, operation)

    override fun toString(): String = delegate.toString()

}