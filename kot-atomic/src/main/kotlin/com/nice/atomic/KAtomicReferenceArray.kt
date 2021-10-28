package com.nice.atomic

fun <E> atomicArrayOf(vararg values: E): KAtomicArray<E> = KAtomicReferenceArray(values)
fun <E> atomicArrayOfNulls(size: Int): KAtomicArray<E?> = NullableKAtomicReferenceArray(size)

class KAtomicReferenceArray<E> internal constructor(array: Array<out E>) : KAtomicArray<E> {

    private val delegate: Array<KAtomic<E>> = Array(array.size) { atomic(array[it]) }

    override val size: Int
        get() = delegate.size

    override fun get(index: Int): E = delegate[index].value
    override fun set(index: Int, newValue: E) {
        delegate[index].value = newValue
    }

    override fun lazySet(index: Int, newValue: E) = delegate[index].lazySet(newValue)
    override fun getAndSet(index: Int, newValue: E): E = delegate[index].getAndSet(newValue)
    override fun compareAndSet(index: Int, expect: E, update: E): Boolean = delegate[index].compareAndSet(expect, update)
    override fun weakCompareAndSetPlain(index: Int, expect: E, update: E): Boolean = delegate[index].weakCompareAndSetPlain(expect, update)
    override fun getAndUpdate(index: Int, operation: UnaryOperator<E>): E = delegate[index].getAndUpdate(operation)
    override fun updateAndGet(index: Int, operation: UnaryOperator<E>): E = delegate[index].updateAndGet(operation)
    override fun getAndAccumulate(index: Int, newValue: E, operation: BinaryOperator<E>): E = delegate[index].getAndAccumulate(newValue, operation)
    override fun accumulateAndGet(index: Int, newValue: E, operation: BinaryOperator<E>): E = delegate[index].accumulateAndGet(newValue, operation)

    override fun toString(): String = delegate.contentToString()

}

class NullableKAtomicReferenceArray<E> internal constructor(size: Int) : KAtomicArray<E?> {

    private val delegate: Array<KAtomic<E?>> = Array(size) { atomic(null) }

    override val size: Int
        get() = delegate.size

    override fun get(index: Int): E? = delegate[index].value
    override fun set(index: Int, newValue: E?) {
        delegate[index].value = newValue
    }

    override fun lazySet(index: Int, newValue: E?) = delegate[index].lazySet(newValue)
    override fun getAndSet(index: Int, newValue: E?): E? = delegate[index].getAndSet(newValue)
    override fun compareAndSet(index: Int, expect: E?, update: E?): Boolean = delegate[index].compareAndSet(expect, update)
    override fun weakCompareAndSetPlain(index: Int, expect: E?, update: E?): Boolean = delegate[index].weakCompareAndSetPlain(expect, update)
    override fun getAndUpdate(index: Int, operation: UnaryOperator<E?>): E? = delegate[index].getAndUpdate(operation)
    override fun updateAndGet(index: Int, operation: UnaryOperator<E?>): E? = delegate[index].updateAndGet(operation)
    override fun getAndAccumulate(index: Int, newValue: E?, operation: BinaryOperator<E?>): E? = delegate[index].getAndAccumulate(newValue, operation)
    override fun accumulateAndGet(index: Int, newValue: E?, operation: BinaryOperator<E?>): E? = delegate[index].accumulateAndGet(newValue, operation)

    override fun toString(): String = delegate.contentToString()

}