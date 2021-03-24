package com.easy.kotlins.widget

interface ProgressView {

    fun showProgress(message: CharSequence? = null)

    fun showProgress(messageId: Int)

    fun dismissProgress()
}