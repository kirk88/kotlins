@file:Suppress("unused")

package com.nice.kotlins.helper

inline fun <K> Map<out K, *>.forEachKey(action: (K) -> Unit) {
    for (element in this) action(element.key)
}

inline fun <K> Map<out K, *>.forEachKeyIndexed(action: (index: Int, key: K) -> Unit) {
    for ((index, element) in entries.withIndex()) action(index, element.key)
}


inline fun <V> Map<*, V>.forEachValue(action: (V) -> Unit) {
    for (element in this) action(element.value)
}

inline fun <V> Map<*, V>.forEachValueIndexed(action: (index: Int, value: V) -> Unit) {
    for ((index, element) in entries.withIndex()) action(index, element.value)
}


fun <K, V> Map<K, V>.joinKeysToString(
    separator: CharSequence = ", ",
    prefix: CharSequence = "",
    postfix: CharSequence = "",
    limit: Int = -1,
    truncated: CharSequence = "...",
    transform: ((K) -> CharSequence)? = null,
): String {
    return keys.joinToString(separator, prefix, postfix, limit, truncated, transform)
}

fun <K, V> Map<K, V>.joinValuesToString(
    separator: CharSequence = ", ",
    prefix: CharSequence = "",
    postfix: CharSequence = "",
    limit: Int = -1,
    truncated: CharSequence = "...",
    transform: ((V) -> CharSequence)? = null,
): String {
    return values.joinToString(separator, prefix, postfix, limit, truncated, transform)
}

fun <K, V, A : Appendable> Map<K, V>.joinKeysTo(
    buffer: A,
    separator: CharSequence = ", ",
    prefix: CharSequence = "",
    postfix: CharSequence = "",
    limit: Int = -1,
    truncated: CharSequence = "...",
    transform: ((K) -> CharSequence)? = null,
): A {
    return keys.joinTo(buffer, separator, prefix, postfix, limit, truncated, transform)
}

fun <K, V, A : Appendable> Map<K, V>.joinValuesTo(
    buffer: A,
    separator: CharSequence = ", ",
    prefix: CharSequence = "",
    postfix: CharSequence = "",
    limit: Int = -1,
    truncated: CharSequence = "...",
    transform: ((V) -> CharSequence)? = null,
): A {
    return values.joinTo(buffer, separator, prefix, postfix, limit, truncated, transform)
}
