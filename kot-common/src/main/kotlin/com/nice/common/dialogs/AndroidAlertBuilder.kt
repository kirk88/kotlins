@file:Suppress("UNUSED", "OverridingDeprecatedMember")

package com.nice.common.dialogs

import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nice.common.external.NO_GETTER

internal class AndroidAlertBuilder(override val context: Context, themeResId: Int = 0) :
    AlertBuilder<AlertDialog> {
    private val builder = MaterialAlertDialogBuilder(context, themeResId)

    override var title: CharSequence
        get() = NO_GETTER
        set(value) {
            builder.setTitle(value)
        }

    override var titleResource: Int
        get() = NO_GETTER
        set(value) {
            builder.setTitle(value)
        }

    override var message: CharSequence
        get() = NO_GETTER
        set(value) {
            builder.setMessage(value)
        }

    override var messageResource: Int
        get() = NO_GETTER
        set(value) {
            builder.setMessage(value)
        }

    override var icon: Drawable
        get() = NO_GETTER
        set(value) {
            builder.setIcon(value)
        }

    override var iconResource: Int
        get() = NO_GETTER
        set(value) {
            builder.setIcon(value)
        }

    override var customTitle: View
        get() = NO_GETTER
        set(value) {
            builder.setCustomTitle(value)
        }

    override var customView: View
        get() = NO_GETTER
        set(value) {
            builder.setView(value)
        }

    override var isCancelable: Boolean
        get() = NO_GETTER
        set(value) {
            builder.setCancelable(value)
        }

    override fun onCancel(handler: (DialogInterface) -> Unit) {
        builder.setOnCancelListener(handler)
    }

    override fun onDismiss(handler: (dialog: DialogInterface) -> Unit) {
        builder.setOnDismissListener(handler)
    }

    override fun onKey(handler: (dialog: DialogInterface, keyCode: Int, event: KeyEvent) -> Boolean) {
        builder.setOnKeyListener(handler)
    }

    override fun positiveButton(
        buttonText: String,
        onClicked: ((dialog: DialogInterface) -> Unit)?
    ) {
        builder.setPositiveButton(buttonText) { dialog, _ -> onClicked?.invoke(dialog) }
    }

    override fun positiveButton(
        buttonTextResource: Int,
        onClicked: ((dialog: DialogInterface) -> Unit)?
    ) {
        builder.setPositiveButton(buttonTextResource) { dialog, _ -> onClicked?.invoke(dialog) }
    }

    override fun negativeButton(
        buttonText: String,
        onClicked: ((dialog: DialogInterface) -> Unit)?
    ) {
        builder.setNegativeButton(buttonText) { dialog, _ -> onClicked?.invoke(dialog) }
    }

    override fun negativeButton(
        buttonTextResource: Int,
        onClicked: ((dialog: DialogInterface) -> Unit)?
    ) {
        builder.setNegativeButton(buttonTextResource) { dialog, _ -> onClicked?.invoke(dialog) }
    }

    override fun neutralButton(
        buttonText: String,
        onClicked: ((dialog: DialogInterface) -> Unit)?
    ) {
        builder.setNeutralButton(buttonText) { dialog, _ -> onClicked?.invoke(dialog) }
    }

    override fun neutralButton(
        buttonTextResource: Int,
        onClicked: ((dialog: DialogInterface) -> Unit)?
    ) {
        builder.setNeutralButton(buttonTextResource) { dialog, _ -> onClicked?.invoke(dialog) }
    }

    override fun items(
        items: List<CharSequence>,
        onItemSelected: (dialog: DialogInterface, index: Int) -> Unit
    ) {
        builder.setItems(Array(items.size) { i -> items[i].toString() }) { dialog, which ->
            onItemSelected(dialog, which)
        }
    }

    override fun <T> items(
        items: List<T>,
        onItemSelected: (dialog: DialogInterface, item: T, index: Int) -> Unit
    ) {
        builder.setItems(Array(items.size) { i -> items[i].toString() }) { dialog, which ->
            onItemSelected(dialog, items[which], which)
        }
    }

    override fun multiChoiceItems(
        items: Array<String>,
        checkedItems: BooleanArray,
        onClick: (dialog: DialogInterface, which: Int, isChecked: Boolean) -> Unit
    ) {
        builder.setMultiChoiceItems(items, checkedItems) { dialog, which, isChecked ->
            onClick(dialog, which, isChecked)
        }
    }

    override fun singleChoiceItems(
        items: Array<String>,
        checkedItem: Int,
        onClick: ((dialog: DialogInterface, which: Int) -> Unit)?
    ) {
        builder.setSingleChoiceItems(items, checkedItem) { dialog, which ->
            onClick?.invoke(dialog, which)
        }
    }

    override fun build(): AlertDialog = builder.create()

    override fun show(): AlertDialog = builder.show()
}