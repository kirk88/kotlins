package com.nice.kotlins.widget

interface ProgressView {

    fun show(message: CharSequence? = null)

    fun show(messageId: Int)

    fun dismiss()
}