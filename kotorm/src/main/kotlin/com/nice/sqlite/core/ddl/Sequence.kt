package com.nice.sqlite.core.ddl

interface MutableSequence<E> : Sequence<E> {

    fun add(element: E): Boolean

    fun remove(element: E): Boolean

    operator fun plus(element: E): MutableSequence<E> = apply {
        add(element)
    }

}

internal class LinkedSequence<E> : MutableSequence<E> {

    private val delegate = linkedSetOf<E>()

    override fun add(element: E): Boolean = delegate.add(element)

    override fun remove(element: E): Boolean = delegate.remove(element)

    override fun iterator(): Iterator<E> = delegate.iterator()

}

internal fun <E> mutableSequenceOf(vararg elements: E): MutableSequence<E> =
    LinkedSequence<E>().apply {
        for (element in elements) {
            add(element)
        }
    }

internal inline fun <T> Sequence<T>.joinTo(
    builder: StringBuilder,
    separator: String = ", ",
    prefix: String = "",
    postfix: String = "",
    transform: (T) -> String = { it.toString() }
) {
    builder.append(prefix)
    var count = 0
    for (element in this) {
        if (++count > 1) builder.append(separator)
        builder.append(transform(element))
    }
    builder.append(postfix)
}

internal class OnceIterator<T>(private val value: T) : Iterator<T> {
    private var valid = true
    override fun hasNext(): Boolean = valid
    override fun next(): T {
        valid = false
        return value
    }
}