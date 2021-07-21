package com.nice.atomic

import java.util.concurrent.atomic.AtomicLong

fun atomic(initial: Long): KAtomicLong = KAtomicLong(initial)

class KAtomicLong(initial: Long) : KAtomicNumber<Long>() {

    private val delegate = AtomicLong(initial)
    override var value: Long
        get() = delegate.get()
        set(value) {
            delegate.set(value)
        }

    override fun lazySet(newValue: Long) = delegate.lazySet(newValue)
    override fun getAndSet(newValue: Long): Long = delegate.getAndSet(newValue)
    override fun compareAndSet(expect: Long, update: Long): Boolean = delegate.compareAndSet(expect, update)
    override fun weakCompareAndSet(expect: Long, update: Long): Boolean = delegate.weakCompareAndSet(expect, update)
    override fun getAndAdd(delta: Long): Long = delegate.getAndAdd(delta)
    override fun getAndIncrement(): Long = delegate.getAndIncrement()
    override fun getAndDecrement(): Long = delegate.getAndDecrement()
    override fun incrementAndGet(): Long = delegate.incrementAndGet()
    override fun decrementAndGet(): Long = delegate.decrementAndGet()
    override fun addAndGet(delta: Long): Long = delegate.addAndGet(delta)
    override fun toByte(): Byte = delegate.toByte()
    override fun toChar(): Char = delegate.toChar()
    override fun toDouble(): Double = delegate.toDouble()
    override fun toFloat(): Float = delegate.toFloat()
    override fun toInt(): Int = delegate.toInt()
    override fun toLong(): Long = delegate.toLong()
    override fun toShort(): Short = delegate.toShort()

    override fun toString(): String = delegate.toString()

}