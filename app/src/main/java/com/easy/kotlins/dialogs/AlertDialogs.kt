package com.easy.kotlins.dialogs

import android.content.Context
import android.content.DialogInterface
import androidx.fragment.app.Fragment

typealias AlertBuilderFactory<D> = (Context, Int) -> AlertBuilder<D>

inline fun <D : DialogInterface> Fragment.alertBuilder(
        noinline factory: AlertBuilderFactory<D>,
        themeResId: Int = 0,
        title: CharSequence? = null,
        message: CharSequence? = null,
        noinline init: (AlertBuilder<D>.() -> Unit)? = null
): AlertBuilder<D> = requireActivity().alertBuilder(factory, themeResId, title, message, init)

fun <D : DialogInterface> Context.alertBuilder(
        factory: AlertBuilderFactory<D>,
        themeResId: Int = 0,
        title: CharSequence? = null,
        message: CharSequence? = null,
        init: (AlertBuilder<D>.() -> Unit)? = null
): AlertBuilder<D> {
    return factory(this, themeResId).apply {
        if (title != null) {
            this.title = title
        }
        if (message != null) {
            this.message = message
        }
        if (init != null) init()
    }
}

inline fun <D : DialogInterface> Fragment.alert(
        noinline factory: AlertBuilderFactory<D>,
        themeResId: Int = 0,
        title: CharSequence? = null,
        message: CharSequence? = null,
        noinline init: (AlertBuilder<D>.() -> Unit)? = null
): D = requireActivity().alert(factory, themeResId, title, message, init)

fun <D : DialogInterface> Context.alert(
        factory: AlertBuilderFactory<D>,
        themeResId: Int = 0,
        title: CharSequence? = null,
        message: CharSequence? = null,
        init: (AlertBuilder<D>.() -> Unit)? = null
): D {
    return factory(this, themeResId).apply {
        if (title != null) {
            this.title = title
        }
        if (message != null) {
            this.message = message
        }
        if (init != null) init()
    }.show()
}