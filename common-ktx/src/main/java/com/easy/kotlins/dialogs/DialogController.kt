package com.easy.kotlins.dialogs

import android.content.Context
import android.content.DialogInterface
import android.view.KeyEvent
import android.view.View
import android.view.Window
import androidx.annotation.LayoutRes

/**
 * Create by LiZhanPing on 2020/9/26
 */
interface DialogController<out D : DialogInterface> {

    val context: Context

    val dialog: DialogInterface

    val window: Window

    var contentView: View
        @Deprecated("NO_GETTER", level = DeprecationLevel.ERROR) get

    var contentResource: Int
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

fun DialogController<*>.contentView(view: () -> View) {
    contentView = view()
}

fun DialogController<*>.contentView(@LayoutRes layoutResId: Int, action: ((view: View) -> Unit)? = null) {
    contentView = View.inflate(context, layoutResId, null).apply{ action?.invoke(this) }
}

fun <D: DialogInterface> DialogController<D>.show(onShow: D.() -> Unit): D{
    return show().apply(onShow)
}