@file:Suppress("unused")

package com.easy.kotlins.dialogs

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment

typealias AlertBuilderFactory<D> = (Context, Int) -> AlertBuilder<D>

fun <D : DialogInterface> Fragment.alertBuilder(
    factory: AlertBuilderFactory<D>,
    themeId: Int = 0,
    title: CharSequence? = null,
    message: CharSequence? = null,
    init: (AlertBuilder<D>.() -> Unit)? = null
): AlertBuilder<D> = requireActivity().alertBuilder(factory, themeId, title, message, init)

fun <D : DialogInterface> Context.alertBuilder(
    factory: AlertBuilderFactory<D>,
    themeId: Int = 0,
    title: CharSequence? = null,
    message: CharSequence? = null,
    init: (AlertBuilder<D>.() -> Unit)? = null
): AlertBuilder<D> {
    return factory(this, themeId).apply {
        if (title != null) {
            this.title = title
        }
        if (message != null) {
            this.message = message
        }
        if (init != null) init()
    }
}

fun <D : DialogInterface> Fragment.alertBuilder(
    factory: AlertBuilderFactory<D>,
    themeId: Int = 0,
    titleId: Int = 0,
    messageId: Int = 0,
    init: (AlertBuilder<D>.() -> Unit)? = null
): AlertBuilder<D> = requireActivity().alertBuilder(factory, themeId, titleId, messageId, init)

fun <D : DialogInterface> Context.alertBuilder(
    factory: AlertBuilderFactory<D>,
    themeId: Int = 0,
    titleId: Int = 0,
    messageId: Int = 0,
    init: (AlertBuilder<D>.() -> Unit)? = null
): AlertBuilder<D> {
    return factory(this, themeId).apply {
        if (titleId != 0) {
            this.titleResource = titleId
        }
        if (messageId != 0) {
            this.messageResource = messageId
        }
        if (init != null) init()
    }
}

fun <D : DialogInterface> Fragment.alert(
    factory: AlertBuilderFactory<D>,
    themeId: Int = 0,
    title: CharSequence? = null,
    message: CharSequence? = null,
    init: (AlertBuilder<D>.() -> Unit)? = null
): D = requireActivity().alert(factory, themeId, title, message, init)

fun <D : DialogInterface> Context.alert(
    factory: AlertBuilderFactory<D>,
    themeId: Int = 0,
    title: CharSequence? = null,
    message: CharSequence? = null,
    init: (AlertBuilder<D>.() -> Unit)? = null
): D = alertBuilder(factory, themeId, title, message, init).show()

fun <D : DialogInterface> Fragment.alert(
    factory: AlertBuilderFactory<D>,
    themeId: Int = 0,
    titleId: Int = 0,
    messageId: Int = 0,
    init: (AlertBuilder<D>.() -> Unit)? = null
): D = requireActivity().alert(factory, themeId, titleId, messageId, init)

fun <D : DialogInterface> Context.alert(
    factory: AlertBuilderFactory<D>,
    themeId: Int = 0,
    titleId: Int = 0,
    messageId: Int = 0,
    init: (AlertBuilder<D>.() -> Unit)? = null
): D = alertBuilder(factory, themeId, titleId, messageId, init).show()