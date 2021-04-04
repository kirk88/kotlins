@file:Suppress("unused")

package com.easy.kotlins.dialogs

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

fun Fragment.alertBuilder(
    themeId: Int = 0,
    title: CharSequence? = null,
    message: CharSequence? = null,
    init: (AlertBuilder<DialogInterface>.() -> Unit)? = null
): AlertBuilder<AlertDialog> = requireActivity().alertBuilder(themeId, title, message, init)

fun Context.alertBuilder(
    themeId: Int = 0,
    title: CharSequence? = null,
    message: CharSequence? = null,
    init: (AlertBuilder<DialogInterface>.() -> Unit)? = null
): AlertBuilder<AlertDialog> {
    return AndroidAlertBuilder(this, themeId).apply {
        if (title != null) {
            this.title = title
        }
        if (message != null) {
            this.message = message
        }
        if (init != null) init()
    }
}

fun Fragment.alertBuilder(
    themeId: Int = 0,
    titleId: Int = 0,
    messageId: Int = 0,
    init: (AlertBuilder<DialogInterface>.() -> Unit)? = null
): AlertBuilder<AlertDialog> = requireActivity().alertBuilder(themeId, titleId, messageId, init)

fun Context.alertBuilder(
    themeId: Int = 0,
    titleId: Int = 0,
    messageId: Int = 0,
    init: (AlertBuilder<DialogInterface>.() -> Unit)? = null
): AlertBuilder<AlertDialog> {
    return AndroidAlertBuilder(this, themeId).apply {
        if (titleId != 0) {
            this.titleResource = titleId
        }
        if (messageId != 0) {
            this.messageResource = messageId
        }
        if (init != null) init()
    }
}

fun Fragment.alert(
    themeId: Int = 0,
    title: CharSequence? = null,
    message: CharSequence? = null,
    init: (AlertBuilder<DialogInterface>.() -> Unit)? = null
): AlertDialog = requireActivity().alert(themeId, title, message, init)

fun Context.alert(
    themeId: Int = 0,
    title: CharSequence? = null,
    message: CharSequence? = null,
    init: (AlertBuilder<DialogInterface>.() -> Unit)? = null
): AlertDialog = alertBuilder(themeId, title, message, init).show()

fun Fragment.alert(
    themeId: Int = 0,
    titleId: Int = 0,
    messageId: Int = 0,
    init: (AlertBuilder<DialogInterface>.() -> Unit)? = null
): AlertDialog = requireActivity().alert(themeId, titleId, messageId, init)

fun Context.alert(
    themeId: Int = 0,
    titleId: Int = 0,
    messageId: Int = 0,
    init: (AlertBuilder<DialogInterface>.() -> Unit)? = null
): AlertDialog = alertBuilder(themeId, titleId, messageId, init).show()