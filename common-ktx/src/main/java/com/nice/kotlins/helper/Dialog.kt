package com.nice.kotlins.helper

import android.app.Dialog
import android.content.DialogInterface
import android.view.KeyEvent

inline fun Dialog.doOnShow(crossinline onShow: (dialog: DialogInterface) -> Unit) = setOnShowListener {
    onShow(it)
}

inline fun Dialog.doOnDismiss(crossinline onDismiss: (dialog: DialogInterface) -> Unit) = setOnDismissListener {
    onDismiss(it)
}

inline fun Dialog.doOnCancel(crossinline onCancel: (dialog: DialogInterface) -> Unit) = setOnCancelListener {
    onCancel(it)
}

inline fun Dialog.doOnKeyPressed(crossinline onKey: (dialog: DialogInterface, keyCode: Int, event: KeyEvent) -> Boolean) =
    setOnKeyListener { dialog, keyCode, event ->
        onKey(dialog, keyCode, event)
    }