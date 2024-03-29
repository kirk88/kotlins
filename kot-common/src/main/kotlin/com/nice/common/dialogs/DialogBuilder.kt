@file:Suppress("UNUSED")

package com.nice.common.dialogs

import android.content.Context
import android.content.DialogInterface
import android.view.KeyEvent
import android.view.View
import android.view.Window
import androidx.annotation.LayoutRes
import com.nice.common.external.NO_GETTER_MESSAGE

interface DialogBuilder<out D : DialogInterface> {

    val context: Context

    val window: Window

    var contentView: View
        @Deprecated(NO_GETTER_MESSAGE, level = DeprecationLevel.ERROR) get

    var isCancelable: Boolean
        @Deprecated(NO_GETTER_MESSAGE, level = DeprecationLevel.ERROR) get

    var isCanceledOnTouchOutside: Boolean
        @Deprecated(NO_GETTER_MESSAGE, level = DeprecationLevel.ERROR) get

    fun onShow(handler: (dialog: DialogInterface) -> Unit)

    fun onCancel(handler: (dialog: DialogInterface) -> Unit)

    fun onDismiss(handler: (dialog: DialogInterface) -> Unit)

    fun onKey(handler: (dialog: DialogInterface, keyCode: Int, event: KeyEvent) -> Boolean)

    fun show(): D

}

fun DialogBuilder<*>.contentView(
        @LayoutRes layoutResId: Int,
        action: View.() -> Unit = {}
) {
    contentView = View.inflate(context, layoutResId, null).apply(action)
}