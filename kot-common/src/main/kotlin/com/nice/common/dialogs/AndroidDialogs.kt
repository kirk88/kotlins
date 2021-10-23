@file:Suppress("UNUSED")

package com.nice.common.dialogs

import android.app.Dialog
import android.content.Context
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment

fun Fragment.dialogBuilder(
        @StyleRes themeId: Int = 0,
        init: (DialogBuilder<Dialog>.() -> Unit)? = null
): DialogBuilder<Dialog> {
    return requireActivity().dialogBuilder(themeId, init)
}

fun Context.dialogBuilder(
        @StyleRes themeId: Int = 0,
        init: (DialogBuilder<Dialog>.() -> Unit)? = null
): DialogBuilder<Dialog> {
    return AndroidDialogBuilder(this, themeId).apply {
        if (init != null) init()
    }
}

fun Fragment.dialog(
        @StyleRes themeId: Int = 0,
        init: DialogBuilder<Dialog>.() -> Unit
): Dialog {
    return requireActivity().dialog(themeId, init)
}

fun Context.dialog(
        @StyleRes themeId: Int = 0,
        init: DialogBuilder<Dialog>.() -> Unit
): Dialog {
    return AndroidDialogBuilder(this, themeId).apply(init).show()
}
