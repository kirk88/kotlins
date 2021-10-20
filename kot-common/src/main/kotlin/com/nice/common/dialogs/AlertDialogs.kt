@file:Suppress("unused")

package com.nice.common.dialogs

import android.content.Context
import android.content.DialogInterface
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment

typealias AlertBuilderFactory<D> = (Context, Int) -> AlertBuilder<D>

fun <D : DialogInterface> Fragment.alertBuilder(
        factory: AlertBuilderFactory<D>,
        title: CharSequence? = null,
        message: CharSequence? = null,
        @StyleRes themeId: Int = 0,
        init: (AlertBuilder<D>.() -> Unit)? = null
): AlertBuilder<D> = requireActivity().alertBuilder(factory, title, message, themeId, init)

fun <D : DialogInterface> Context.alertBuilder(
        factory: AlertBuilderFactory<D>,
        title: CharSequence? = null,
        message: CharSequence? = null,
        @StyleRes themeId: Int = 0,
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
        @StringRes titleId: Int,
        @StringRes messageId: Int,
        @StyleRes themeId: Int = 0,
        init: (AlertBuilder<D>.() -> Unit)? = null
): AlertBuilder<D> = requireActivity().alertBuilder(factory, titleId, messageId, themeId, init)

fun <D : DialogInterface> Context.alertBuilder(
        factory: AlertBuilderFactory<D>,
        @StringRes titleId: Int,
        @StringRes messageId: Int,
        @StyleRes themeId: Int = 0,
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
        title: CharSequence? = null,
        message: CharSequence? = null,
        @StyleRes themeId: Int = 0,
        init: (AlertBuilder<D>.() -> Unit)? = null
): D = requireActivity().alert(factory, title, message, themeId, init)

fun <D : DialogInterface> Context.alert(
        factory: AlertBuilderFactory<D>,
        title: CharSequence? = null,
        message: CharSequence? = null,
        @StyleRes themeId: Int = 0,
        init: (AlertBuilder<D>.() -> Unit)? = null
): D = alertBuilder(factory, title, message, themeId, init).show()

fun <D : DialogInterface> Fragment.alert(
        factory: AlertBuilderFactory<D>,
        @StringRes titleId: Int,
        @StringRes messageId: Int,
        @StyleRes themeId: Int = 0,
        init: (AlertBuilder<D>.() -> Unit)? = null
): D = requireActivity().alert(factory, titleId, messageId, themeId, init)

fun <D : DialogInterface> Context.alert(
        factory: AlertBuilderFactory<D>,
        @StringRes titleId: Int,
        @StringRes messageId: Int,
        @StyleRes themeId: Int = 0,
        init: (AlertBuilder<D>.() -> Unit)? = null
): D = alertBuilder(factory, titleId, messageId, themeId, init).show()