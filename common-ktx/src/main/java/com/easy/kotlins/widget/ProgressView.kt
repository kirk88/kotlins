package com.easy.kotlins.widget

import android.app.Activity
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Create by LiZhanPing on 2020/10/11
 */
interface ProgressView {

    fun showProgress(message: CharSequence? = null)

    fun showProgress(messageId: Int)

    fun dismissProgress()
}