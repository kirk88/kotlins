package com.easy.kotlins.dialogs

import android.content.Context
import android.content.DialogInterface
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.easy.kotlins.R
import com.easy.kotlins.helper.*
import kotlinx.android.synthetic.main.dialog_alert_custom.view.*

/**
 * Create by LiZhanPing on 2020/9/16
 */

class CustomAlertBuilderDelegate(dialogBuilder: AlertBuilder<AlertDialog>) : AlertBuilderDelegate<AlertDialog> {

    override val context: Context = dialogBuilder.context

    private val view: View = View.inflate(context, R.layout.dialog_alert_custom, null)
    private val builder: AlertBuilder<AlertDialog> = dialogBuilder.apply {
        customView = view
    }
    private val dialog: AlertDialog by lazy { builder.build() }

    override var title: CharSequence?
        get() = view.dialog_title.text
        set(value) {
            if (value.isNullOrEmpty()) {
                view.dialog_title.isGone = true
            } else {
                view.dialog_title.isGone = false
                view.dialog_title.text = value
            }
        }

    override var titleResource: Int
        get() = error("no getter")
        set(value) {
            view.dialog_title.setText(value)
        }

    override var message: CharSequence?
        get() = view.dialog_message.text
        set(value) {
            if (value.isNullOrEmpty()) {
                view.dialog_message.isGone = true
            } else {
                view.dialog_message.isGone = false
                view.dialog_message.text = value
            }
        }

    override var messageResource: Int
        get() = error("no getter")
        set(value) {
            view.dialog_message.setText(value)
        }

    override fun positiveButton(buttonText: String, onClicked: ((dialog: DialogInterface) -> Unit)?) {
        view.dialog_buttons_divider.isVisible = true
        view.dialog_positive_button.apply {
            isVisible = true

            string = buttonText

            onClick {
                dialog.dismiss()
                onClicked?.invoke(dialog)
            }
        }
    }

    override fun positiveButton(buttonTextResource: Int, onClicked: ((dialog: DialogInterface) -> Unit)?) {
        view.dialog_buttons_divider.isVisible = true
        view.dialog_positive_button.apply {
            isVisible = true

            textResource = buttonTextResource

            onClick {
                dialog.dismiss()
                onClicked?.invoke(dialog)
            }
        }
    }

    override fun negativeButton(buttonText: String, onClicked: ((dialog: DialogInterface) -> Unit)?) {
        view.dialog_buttons_divider.isVisible = true
        view.dialog_negative_button.apply {
            isVisible = true

            string = buttonText

            onClick {
                dialog.dismiss()
                onClicked?.invoke(dialog)
            }
        }
    }

    override fun negativeButton(buttonTextResource: Int, onClicked: ((dialog: DialogInterface) -> Unit)?) {
        view.dialog_buttons_divider.isVisible = true
        view.dialog_negative_button.apply {
            isVisible = true

            textResource = buttonTextResource

            onClick {
                dialog.dismiss()
                onClicked?.invoke(dialog)
            }
        }
    }

    override fun build(): AlertDialog = dialog
    override fun show(): AlertDialog = dialog.apply { dialog.show() }

}