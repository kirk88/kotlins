@file:Suppress("unused")

package com.nice.kotlins.dialogs

import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nice.kotlins.helper.Internals.NO_GETTER

val Android: AlertBuilderFactory<AlertDialog> = ::AndroidAlertBuilder

internal class AndroidAlertBuilder(override val context: Context, themeResId: Int = 0) :
    AlertBuilder<AlertDialog> {
    private val builder = MaterialAlertDialogBuilder(context, themeResId)

    override var title: CharSequence = NO_GETTER
        set(value) {
            builder.setTitle(value)
        }

    override var titleResource: Int = NO_GETTER
        set(value) {
            builder.setTitle(value)
        }

    override var message: CharSequence = NO_GETTER
        set(value) {
            builder.setMessage(value)
        }

    override var messageResource: Int = NO_GETTER
        set(value) {
            builder.setMessage(value)
        }

    override var icon: Drawable = NO_GETTER
        set(value) {
            builder.setIcon(value)
        }

    override var iconResource: Int = NO_GETTER
        set(value) {
            builder.setIcon(value)
        }

    override var customTitle: View = NO_GETTER
        set(value) {
            builder.setCustomTitle(value)
        }

    override var customView: View = NO_GETTER
        set(value) {
            builder.setView(value)
        }

    override var isCancelable: Boolean = NO_GETTER
        set(value) {
            builder.setCancelable(value)
        }

    override fun onCancelled(handler: (DialogInterface) -> Unit) {
        builder.setOnCancelListener(handler)
    }

    override fun onDismissed(handler: (dialog: DialogInterface) -> Unit) {
        builder.setOnDismissListener(handler)
    }

    override fun onKeyPressed(handler: (dialog: DialogInterface, keyCode: Int, e: KeyEvent) -> Boolean) {
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