package com.easy.kotlins.dialogs

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

/**
 * Create by LiZhanPing on 2020/9/26
 */

inline fun Fragment.customAlertBuilder(
        themeResId: Int = 0,
        title: CharSequence? = null,
        message: CharSequence? = null,
        noinline init: (AlertBuilderDelegate<AlertDialog>.() -> Unit)? = null
): AlertBuilderDelegate<AlertDialog> = requireActivity().customAlertBuilder(themeResId, title, message, init)

fun Context.customAlertBuilder(
        themeResId: Int = 0,
        title: CharSequence? = null,
        message: CharSequence? = null,
        init: (AlertBuilderDelegate<AlertDialog>.() -> Unit)? = null
): AlertBuilderDelegate<AlertDialog> {
    return CustomAlertBuilderDelegate(alertBuilder(themeResId)).apply {
        if (title != null) {
            this.title = title
        }
        if (message != null) {
            this.message = message
        }
        if (init != null) init()
    }
}


inline fun Fragment.customAlert(
        themeResId: Int = 0,
        title: CharSequence? = null,
        message: CharSequence? = null,
        noinline init: (AlertBuilderDelegate<AlertDialog>.() -> Unit)? = null
): AlertDialog = requireActivity().customAlert(themeResId, title, message, init)

fun Context.customAlert(
        themeResId: Int = 0,
        title: CharSequence? = null,
        message: CharSequence? = null,
        init: (AlertBuilderDelegate<AlertDialog>.() -> Unit)? = null
): AlertDialog {
    return CustomAlertBuilderDelegate(alertBuilder(themeResId)).apply {
        if (title != null) {
            this.title = title
        }
        if (message != null) {
            this.message = message
        }
        if (init != null) init()
    }.show()
}
