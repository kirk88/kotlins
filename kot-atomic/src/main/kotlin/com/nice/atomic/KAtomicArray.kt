package com.nice.atomic

interface KAtomicArray<E> {

    val size: Int

    operator fun get(index: Int): E

    operator fun set(index: Int, newValue: E)

    fun lazySet(index: Int, newValue: E)

    fun getAndSet(index: Int, newValue: E): E

    fun compareAndSet(index: Int, expect: E, update: E): Boolean

    fun weakCompareAndSet(index: Int, expect: E, update: E): Boolean

    fun getAndUpdate(index: Int, operation: UnaryOperator<E>): E

    fun updateAndGet(index: Int, operation: UnaryOperator<E>): E

    fun getAndAccumulate(index: Int, newValue: E, operation: BinaryOperator<E>): E

    fun accumulateAndGet(index: Int, newValue: E, operation: BinaryOperator<E>): E

}

abstract class KAtomicNumberArray<E : Number> : KAtomicArray<E> {

    abstract fun getAndIncrement(index: Int): E

    abstract fun getAndDecrement(index: Int): E

    abstract fun getAndAdd(index: Int, delta: E): E


    abstract fun incrementAndGet(index: Int): E

    abstract fun decrementAndGet(index: Int): E

    abstract fun addAndGet(index: Int, delta: E): E

}