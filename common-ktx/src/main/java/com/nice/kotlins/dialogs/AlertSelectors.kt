@file:Suppress("unused")

package com.nice.kotlins.dialogs

import android.content.Context
import android.content.DialogInterface
import androidx.fragment.app.Fragment

fun <D : DialogInterface> Fragment.selector(
        factory: AlertBuilderFactory<D>,
        themeResId: Int = 0,
        title: CharSequence? = null,
        items: List<CharSequence>,
        onClick: (DialogInterface, CharSequence, Int) -> Unit
): D = requireActivity().selector(factory, themeResId, title, items, onClick)

fun <D : DialogInterface> Context.selector(
        factory: AlertBuilderFactory<D>,
        themeResId: Int = 0,
        title: CharSequence? = null,
        items: List<CharSequence>,
        onClick: (DialogInterface, CharSequence, Int) -> Unit
): D = with(factory(this, themeResId)) {
    if (title != null) {
        this.title = title
    }
    items(items, onClick)
    show()
}

fun <D : DialogInterface> Fragment.selector(
        factory: AlertBuilderFactory<D>,
        themeResId: Int = 0,
        titleId: Int,
        items: List<CharSequence>,
        onClick: (DialogInterface, CharSequence, Int) -> Unit
): D = requireActivity().selector(factory, themeResId, titleId, items, onClick)

fun <D : DialogInterface> Context.selector(
        factory: AlertBuilderFactory<D>,
        themeResId: Int = 0,
        titleId: Int,
        items: List<CharSequence>,
        onClick: (DialogInterface, CharSequence, Int) -> Unit
): D = with(factory(this, themeResId)) {
    this.titleResource = titleId
    items(items, onClick)
    show()
}