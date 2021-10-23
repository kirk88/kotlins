@file:Suppress("UNUSED")

package com.nice.common.helper

import android.os.Bundle
import androidx.core.os.bundleOf

fun Bundle.putAll(vararg values: Pair<String, Any?>) {
    putAll(bundleOf(*values))
}

fun Bundle.toMap(): Map<String, Any?> = run {
    val map = mutableMapOf<String, Any?>()
    for (key in keySet()) {
        map[key] = get(key)
    }
    map
}

fun Bundle.toMap(destination: MutableMap<String, Any?>): Map<String, Any?> = run {
    for (key in keySet()) {
        destination[key] = get(key)
    }
    destination
}