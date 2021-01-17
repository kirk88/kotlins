package com.easy.kotlins.dialogs

import android.content.Context
import android.content.DialogInterface
import androidx.fragment.app.Fragment

fun Fragment.selector(
        themeResId: Int = 0,
        title: CharSequence? = null,
        items: List<CharSequence>,
        onClick: (DialogInterface, Int) -> Unit
) = activity?.selector(themeResId, title, items, onClick)

fun Context.selector(
        themeResId: Int = 0,
        title: CharSequence? = null,
        items: List<CharSequence>,
        onClick: (DialogInterface, Int) -> Unit
) {
    with(AndroidAlertBuilder(this, themeResId)) {
        if (title != null) {
            this.title = title
        }
        items(items, onClick)
        show()
    }
}