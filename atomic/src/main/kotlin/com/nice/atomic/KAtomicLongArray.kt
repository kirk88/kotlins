package com.nice.atomic

import java.util.concurrent.atomic.AtomicLongArray

fun atomicLongArrayOf(array: LongArray): KAtomicLongArray = KAtomicLongArray(array)
fun atomicLongArrayOf(size: Int): KAtomicLongArray = KAtomicLongArray(size)

class KAtomicLongArray : KAtomicNumberArray<Long> {

    private val delegate: AtomicLongArray

    internal constructor(array: LongArray) {
        delegate = AtomicLongArray(array)
    }

    internal constructor(size: Int) {
        delegate = AtomicLongArray(size)
    }

    override val size: Int
        get() = delegate.length()

    override fun get(index: Int): Long = delegate.get(index)
    override fun set(index: Int, newValue: Long) = delegate.set(index, newValue)
    override fun lazySet(index: Int, newValue: Long) = delegate.lazySet(index, newValue)
    override fun getAndSet(index: Int, newValue: Long): Long = delegate.getAndSet(index, newValue)
    override fun compareAndSet(index: Int, expect: Long, update: Long): Boolean = delegate.compareAndSet(index, expect, update)
    override fun weakCompareAndSet(index: Int, expect: Long, update: Long): Boolean = delegate.weakCompareAndSet(index, expect, update)
    override fun getAndIncrement(index: Int): Long = delegate.getAndIncrement(index)
    override fun getAndDecrement(index: Int): Long = delegate.getAndDecrement(index)
    override fun getAndAdd(index: Int, delta: Long): Long = delegate.getAndAdd(index, delta)
    override fun incrementAndGet(index: Int): Long = delegate.incrementAndGet(index)
    override fun decrementAndGet(index: Int): Long = delegate.decrementAndGet(index)
    override fun addAndGet(index: Int, delta: Long): Long = delegate.addAndGet(index, delta)
    override fun getAndUpdate(index: Int, operation: UnaryOperator<Long>): Long = delegate.getAndUpdate(index, operation)
    override fun updateAndGet(index: Int, operation: UnaryOperator<Long>): Long = delegate.updateAndGet(index, operation)
    override fun getAndAccumulate(index: Int, newValue: Long, operation: BinaryOperator<Long>): Long = delegate.getAndAccumulate(index, newValue, operation)
    override fun accumulateAndGet(index: Int, newValue: Long, operation: BinaryOperator<Long>): Long = delegate.accumulateAndGet(index, newValue, operation)

    override fun toString(): String = delegate.toString()

}