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