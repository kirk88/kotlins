@file:Suppress("unused")

package com.nice.kotlins.dialogs

import android.content.Context
import android.content.DialogInterface
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

fun Fragment.alertBuilder(
    title: CharSequence? = null,
    message: CharSequence? = null,
    @StyleRes themeId: Int = 0,
    init: (AlertBuilder<DialogInterface>.() -> Unit)? = null
): AlertBuilder<AlertDialog> = requireActivity().alertBuilder(title, message, themeId, init)

fun Context.alertBuilder(
    title: CharSequence? = null,
    message: CharSequence? = null,
    @StyleRes themeId: Int = 0,
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
    @StringRes titleId: Int,
    @StringRes messageId: Int,
    @StyleRes themeId: Int = 0,
    init: (AlertBuilder<DialogInterface>.() -> Unit)? = null
): AlertBuilder<AlertDialog> = requireActivity().alertBuilder(titleId, messageId, themeId, init)

fun Context.alertBuilder(
    @StringRes titleId: Int,
    @StringRes messageId: Int,
    @StyleRes themeId: Int = 0,
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
    title: CharSequence? = null,
    message: CharSequence? = null,
    @StyleRes themeId: Int = 0,
    init: (AlertBuilder<DialogInterface>.() -> Unit)? = null
): AlertDialog = requireActivity().alert(title, message, themeId, init)

fun Context.alert(
    title: CharSequence? = null,
    message: CharSequence? = null,
    @StyleRes themeId: Int = 0,
    init: (AlertBuilder<DialogInterface>.() -> Unit)? = null
): AlertDialog = alertBuilder(title, message, themeId, init).show()

fun Fragment.alert(
    @StringRes titleId: Int,
    @StringRes messageId: Int,
    @StyleRes themeId: Int = 0,
    init: (AlertBuilder<DialogInterface>.() -> Unit)? = null
): AlertDialog = requireActivity().alert(titleId, messageId, themeId, init)

fun Context.alert(
    @StringRes titleId: Int,
    @StringRes messageId: Int,
    @StyleRes themeId: Int = 0,
    init: (AlertBuilder<DialogInterface>.() -> Unit)? = null
): AlertDialog = alertBuilder(titleId, messageId, themeId, init).show()