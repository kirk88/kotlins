package com.nice.atomic

fun atomicIntArrayOf(array: Array<Int>): KAtomicIntArray = KAtomicIntArray(array.toIntArray())
fun atomicIntArrayOf(vararg values: Int): KAtomicIntArray = KAtomicIntArray(intArrayOf(*values))
fun atomicIntArrayOf(size: Int): KAtomicIntArray = KAtomicIntArray(size)

class KAtomicIntArray : KAtomicNumberArray<Int> {

    private val delegate: Array<KAtomicInt>

    internal constructor(array: IntArray) {
        delegate = Array(array.size) { atomic(array[it]) }
    }

    internal constructor(size: Int) {
        delegate = Array(size) { atomic(0) }
    }

    override val size: Int
        get() = delegate.size

    override fun get(index: Int): Int = delegate[index].value
    override fun set(index: Int, newValue: Int) {
        delegate[index].value = newValue
    }

    override fun lazySet(index: Int, newValue: Int) = delegate[index].lazySet(newValue)
    override fun getAndSet(index: Int, newValue: Int): Int = delegate[index].getAndSet(newValue)
    override fun compareAndSet(index: Int, expect: Int, update: Int): Boolean = delegate[index].compareAndSet(expect, update)
    override fun weakCompareAndSet(index: Int, expect: Int, update: Int): Boolean = delegate[index].weakCompareAndSet(expect, update)
    override fun getAndIncrement(index: Int): Int = delegate[index].getAndIncrement()
    override fun getAndDecrement(index: Int): Int = delegate[index].getAndDecrement()
    override fun getAndAdd(index: Int, delta: Int): Int = delegate[index].getAndAdd(delta)
    override fun incrementAndGet(index: Int): Int = delegate[index].incrementAndGet()
    override fun decrementAndGet(index: Int): Int = delegate[index].decrementAndGet()
    override fun addAndGet(index: Int, delta: Int): Int = delegate[index].addAndGet(delta)
    override fun getAndUpdate(index: Int, operation: UnaryOperator<Int>): Int = delegate[index].getAndUpdate(operation)
    override fun updateAndGet(index: Int, operation: UnaryOperator<Int>): Int = delegate[index].updateAndGet(operation)
    override fun getAndAccumulate(index: Int, newValue: Int, operation: BinaryOperator<Int>): Int = delegate[index].getAndAccumulate(newValue, operation)
    override fun accumulateAndGet(index: Int, newValue: Int, operation: BinaryOperator<Int>): Int = delegate[index].accumulateAndGet(newValue, operation)

    override fun toString(): String = delegate.contentToString()

}