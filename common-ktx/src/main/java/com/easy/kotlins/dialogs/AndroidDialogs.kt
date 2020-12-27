package com.easy.kotlins.dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment

/**
 * Create by LiZhanPing on 2020/9/26
 */

inline fun Fragment.buildDialog(themeResId: Int = 0, noinline init: DialogController<Dialog>.() -> Unit): Dialog {
    return requireActivity().buildDialog(themeResId, init)
}

fun Context.buildDialog(themeResId: Int = 0, init: DialogController<Dialog>.() -> Unit): Dialog {
    return AndroidDialogController(this, themeResId).apply(init).dialog
}

inline fun Fragment.dialog(@StyleRes themeResId: Int = 0, noinline init: DialogController<Dialog>.() -> Unit): Dialog {
    return requireActivity().dialog(themeResId, init)
}

fun Context.dialog(@StyleRes themeResId: Int = 0, init: DialogController<Dialog>.() -> Unit): Dialog {
    return AndroidDialogController(this, themeResId).apply {
        init()
    }.show()
}

fun Fragment.customDialog(@StyleRes themeResId: Int = 0, @LayoutRes layoutResId: Int, init: (view: View, dialog: DialogInterface) -> Unit): Dialog {
    return requireActivity().customDialog(themeResId, layoutResId, init)
}

fun Context.customDialog(@StyleRes themeResId: Int = 0, @LayoutRes layoutResId: Int, init: (view: View, dialog: DialogInterface) -> Unit): Dialog {
    return AndroidDialogController(this, themeResId).apply {
        contentView {
            val view = View.inflate(context, layoutResId, null)

            init(view, dialog)

            view
        }
    }.show()
}

fun Fragment.customDialog(@StyleRes themeResId: Int = 0, init: (dialog: DialogInterface) -> View): Dialog {
    return requireActivity().customDialog(themeResId, init)
}

fun Context.customDialog(@StyleRes themeResId: Int = 0, init: (dialog: DialogInterface) -> View): Dialog {
    return AndroidDialogController(this, themeResId).apply {
        contentView = init(dialog)
    }.show()
}