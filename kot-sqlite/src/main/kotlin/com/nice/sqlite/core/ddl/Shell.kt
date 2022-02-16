@file:Suppress("UNUSED")

package com.nice.sqlite.core.ddl

interface Shell<out T> {

    val size: Int

    fun isEmpty(): Boolean = size == 0

    operator fun iterator(): Iterator<T>

}

interface MutableShell<T> : Shell<T> {

    fun add(element: T): Boolean

    fun addAll(elements: Shell<T>): Boolean

    fun remove(element: T): Boolean

    fun removeAll(elements: Shell<T>): Boolean

    fun clear()

}

operator fun <T> Shell<T>.plus(element: T): Shell<T> {
    if (this is MutableShell<T>) {
        add(element)
        return this
    }
    val shell = ArrayShell(this)
    shell.add(element)
    return shell
}

operator fun <T> Shell<T>.minus(element: T): Shell<T> {
    if (this is MutableShell<T>) {
        remove(element)
        return this
    }
    var removed = false
    return filterTo(ArrayShell()) {
        if (!removed && it == element) {
            removed = true; false
        } else true
    }
}

@PublishedApi
internal class ArrayShell<T> : MutableShell<T> {

    private val delegate: ArrayList<T>

    constructor() : super() {
        delegate = ArrayList()
    }

    constructor(elements: Shell<T>) : super() {
        delegate = ArrayList()
        addAll(elements)
    }

    override val size: Int
        get() = delegate.size

    override fun add(element: T): Boolean = delegate.add(element)

    override fun addAll(elements: Shell<T>): Boolean {
        var modified = false
        for (element in elements) {
            if (delegate.add(element)) {
                modified = true
            }
        }
        return modified
    }

    override fun remove(element: T): Boolean = delegate.remove(element)

    override fun removeAll(elements: Shell<T>): Boolean {
        var modified = false
        for (element in elements) {
            if (delegate.remove(element)) {
                modified = true
            }
        }
        return modified
    }

    override fun clear() = delegate.clear()

    override fun iterator(): Iterator<T> = delegate.iterator()

}

internal class OnceIterator<T>(private val value: T) : Iterator<T> {
    private var valid = true
    override fun hasNext(): Boolean = valid
    override fun next(): T {
        valid = false
        return value
    }
}

internal object EmptyShellIterator : Iterator<Nothing> {
    override fun hasNext(): Boolean = false
    override fun next(): Nothing = throw NoSuchElementException()
}

@PublishedApi
internal object EmptyShell : Shell<Nothing> {
    override val size: Int = 0
    override fun iterator(): Iterator<Nothing> = EmptyShellIterator
}

fun <T> emptyShell(): Shell<T> = EmptyShell

fun <T> mutableShellOf(vararg elements: T): MutableShell<T> =
    if (elements.isEmpty()) ArrayShell() else ArrayShell<T>().apply {
        for (element in elements) add(element)
    }

fun <T> mutableShellOf(): MutableShell<T> = ArrayShell()

fun <T> shellOf(): Shell<T> = EmptyShell

fun <T> shellOf(vararg elements: T): Shell<T> = object : Shell<T> {
    override val size: Int = elements.size
    override fun iterator(): Iterator<T> = elements.iterator()
}

inline fun <T> Shell<T>.none(predicate: (T) -> Boolean): Boolean {
    if (isEmpty()) return true
    for (element in this) if (predicate(element)) return false
    return true
}

inline fun <T> Shell<T>.forEach(action: (T) -> Unit) {
    for (element in this) action(element)
}

inline fun <T, R> Shell<T>.mapTo(destination: MutableShell<R>, transform: (T) -> R): Shell<R> {
    for (element in this)
        destination.add(transform(element))
    return destination
}

inline fun <T, R> Shell<T>.map(transform: (T) -> R): Shell<R> {
    return mapTo(ArrayShell(), transform)
}

inline fun <T, C : MutableShell<in T>> Shell<T>.filterTo(destination: C, predicate: (T) -> Boolean): C {
    for (element in this)
        if (predicate(element)) destination.add(element)
    return destination
}

inline fun <T> Shell<T>.filter(predicate: (T) -> Boolean): Shell<T> {
    return filterTo(ArrayShell(), predicate)
}

inline fun <reified R, C : MutableShell<in R>> Shell<*>.filterIsInstanceTo(destination: C): C {
    for (element in this)
        if (element is R) destination.add(element)
    return destination
}

inline fun <reified R> Shell<*>.filterIsInstance(): Shell<R> {
    listOf<String>().withIndex()
    return filterIsInstanceTo(ArrayShell())
}

fun <T> Shell<T>.withIndex(): Iterable<IndexedValue<T>> {
    return IndexingIterable { iterator() }
}

internal class IndexingIterable<out T>(private val iteratorFactory: () -> Iterator<T>) : Iterable<IndexedValue<T>> {
    override fun iterator(): Iterator<IndexedValue<T>> = iteratorFactory().withIndex()
}

inline fun <T, A : Appendable> Shell<T>.joinTo(
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

inline fun <T> Shell<T>.joinToString(
    separator: String = ", ",
    prefix: String = "",
    postfix: String = "",
    transform: (T) -> String = { it.toString() }
): String {
    return joinTo(StringBuilder(), separator, prefix, postfix, transform).toString()
}