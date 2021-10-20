package com.nice.atomic

fun atomicLongArrayOf(array: Array<Long>): KAtomicLongArray = KAtomicLongArray(array.toLongArray())
fun atomicLongArrayOf(vararg values: Long): KAtomicLongArray = KAtomicLongArray(longArrayOf(*values))
fun atomicLongArrayOf(size: Int): KAtomicLongArray = KAtomicLongArray(size)

class KAtomicLongArray : KAtomicNumberArray<Long> {

    private val delegate: Array<KAtomicLong>

    internal constructor(array: LongArray) {
        delegate = Array(array.size) { atomic(array[it]) }
    }

    internal constructor(size: Int) {
        delegate = Array(size) { atomic(0.toLong()) }
    }

    override val size: Int
        get() = delegate.size

    override fun get(index: Int): Long = delegate[index].value
    override fun set(index: Int, newValue: Long) {
        delegate[index].value = newValue
    }

    override fun lazySet(index: Int, newValue: Long) = delegate[index].lazySet(newValue)
    override fun getAndSet(index: Int, newValue: Long): Long = delegate[index].getAndSet(newValue)
    override fun compareAndSet(index: Int, expect: Long, update: Long): Boolean = delegate[index].compareAndSet(expect, update)
    override fun weakCompareAndSet(index: Int, expect: Long, update: Long): Boolean = delegate[index].weakCompareAndSet(expect, update)
    override fun getAndIncrement(index: Int): Long = delegate[index].getAndIncrement()
    override fun getAndDecrement(index: Int): Long = delegate[index].getAndDecrement()
    override fun getAndAdd(index: Int, delta: Long): Long = delegate[index].getAndAdd(delta)
    override fun incrementAndGet(index: Int): Long = delegate[index].incrementAndGet()
    override fun decrementAndGet(index: Int): Long = delegate[index].decrementAndGet()
    override fun addAndGet(index: Int, delta: Long): Long = delegate[index].addAndGet(delta)
    override fun getAndUpdate(index: Int, operation: UnaryOperator<Long>): Long = delegate[index].getAndUpdate(operation)
    override fun updateAndGet(index: Int, operation: UnaryOperator<Long>): Long = delegate[index].updateAndGet(operation)
    override fun getAndAccumulate(index: Int, newValue: Long, operation: BinaryOperator<Long>): Long = delegate[index].getAndAccumulate(newValue, operation)
    override fun accumulateAndGet(index: Int, newValue: Long, operation: BinaryOperator<Long>): Long = delegate[index].accumulateAndGet(newValue, operation)

    override fun toString(): String = delegate.contentToString()

}