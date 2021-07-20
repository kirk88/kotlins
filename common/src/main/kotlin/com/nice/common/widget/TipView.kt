package com.nice.common.widget

import androidx.annotation.StringRes

interface TipView {

    fun show(message: CharSequence)

    fun show(@StringRes messageId: Int)

    fun dismiss()

}

