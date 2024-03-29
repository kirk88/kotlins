@file:Suppress("UNUSED")

package com.nice.common.dialogs

import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.view.KeyEvent
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import com.nice.common.external.NO_GETTER_MESSAGE
import kotlin.DeprecationLevel.ERROR

@Suppress("SupportAnnotationUsage")
interface AlertBuilder<out D : DialogInterface> {
    val context: Context

    var title: CharSequence
        @Deprecated(NO_GETTER_MESSAGE, level = ERROR) get

    @setparam:StringRes
    var titleResource: Int
        @Deprecated(NO_GETTER_MESSAGE, level = ERROR) get

    var message: CharSequence
        @Deprecated(NO_GETTER_MESSAGE, level = ERROR) get

    @setparam:StringRes
    var messageResource: Int
        @Deprecated(NO_GETTER_MESSAGE, level = ERROR) get

    var icon: Drawable
        @Deprecated(NO_GETTER_MESSAGE, level = ERROR) get

    @setparam:DrawableRes
    var iconResource: Int
        @Deprecated(NO_GETTER_MESSAGE, level = ERROR) get

    var customTitle: View
        @Deprecated(NO_GETTER_MESSAGE, level = ERROR) get

    var customView: View
        @Deprecated(NO_GETTER_MESSAGE, level = ERROR) get

    var isCancelable: Boolean
        @Deprecated(NO_GETTER_MESSAGE, level = ERROR) get

    fun positiveButton(buttonText: String, onClicked: ((dialog: DialogInterface) -> Unit)? = null)
    fun positiveButton(
            @StringRes buttonTextResource: Int,
            onClicked: ((dialog: DialogInterface) -> Unit)? = null
    )

    fun negativeButton(buttonText: String, onClicked: ((dialog: DialogInterface) -> Unit)? = null)
    fun negativeButton(
            @StringRes buttonTextResource: Int,
            onClicked: ((dialog: DialogInterface) -> Unit)? = null
    )

    fun neutralButton(buttonText: String, onClicked: ((dialog: DialogInterface) -> Unit)? = null)
    fun neutralButton(
            @StringRes buttonTextResource: Int,
            onClicked: ((dialog: DialogInterface) -> Unit)? = null
    )

    fun onCancel(handler: (dialog: DialogInterface) -> Unit)

    fun onDismiss(handler: (dialog: DialogInterface) -> Unit)

    fun onKey(handler: (dialog: DialogInterface, keyCode: Int, event: KeyEvent) -> Boolean)

    fun items(
            items: List<CharSequence>,
            onItemSelected: (dialog: DialogInterface, index: Int) -> Unit
    )

    fun <T> items(
            items: List<T>,
            onItemSelected: (dialog: DialogInterface, item: T, index: Int) -> Unit
    )

    fun multiChoiceItems(
            items: Array<String>,
            checkedItems: BooleanArray,
            onClick: (dialog: DialogInterface, which: Int, isChecked: Boolean) -> Unit
    )

    fun singleChoiceItems(
            items: Array<String>,
            checkedItem: Int = 0,
            onClick: ((dialog: DialogInterface, which: Int) -> Unit)? = null
    )

    fun build(): D
    fun show(): D
}

fun AlertBuilder<*>.customTitle(
        @LayoutRes layoutResId: Int,
        action: View.() -> Unit = {}
) {
    customTitle = View.inflate(context, layoutResId, null).apply(action)
}

fun AlertBuilder<*>.customView(
        @LayoutRes layoutResId: Int,
        action: View.() -> Unit = {}
) {
    customView = View.inflate(context, layoutResId, null).apply(action)
}

fun AlertBuilder<*>.okButton(handler: ((dialog: DialogInterface) -> Unit)? = null) = positiveButton(android.R.string.ok, handler)

fun AlertBuilder<*>.cancelButton(handler: ((dialog: DialogInterface) -> Unit)? = null) = negativeButton(android.R.string.cancel, handler)