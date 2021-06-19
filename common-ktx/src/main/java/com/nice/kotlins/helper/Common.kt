@file:Suppress("unused")

package com.nice.kotlins.helper

import android.os.Looper

val isMainThread: Boolean
    get() = Looper.getMainLooper() == Looper.myLooper()

fun <T : Any> Boolean.opt(right: T, wrong: T): T {
    return if (this) right else wrong
}

fun <T : Any> Boolean.opt(right: () -> T, wrong: () -> T): T {
    return if (this) right() else wrong()
}

fun <T : Any?> Boolean.optNulls(right: T?, wrong: T?): T? {
    return if (this) right else wrong
}

fun <T : Any?> Boolean.optNulls(right: () -> T?, wrong: () -> T?): T? {
    return if (this) right() else wrong()
}

fun Any?.toStringOrEmpty(): String = this?.toString().orEmpty()
