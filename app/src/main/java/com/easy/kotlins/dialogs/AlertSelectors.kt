package com.easy.kotlins.dialogs

import android.content.Context
import android.content.DialogInterface
import androidx.fragment.app.Fragment

inline fun <D : DialogInterface> Fragment.selector(
        noinline factory: AlertBuilderFactory<D>,
        themeResId: Int = 0,
        title: CharSequence? = null,
        items: List<CharSequence>,
        noinline onClick: (DialogInterface, CharSequence, Int) -> Unit
) = requireActivity().selector(factory, themeResId, title, items, onClick)

fun <D : DialogInterface> Context.selector(
        factory: AlertBuilderFactory<D>,
        themeResId: Int = 0,
        title: CharSequence? = null,
        items: List<CharSequence>,
        onClick: (DialogInterface, CharSequence, Int) -> Unit
) {
    with(factory(this, themeResId)) {
        if (title != null) {
            this.title = title
        }
        items(items, onClick)
    }
}