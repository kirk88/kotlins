@file:Suppress("unused")

package com.easy.kotlins.dialogs

import android.app.Dialog
import android.content.Context
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment

fun Fragment.dialogBuilder(
    themeResId: Int = 0,
    init: (DialogBuilder<Dialog>.() -> Unit)? = null
): DialogBuilder<Dialog> {
    return requireActivity().dialogBuilder(themeResId, init)
}

fun Context.dialogBuilder(
    themeResId: Int = 0,
    init: (DialogBuilder<Dialog>.() -> Unit)? = null
): DialogBuilder<Dialog> {
    return AndroidDialogBuilder(this, themeResId).apply {
        if (init != null) init()
    }
}

fun Fragment.dialog(
    @StyleRes themeResId: Int = 0,
    init: DialogBuilder<Dialog>.() -> Unit
): Dialog {
    return requireActivity().dialog(themeResId, init)
}

fun Context.dialog(
    @StyleRes themeResId: Int = 0,
    init: DialogBuilder<Dialog>.() -> Unit
): Dialog {
    return AndroidDialogBuilder(this, themeResId).apply {
        init()
    }.show()
}
