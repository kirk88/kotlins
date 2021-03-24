@file:Suppress("unused")

package com.easy.kotlins.dialogs

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

fun Fragment.alertBuilder(
    themeResId: Int = 0,
    title: CharSequence? = null,
    message: CharSequence? = null,
    init: (AlertBuilder<DialogInterface>.() -> Unit)? = null
): AlertBuilder<AlertDialog> = requireActivity().alertBuilder(themeResId, title, message, init)

fun Context.alertBuilder(
    themeResId: Int = 0,
    title: CharSequence? = null,
    message: CharSequence? = null,
    init: (AlertBuilder<DialogInterface>.() -> Unit)? = null
): AlertBuilder<AlertDialog> {
    return AndroidAlertBuilder(this, themeResId).apply {
        if (title != null) {
            this.title = title
        }
        if (message != null) {
            this.message = message
        }
        if (init != null) init()
    }
}

fun Fragment.alert(
    themeResId: Int = 0,
    title: CharSequence? = null,
    message: CharSequence? = null,
    init: (AlertBuilder<DialogInterface>.() -> Unit)? = null
): AlertDialog = requireActivity().alert(themeResId, title, message, init)

fun Context.alert(
    themeResId: Int = 0,
    title: CharSequence? = null,
    message: CharSequence? = null,
    init: (AlertBuilder<DialogInterface>.() -> Unit)? = null
): AlertDialog {
    return AndroidAlertBuilder(this, themeResId).apply {
        if (title != null) {
            this.title = title
        }
        if (message != null) {
            this.message = message
        }
        if (init != null) init()
    }.show()
}