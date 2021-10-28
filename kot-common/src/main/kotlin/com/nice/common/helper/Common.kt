@file:Suppress("UNUSED")

package com.nice.common.helper

import android.os.Looper

fun isInMainThread(): Boolean = Looper.getMainLooper() == Looper.myLooper()
fun currentThread(): Thread = Thread.currentThread()

fun Any?.toStringOrEmpty(): String = this?.toString().orEmpty()