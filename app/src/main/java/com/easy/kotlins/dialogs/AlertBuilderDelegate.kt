package com.easy.kotlins.dialogs

import android.content.Context
import android.content.DialogInterface
import androidx.annotation.StringRes
import kotlin.DeprecationLevel.ERROR

/**
 * Create by LiZhanPing on 2020/9/16
 */
interface AlertBuilderDelegate<D: DialogInterface> {

    val context: Context

    var title: CharSequence?

    var titleResource: Int
        @Deprecated("NO_GETTER", level = ERROR) get

    var message: CharSequence?

    var messageResource: Int
        @Deprecated("NO_GETTER", level = ERROR) get

    fun positiveButton(buttonText: String, onClicked: ((dialog: DialogInterface) -> Unit)? = null)
    fun positiveButton(@StringRes buttonTextResource: Int, onClicked: ((dialog: DialogInterface) -> Unit)? = null)

    fun negativeButton(buttonText: String, onClicked: ((dialog: DialogInterface) -> Unit)? = null)
    fun negativeButton(@StringRes buttonTextResource: Int, onClicked: ((dialog: DialogInterface) -> Unit)? = null)

    fun build(): D
    fun show(): D
}

inline fun AlertBuilderDelegate<*>.okButton(noinline handler: ((dialog: DialogInterface) -> Unit)? = null) =
        positiveButton(android.R.string.ok, handler)

inline fun AlertBuilderDelegate<*>.cancelButton(noinline handler: ((dialog: DialogInterface) -> Unit)? = null) =
        negativeButton(android.R.string.cancel, handler)

inline fun AlertBuilderDelegate<*>.yesButton(noinline handler: ((dialog: DialogInterface) -> Unit)? = null) =
        positiveButton(android.R.string.yes, handler)

inline fun AlertBuilderDelegate<*>.noButton(noinline handler: ((dialog: DialogInterface) -> Unit)? = null) =
        negativeButton(android.R.string.no, handler)