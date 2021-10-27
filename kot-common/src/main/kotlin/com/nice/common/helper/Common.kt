@file:Suppress("UNUSED")

package com.nice.common.helper

import android.os.Looper

fun isInMainThread(): Boolean = Looper.getMainLooper() == Looper.myLooper()

fun Any?.toStringOrEmpty(): String = this?.toString().orEmpty()