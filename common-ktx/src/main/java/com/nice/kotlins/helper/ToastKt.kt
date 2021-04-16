@file:Suppress("unused")

package com.nice.kotlins.helper

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

fun Context.toast(msg: CharSequence) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun Context.toast(@StringRes msgId: Int) {
    Toast.makeText(this, msgId, Toast.LENGTH_SHORT).show()
}

fun Context.longToast(msg: CharSequence) {
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}

fun Context.longToast(@StringRes msgId: Int) {
    Toast.makeText(this, msgId, Toast.LENGTH_LONG).show()
}

fun Fragment.toast(msg: CharSequence) {
    activity?.toast(msg)
}

fun Fragment.toast(@StringRes msgId: Int) {
    activity?.toast(msgId)
}

fun Fragment.longToast(msg: CharSequence) {
    activity?.longToast(msg)
}

fun Fragment.longToast(msgId: Int) {
    activity?.longToast(msgId)
}

fun View.toast(msg: CharSequence) {
    context?.toast(msg)
}

fun View.toast(@StringRes msgId: Int) {
    context.toast(msgId)
}

fun View.longToast(msg: CharSequence) {
    context.longToast(msg)
}

fun View.longToast(@StringRes msgId: Int) {
    context.longToast(msgId)
}