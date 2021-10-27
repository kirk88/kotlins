@file:Suppress("UNUSED")

package com.nice.common.helper

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

private val ToastHandler: Handler by lazy { Handler(Looper.getMainLooper()) }

fun Context.toast(msg: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    val runnable = Runnable { Toast.makeText(this, msg, duration).show() }
    if (isInMainThread()) {
        runnable.run()
    } else {
        ToastHandler.post(runnable)
    }
}

fun Context.toast(@StringRes msgId: Int, duration: Int = Toast.LENGTH_SHORT) {
    val runnable = Runnable { Toast.makeText(this, msgId, duration).show() }
    if (isInMainThread()) {
        runnable.run()
    } else {
        ToastHandler.post(runnable)
    }
}

fun Fragment.toast(msg: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    context?.toast(msg, duration)
}

fun Fragment.toast(@StringRes msgId: Int, duration: Int = Toast.LENGTH_SHORT) {
    context?.toast(msgId, duration)
}

fun View.toast(msg: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    context?.toast(msg, duration)
}

fun View.toast(@StringRes msgId: Int, duration: Int = Toast.LENGTH_SHORT) {
    context.toast(msgId, duration)
}