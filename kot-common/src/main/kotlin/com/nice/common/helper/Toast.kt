@file:Suppress("UNUSED")

package com.nice.common.helper

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

fun Context.toast(msg: CharSequence, duration: Int = Toast.LENGTH_SHORT): Toast {
    return Toast.makeText(this, msg, duration)
}

fun Context.toast(@StringRes msgId: Int, duration: Int = Toast.LENGTH_SHORT): Toast {
    return Toast.makeText(this, msgId, duration)
}

fun Fragment.toast(msg: CharSequence, duration: Int = Toast.LENGTH_SHORT): Toast {
    return requireContext().toast(msg, duration)
}

fun Fragment.toast(@StringRes msgId: Int, duration: Int = Toast.LENGTH_SHORT): Toast {
    return requireContext().toast(msgId, duration)
}

fun View.toast(msg: CharSequence, duration: Int = Toast.LENGTH_SHORT): Toast {
    return context.toast(msg, duration)
}

fun View.toast(@StringRes msgId: Int, duration: Int = Toast.LENGTH_SHORT): Toast {
    return context.toast(msgId, duration)
}

private val ToastHandler: Handler by lazy { Handler(Looper.getMainLooper()) }

fun Context.showToast(msg: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    val runnable = Runnable { toast(msg, duration).show() }
    if (isInMainThread()) {
        runnable.run()
    } else {
        ToastHandler.post(runnable)
    }
}

fun Context.showToast(@StringRes msgId: Int, duration: Int = Toast.LENGTH_SHORT) {
    val runnable = Runnable { toast(msgId, duration).show() }
    if (isInMainThread()) {
        runnable.run()
    } else {
        ToastHandler.post(runnable)
    }
}

fun Fragment.showToast(msg: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    context?.showToast(msg, duration)
}

fun Fragment.showToast(@StringRes msgId: Int, duration: Int = Toast.LENGTH_SHORT) {
    context?.showToast(msgId, duration)
}

fun View.showToast(msg: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    context.showToast(msg, duration)
}

fun View.showToast(@StringRes msgId: Int, duration: Int = Toast.LENGTH_SHORT) {
    context.showToast(msgId, duration)
}