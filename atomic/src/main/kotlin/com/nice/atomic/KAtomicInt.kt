package com.nice.atomic

import java.util.concurrent.atomic.AtomicInteger

fun atomic(initial: Int): KAtomicInt = KAtomicInt(initial)

class KAtomicInt internal constructor(initial: Int) : KAtomicNumber<Int>() {

    private val delegate = AtomicInteger(initial)
    override var value: Int
        get() = delegate.get()
        set(value) {
            delegate.set(value)
        }

    override fun lazySet(newValue: Int) = delegate.lazySet(newValue)
    override fun getAndSet(newValue: Int): Int = delegate.getAndSet(newValue)
    override fun compareAndSet(expect: Int, update: Int): Boolean = delegate.compareAndSet(expect, update)
    override fun weakCompareAndSet(expect: Int, update: Int): Boolean = delegate.weakCompareAndSet(expect, update)
    override fun getAndAdd(delta: Int): Int = delegate.getAndAdd(delta)
    override fun getAndIncrement(): Int = delegate.getAndIncrement()
    override fun getAndDecrement(): Int = delegate.getAndDecrement()
    override fun incrementAndGet(): Int = delegate.incrementAndGet()
    override fun decrementAndGet(): Int = delegate.decrementAndGet()
    override fun addAndGet(delta: Int): Int = delegate.addAndGet(delta)
    override fun toByte(): Byte = delegate.toByte()
    override fun toChar(): Char = delegate.toChar()
    override fun toDouble(): Double = delegate.toDouble()
    override fun toFloat(): Float = delegate.toFloat()
    override fun toInt(): Int = delegate.toInt()
    override fun toLong(): Long = delegate.toLong()
    override fun toShort(): Short = delegate.toShort()

    override fun toString(): String = delegate.toString()

}

operator fun KAtomicInt.plus(value: Int): Int = addAndGet(value)
operator fun KAtomicInt.minus(value: Int): Int = addAndGet(-value)