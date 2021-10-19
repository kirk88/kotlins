@file:Suppress("unused")

package com.nice.common.helper

import android.os.Looper

val isMainThread: Boolean
    get() = Looper.getMainLooper() == Looper.myLooper()

fun Any?.toStringOrEmpty(): String = this?.toString().orEmpty()