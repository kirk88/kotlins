@file:Suppress("unused")

package com.nice.kotlins.helper

import android.app.Application
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment

fun Fragment.finishActivity() = activity?.finish()

inline fun <T : Fragment> T.withBundle(crossinline block: Bundle.() -> Unit): T = apply {
    val args = arguments ?: Bundle().also { arguments = it }
    args.apply(block)
}

fun <T : Fragment> T.withBundle(vararg args: Pair<String, Any?>): T = apply {
    val bundle = arguments ?: Bundle().also { arguments = it }
    bundle.putAll(*args)
}

val Fragment.application: Application
    get() {
        var application: Application? = null
        var appContext = requireContext().applicationContext
        while (appContext is ContextWrapper) {
            if (appContext is Application) {
                application = appContext
                break
            }
            appContext = appContext.baseContext
        }

        return application ?: requireActivity().application
    }

fun Fragment.setActivityResult(resultCode: Int){
    requireActivity().setResult(resultCode)
}

fun Fragment.setActivityResult(resultCode: Int, data: Intent){
    requireActivity().setResult(resultCode, data)
}