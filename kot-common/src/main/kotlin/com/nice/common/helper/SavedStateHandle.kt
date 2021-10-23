@file:Suppress("UNUSED")

package com.nice.common.helper

import androidx.lifecycle.SavedStateHandle

fun <T : Any> SavedStateHandle.getValue(key: String): T = requireNotNull(get(key))

fun <T : Any> SavedStateHandle.getOrDefault(key: String, defaultValue: T): T {
    return this.get<T>(key) ?: defaultValue
}

fun <T : Any> SavedStateHandle.getOrElse(key: String, defaultValue: () -> T): T {
    return this.get<T>(key) ?: defaultValue()
}