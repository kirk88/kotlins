@file:Suppress("unused")

package com.nice.common.helper

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

fun Context.toast(msg: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, msg, duration).show()
}

fun Context.toast(@StringRes msgId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, msgId, duration).show()
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