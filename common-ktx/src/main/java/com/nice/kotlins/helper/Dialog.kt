package com.nice.kotlins.helper

import android.app.Dialog
import android.content.DialogInterface
import android.view.KeyEvent

inline fun Dialog.doOnShow(crossinline action: (dialog: DialogInterface) -> Unit) = setOnShowListener {
    action(it)
}

inline fun Dialog.doOnDismiss(crossinline action: (dialog: DialogInterface) -> Unit) = setOnDismissListener {
    action(it)
}

inline fun Dialog.doOnCancel(crossinline action: (dialog: DialogInterface) -> Unit) = setOnCancelListener {
    action(it)
}

inline fun Dialog.doOnKeyPressed(crossinline action: (dialog: DialogInterface, keyCode: Int, event: KeyEvent) -> Boolean) =
    setOnKeyListener { dialog, keyCode, event ->
        action(dialog, keyCode, event)
    }