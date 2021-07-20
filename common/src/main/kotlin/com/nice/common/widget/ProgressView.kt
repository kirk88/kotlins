package com.nice.common.widget

interface ProgressView {

    fun show(message: CharSequence? = null)

    fun show(messageId: Int)

    fun dismiss()
}