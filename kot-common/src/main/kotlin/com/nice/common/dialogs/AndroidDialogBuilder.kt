@file:Suppress("OverridingDeprecatedMember")

package com.nice.common.dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.KeyEvent
import android.view.View
import android.view.Window
import androidx.annotation.StyleRes
import com.nice.common.external.NO_GETTER

internal class AndroidDialogBuilder(
        override val context: Context,
        @StyleRes themeResId: Int = 0
) : DialogBuilder<Dialog> {

    private val dialog = Dialog(context, themeResId)

    override val window: Window
        get() = dialog.window ?: error("Dialog is not attach to window")

    override var contentView: View
        get() = NO_GETTER
        set(value) {
            dialog.setContentView(value)
        }

    override var isCancelable: Boolean
        get() = NO_GETTER
        set(value) {
            dialog.setCancelable(value)
        }

    override var isCanceledOnTouchOutside: Boolean
        get() = NO_GETTER
        set(value) {
            dialog.setCanceledOnTouchOutside(value)
        }

    override fun onShow(handler: (dialog: DialogInterface) -> Unit) {
        dialog.setOnShowListener(handler)
    }

    override fun onCancel(handler: (dialog: DialogInterface) -> Unit) {
        dialog.setOnCancelListener(handler)
    }

    override fun onDismiss(handler: (dialog: DialogInterface) -> Unit) {
        dialog.setOnDismissListener(handler)
    }

    override fun onKey(handler: (dialog: DialogInterface, keyCode: Int, e: KeyEvent) -> Boolean) {
        dialog.setOnKeyListener(handler)
    }

    override fun show(): Dialog = dialog.apply { dialog.show() }


}