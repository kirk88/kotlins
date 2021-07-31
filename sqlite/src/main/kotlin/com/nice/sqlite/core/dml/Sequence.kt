package com.nice.sqlite.core.dml

interface MutableSequence<T> : Sequence<T> {

    fun add(element: T): Boolean

    fun remove(element: T): Boolean

    operator fun plus(element: T): MutableSequence<T> = apply {
        add(element)
    }

}

internal class LinkedSequence<T> : MutableSequence<T> {

    private val delegate = linkedSetOf<T>()

    override fun add(element: T): Boolean = delegate.add(element)

    override fun remove(element: T): Boolean = delegate.remove(element)

    override fun iterator(): Iterator<T> = delegate.iterator()

}

internal fun <T> mutableSequenceOf(vararg elements: T) = LinkedSequence<T>().apply {
    for (element in elements) {
        add(element)
    }
}

internal class OnceIterator<T>(private val value: T) : Iterator<T> {
    private var valid = true
    override fun hasNext(): Boolean = valid
    override fun next(): T {
        valid = false
        return value
    }
}