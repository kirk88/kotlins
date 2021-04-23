package com.nice.kotlins.widget

import androidx.annotation.StringRes

interface TipsView {

    fun show(message: CharSequence)

    fun show(@StringRes messageId: Int)

    fun dismiss()

}

