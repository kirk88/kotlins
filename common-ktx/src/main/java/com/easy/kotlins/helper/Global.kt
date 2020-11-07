package com.easy.kotlins.helper

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.io.Serializable

/**
 * Create by LiZhanPing on 2020/8/22
 * desc: 通用
 */
fun Context.toast(msg: CharSequence) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun Context.toast(@StringRes msgId: Int) {
    Toast.makeText(this, msgId, Toast.LENGTH_SHORT).show()
}

fun Context.longToast(msg: CharSequence) {
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}

fun Context.longToast(@StringRes msgId: Int) {
    Toast.makeText(this, msgId, Toast.LENGTH_LONG).show()
}

fun Fragment.toast(msg: CharSequence) {
    activity?.toast(msg)
}

fun Fragment.toast(@StringRes msgId: Int) {
    activity?.toast(msgId)
}

fun Fragment.longToast(msg: CharSequence) {
    activity?.longToast(msg)
}

fun Fragment.longToast(msgId: Int) {
    activity?.longToast(msgId)
}

fun View.toast(msg: CharSequence) {
    context?.toast(msg)
}

fun View.toast(@StringRes msgId: Int) {
    context.toast(msgId)
}

fun View.longToast(msg: CharSequence) {
    context.longToast(msg)
}

fun View.longToast(@StringRes msgId: Int) {
    context.longToast(msgId)
}

val Fragment.isActive: Boolean
    get() = isVisible || userVisibleHint

fun Fragment.finishActivity() = activity?.finish()

inline fun <T : Fragment> T.withBundle(crossinline action: Bundle.() -> Unit): T = apply {
    val args = arguments ?: Bundle().also { arguments = it }
    args.apply(action)
}

fun <T : Fragment> T.withBundle(vararg pairs: Pair<String, Any?>): T = apply {
    val args = arguments ?: Bundle().also { arguments = it }
    args.putAll(pairs.toBundle())
}

inline fun <reified A : Activity> Fragment.startActivity(noinline action: (Intent.() -> Unit)? = null) {
    startActivity(Intent(context, A::class.java).apply { action?.invoke(this) })
}

inline fun <reified A : Activity> Fragment.startActivity(vararg pairs: Pair<String, Any?>) {
    startActivity(Intent(context, A::class.java).apply { putExtras(pairs.toBundle()) })
}

inline fun <reified A : Activity> Context.startActivity(noinline action: (Intent.() -> Unit)? = null) {
    startActivity(Intent(this, A::class.java).apply { action?.invoke(this) })
}

inline fun <reified A : Activity> Context.startActivity(vararg pairs: Pair<String, Any?>) {
    startActivity(Intent(this, A::class.java).apply { putExtras(pairs.toBundle()) })
}

inline fun <reified A : Activity> Fragment.startActivityForResult(requestCode: Int, noinline action: (Intent.() -> Unit)? = null) {
    startActivityForResult(Intent(context, A::class.java).apply { action?.invoke(this) }, requestCode)
}

inline fun <reified A : Activity> Fragment.startActivityForResult(requestCode: Int, vararg pairs: Pair<String, Any?>) {
    startActivityForResult(Intent(context, A::class.java).apply { putExtras(pairs.toBundle()) }, requestCode)
}

inline fun <reified A : Activity> Activity.startActivityForResult(requestCode: Int, noinline action: (Intent.() -> Unit)? = null) {
    startActivityForResult(Intent(this, A::class.java).apply { action?.invoke(this) }, requestCode)
}

inline fun <reified A : Activity> Activity.startActivityForResult(requestCode: Int, vararg pairs: Pair<String, Any?>) {
    startActivityForResult(Intent(this, A::class.java).apply { putExtras(pairs.toBundle()) }, requestCode)
}

fun Context.getCompatColor(@ColorRes resId: Int): Int = ContextCompat.getColor(this, resId)

fun Context.getCompatDrawable(@DrawableRes resId: Int): Drawable = requireNotNull(ContextCompat.getDrawable(this, resId))

fun Fragment.getCompatColor(@ColorRes resId: Int): Int = ContextCompat.getColor(requireContext(), resId)

fun Fragment.getCompatDrawable(@DrawableRes resId: Int): Drawable = requireNotNull(ContextCompat.getDrawable(requireContext(), resId))

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


val Context.screenWidthPixels: Int
    get() = resources.displayMetrics.widthPixels

val Context.screenHeightPixels: Int
    get() = resources.displayMetrics.heightPixels