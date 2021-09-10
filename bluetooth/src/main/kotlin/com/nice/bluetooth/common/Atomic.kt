package com.nice.bluetooth.common

import java.util.concurrent.atomic.AtomicReference

fun <T> atomic(initialValue: T? = null) = AtomicReference<T>(initialValue)

internal fun <T> AtomicReference<T>.updateAndGetCompat(operation: (T) -> T?): T? {
    var prev: T?
    var next: T?
    do {
        prev = get()
        next = operation(prev)
    } while (!compareAndSet(prev, next))
    return next
}

internal fun <T> AtomicReference<T>.getAndUpdateCompat(operation: (T) -> T?): T? {
    var prev: T?
    var next: T?
    do {
        prev = get()
        next = operation(prev)
    } while (!compareAndSet(prev, next))
    return prev
}