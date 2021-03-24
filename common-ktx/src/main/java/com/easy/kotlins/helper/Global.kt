@file:Suppress("unused")

package com.easy.kotlins.helper

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

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

fun Fragment.finishActivity() = activity?.finish()

inline fun <T : Fragment> T.withBundle(crossinline block: Bundle.() -> Unit): T = apply {
    val args = arguments ?: Bundle().also { arguments = it }
    args.apply(block)
}

fun <T : Fragment> T.withBundle(vararg args: Pair<String, Any?>): T = apply {
    val bundle = arguments ?: Bundle().also { arguments = it }
    bundle.putAll(args.toBundle())
}

inline fun <reified A : Activity> Fragment.startActivity(noinline block: (Intent.() -> Unit)? = null) {
    startActivity(Intent(context, A::class.java).apply { block?.invoke(this) })
}

inline fun <reified A : Activity> Fragment.startActivity(vararg args: Pair<String, Any?>) {
    startActivity(Intent(context, A::class.java).apply { putExtras(args.toBundle()) })
}

inline fun <reified A : Activity> Context.startActivity(noinline block: (Intent.() -> Unit)? = null) {
    startActivity(Intent(this, A::class.java).apply { block?.invoke(this) })
}

inline fun <reified A : Activity> Context.startActivity(vararg args: Pair<String, Any?>) {
    startActivity(Intent(this, A::class.java).apply { putExtras(args.toBundle()) })
}

inline fun <reified A : Activity> Fragment.startActivityForResult(
    requestCode: Int,
    noinline block: (Intent.() -> Unit)? = null
) {
    startActivityForResult(
        Intent(context, A::class.java).apply { block?.invoke(this) },
        requestCode
    )
}

inline fun <reified A : Activity> Fragment.startActivityForResult(
    requestCode: Int,
    vararg args: Pair<String, Any?>
) {
    startActivityForResult(
        Intent(context, A::class.java).apply { putExtras(args.toBundle()) },
        requestCode
    )
}

inline fun <reified A : Activity> Activity.startActivityForResult(
    requestCode: Int,
    noinline block: (Intent.() -> Unit)? = null
) {
    startActivityForResult(Intent(this, A::class.java).apply { block?.invoke(this) }, requestCode)
}

inline fun <reified A : Activity> Activity.startActivityForResult(
    requestCode: Int,
    vararg args: Pair<String, Any?>
) {
    startActivityForResult(
        Intent(this, A::class.java).apply { putExtras(args.toBundle()) },
        requestCode
    )
}

fun Context.getCompatColor(@ColorRes resId: Int): Int = ContextCompat.getColor(this, resId)

fun Context.getCompatColorStateList(@ColorRes resId: Int): ColorStateList? =
    ContextCompat.getColorStateList(this, resId)

fun Context.getCompatDrawable(@DrawableRes resId: Int): Drawable = requireNotNull(
    ContextCompat.getDrawable(
        this,
        resId
    )
)

fun Context.getDimension(@DimenRes resId: Int): Float = resources.getDimension(resId)

fun Context.getDimensionPixelOffset(@DimenRes resId: Int): Int =
    resources.getDimensionPixelOffset(resId)

fun Context.getDimensionPixelSize(@DimenRes resId: Int): Int =
    resources.getDimensionPixelSize(resId)

fun Fragment.getCompatColor(@ColorRes resId: Int): Int = ContextCompat.getColor(
    requireContext(),
    resId
)

fun Fragment.getCompatColorStateList(@ColorRes resId: Int): ColorStateList? =
    ContextCompat.getColorStateList(
        requireContext(),
        resId
    )

fun Fragment.getCompatDrawable(@DrawableRes resId: Int): Drawable = requireNotNull(
    ContextCompat.getDrawable(
        requireContext(),
        resId
    )
)

fun Fragment.getDimension(@DimenRes resId: Int): Float = resources.getDimension(resId)

fun Fragment.getDimensionPixelOffset(@DimenRes resId: Int): Int =
    resources.getDimensionPixelOffset(resId)

fun Fragment.getDimensionPixelSize(@DimenRes resId: Int): Int =
    resources.getDimensionPixelSize(resId)

val Context.screenWidthPixels: Int
    get() = resources.displayMetrics.widthPixels

val Context.screenHeightPixels: Int
    get() = resources.displayMetrics.heightPixels

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


val Context.activity: Activity?
    get() = scanActivity(this)

private tailrec fun scanActivity(context: Context): Activity? {
    return when (context) {
        is Activity -> context
        is ContextWrapper -> scanActivity(context.baseContext)
        else -> null
    }
}

val Context.appCompatActivity: AppCompatActivity?
    get() = scanCompatActivity(this)

private tailrec fun scanCompatActivity(context: Context?): AppCompatActivity? {
    return when (context) {
        is AppCompatActivity -> context
        is androidx.appcompat.view.ContextThemeWrapper -> scanCompatActivity(context.baseContext)
        is ContextThemeWrapper -> scanCompatActivity(context.baseContext)
        else -> null
    }
}