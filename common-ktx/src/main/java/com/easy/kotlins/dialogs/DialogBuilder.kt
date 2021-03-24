@file:Suppress("unused")

package com.easy.kotlins.dialogs

import android.content.Context
import android.content.DialogInterface
import android.view.KeyEvent
import android.view.View
import android.view.Window
import androidx.annotation.LayoutRes

interface DialogBuilder<out D : DialogInterface> {

    val context: Context

    val window: Window

    var contentView: View
        @Deprecated("NO_GETTER", level = DeprecationLevel.ERROR) get

    var isCancelable: Boolean
        @Deprecated("NO_GETTER", level = DeprecationLevel.ERROR) get

    var isCanceledOnTouchOutside: Boolean
        @Deprecated("NO_GETTER", level = DeprecationLevel.ERROR) get

    fun onCancelled(handler: (dialog: DialogInterface) -> Unit)

    fun onDismissed(handler: (dialog: DialogInterface) -> Unit)

    fun onShowed(handler: (dialog: DialogInterface) -> Unit)

    fun onKeyPressed(handler: (dialog: DialogInterface, keyCode: Int, e: KeyEvent) -> Boolean)

    fun show(): D

}

fun DialogBuilder<*>.contentView(view: () -> View) {
    contentView = view()
}

fun DialogBuilder<*>.contentView(
    @LayoutRes layoutResId: Int,
    action: View.() -> Unit = {}
) {
    contentView = View.inflate(context, layoutResId, null).apply(action)
}