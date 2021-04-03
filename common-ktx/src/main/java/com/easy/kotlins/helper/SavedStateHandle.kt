@file:Suppress("unused")

package com.easy.kotlins.helper

import androidx.lifecycle.SavedStateHandle

fun <T> SavedStateHandle.getOrDefault(key: String, defaultValue: T): T{
    return this.get<T>(key) ?: defaultValue
}

fun <T> SavedStateHandle.getOrElse(key: String, defaultValue: ()-> T): T{
    return this.get<T>(key) ?: defaultValue()
}