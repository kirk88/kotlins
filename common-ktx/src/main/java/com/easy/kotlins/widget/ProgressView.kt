package com.easy.kotlins.widget

import android.app.ProgressDialog
import androidx.lifecycle.*
import kotlin.reflect.KClass

/**
 * Create by LiZhanPing on 2020/10/11
 */
interface ProgressView {

    fun showProgress(message: CharSequence? = null)

    fun showProgress(messageId: Int)

    fun dismissProgress()
}