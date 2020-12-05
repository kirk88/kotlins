package com.easy.kotlins.widget

import com.load.dynamiclayout.DynamicLayout

/**
 * Create by LiZhanPing on 2020/10/11
 */
interface ProgressView {

    fun showProgress(message: CharSequence? = null)

    fun showProgress(messageId: Int)

    fun dismissProgress()
}