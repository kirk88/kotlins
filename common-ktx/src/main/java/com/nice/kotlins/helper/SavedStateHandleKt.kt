@file:Suppress("unused")

package com.nice.kotlins.helper

import androidx.lifecycle.SavedStateHandle

fun <T> SavedStateHandle.getValue(key: String): T {
    return requireNotNull(this.get<T>(key)) {
        "Key $key is missing in the saved state"
    }
}

fun <T> SavedStateHandle.getOrDefault(key: String, defaultValue: T): T {
    return this.get<T>(key) ?: defaultValue
}

fun <T> SavedStateHandle.getOrElse(key: String, defaultValue: () -> T): T {
    return this.get<T>(key) ?: defaultValue()
}