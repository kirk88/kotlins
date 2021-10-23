@file:Suppress("UNUSED")

package com.nice.common.dialogs

import android.content.Context
import android.content.DialogInterface
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

fun Fragment.selector(
        title: CharSequence,
        items: List<CharSequence>,
        @StyleRes themeId: Int = 0,
        onClick: (DialogInterface, Int) -> Unit
): AlertDialog = requireActivity().selector(title, items, themeId, onClick)

fun Context.selector(
        title: CharSequence,
        items: List<CharSequence>,
        @StyleRes themeId: Int = 0,
        onClick: (DialogInterface, Int) -> Unit
): AlertDialog = with(AndroidAlertBuilder(this, themeId)) {
    this.title = title
    items(items, onClick)
    show()
}

fun Fragment.selector(
        @StringRes titleId: Int,
        items: List<CharSequence>,
        @StyleRes themeId: Int = 0,
        onClick: (DialogInterface, Int) -> Unit
): AlertDialog = requireActivity().selector(titleId, items, themeId, onClick)

fun Context.selector(
        @StringRes titleId: Int,
        items: List<CharSequence>,
        @StyleRes themeId: Int = 0,
        onClick: (DialogInterface, Int) -> Unit
): AlertDialog = with(AndroidAlertBuilder(this, themeId)) {
    this.titleResource = titleId
    items(items, onClick)
    show()
}