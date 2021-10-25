@file:Suppress("UNUSED")

package com.nice.sqlite.core.ddl

interface Bag<out T> {

    operator fun iterator(): Iterator<T>

}

interface MutableBag<T> : Bag<T> {

    fun add(element: T): Boolean

    fun addAll(elements: Bag<T>): Boolean

    fun remove(element: T): Boolean

    fun removeAll(elements: Bag<T>): Boolean

}

operator fun <T> Bag<T>.plus(element: T): Bag<T> {
    if (this is MutableBag<T>) {
        add(element)
        return this
    }
    val bag = LinkedBag(this)
    bag.add(element)
    return bag
}

operator fun <T> Bag<T>.minus(element: T): Bag<T> {
    if (this is MutableBag<T>) {
        remove(element)
        return this
    }
    var removed = false
    return filterTo(LinkedBag()) {
        if (!removed && it == element) {
            removed = true; false
        } else true
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

@PublishedApi
internal class LinkedBag<T> : MutableBag<T> {

    private val delegate: LinkedHashSet<T>

    constructor() : super() {
        delegate = LinkedHashSet()
    }

    constructor(elements: Bag<T>) : super() {
        delegate = LinkedHashSet()
        addAll(elements)
    }

    override fun add(element: T): Boolean = delegate.add(element)

    override fun addAll(elements: Bag<T>): Boolean {
        var modified = false
        for (element in elements) {
            if (delegate.add(element)) {
                modified = true
            }
        }
        return modified
    }

    override fun remove(element: T): Boolean = delegate.remove(element)

    override fun removeAll(elements: Bag<T>): Boolean {
        var modified = false
        for (element in elements) {
            if (delegate.remove(element)) {
                modified = true
            }
        }
        return modified
    }

    override fun iterator(): Iterator<T> = delegate.iterator()

}

internal object EmptyBagIterator : Iterator<Nothing> {
    override fun hasNext(): Boolean = false
    override fun next(): Nothing = throw NoSuchElementException()
}

@PublishedApi
internal object EmptyBag : Bag<Nothing> {
    override fun iterator(): Iterator<Nothing> = EmptyBagIterator
}

fun <T> emptyBag(): Bag<T> = EmptyBag

fun <T> mutableBagOf(vararg elements: T): MutableBag<T> =
    if (elements.isEmpty()) LinkedBag() else LinkedBag<T>().apply {
        for (element in elements) add(element)
    }

fun <T> mutableBagOf(): MutableBag<T> = LinkedBag()

fun <T> Bag<T>.none() = !iterator().hasNext()

inline fun <T> Bag<T>.none(predicate: (T) -> Boolean): Boolean {
    if (none()) return true
    for (element in this) if (predicate(element)) return false
    return true
}

inline fun <T> Bag<T>.forEach(action: (T) -> Unit) {
    for (element in this) action(element)
}

inline fun <T, R> Bag<T>.mapTo(destination: MutableBag<R>, transform: (T) -> R): Bag<R> {
    for (element in this)
        destination.add(transform(element))
    return destination
}

inline fun <T, R> Bag<T>.map(transform: (T) -> R): Bag<R> {
    return mapTo(LinkedBag(), transform)
}

inline fun <T, C : MutableBag<in T>> Bag<T>.filterTo(destination: C, predicate: (T) -> Boolean): C {
    for (element in this)
        if (predicate(element)) destination.add(element)
    return destination
}

inline fun <T> Bag<T>.filter(predicate: (T) -> Boolean): Bag<T> {
    return filterTo(LinkedBag(), predicate)
}

inline fun <reified R, C : MutableBag<in R>> Bag<*>.filterIsInstanceTo(destination: C): C {
    for (element in this)
        if (element is R) destination.add(element)
    return destination
}

inline fun <reified R> Bag<*>.filterIsInstance(): Bag<R> {
    listOf<String>().withIndex()
    return filterIsInstanceTo(LinkedBag())
}

fun <T> Bag<T>.withIndex(): Iterable<IndexedValue<T>> {
    return IndexingIterable { iterator() }
}

internal class IndexingIterable<out T>(private val iteratorFactory: () -> Iterator<T>) : Iterable<IndexedValue<T>> {
    override fun iterator(): Iterator<IndexedValue<T>> = iteratorFactory().withIndex()
}

inline fun <T, A : Appendable> Bag<T>.joinTo(
    appendable: A,
    separator: String = ", ",
    prefix: String = "",
    postfix: String = "",
    transform: (T) -> String = { it.toString() }
): A {
    appendable.append(prefix)
    var count = 0
    for (element in this) {
        if (++count > 1) appendable.append(separator)
        appendable.append(transform(element))
    }
    appendable.append(postfix)
    return appendable
}

inline fun <T> Bag<T>.joinToString(
    separator: String = ", ",
    prefix: String = "",
    postfix: String = "",
    transform: (T) -> String = { it.toString() }
): String {
    return joinTo(StringBuilder(), separator, prefix, postfix, transform).toString()
}