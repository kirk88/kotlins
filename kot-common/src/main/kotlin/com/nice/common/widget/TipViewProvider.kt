@file:Suppress("UNUSED")

package com.nice.common.widget

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.nice.common.helper.activity
import com.nice.common.helper.isInMainThread
import com.nice.common.helper.snackBar

class TipViewLazy(private val factoryProducer: () -> TipViewFactory) :
    Lazy<TipView> {

    private var cached: TipView? = null

    override val value: TipView
        get() = cached ?: factoryProducer().create().also {
            cached = it
        }

    override fun isInitialized(): Boolean = cached != null

}


fun interface TipViewFactory {

    fun create(): TipView

}

internal class ToastTipView(private val context: Context) : TipView {

    private val handler by lazy { Handler(Looper.getMainLooper()) }

    private var currentToast: Toast? = null

    override fun show(message: CharSequence) {
        val runnable = Runnable {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).also {
                currentToast = it
            }.show()
        }
        if (isInMainThread()) {
            runnable.run()
        } else {
            handler.post(runnable)
        }
    }

    override fun show(messageId: Int) {
        val runnable = Runnable {
            Toast.makeText(context, messageId, Toast.LENGTH_SHORT).also {
                currentToast = it
            }.show()
        }
        if (isInMainThread()) {
            runnable.run()
        } else {
            handler.post(runnable)
        }
    }

    override fun dismiss() {
        currentToast?.cancel()
    }

}

internal class ToastTipViewFactory(private val context: Context) : TipViewFactory {

    override fun create(): TipView {
        return ToastTipView(context)
    }

}

internal class SnackTipView(private val view: View) : TipView {

    private val handler by lazy { Handler(Looper.getMainLooper()) }

    private var currentSnackbar: Snackbar? = null

    override fun show(message: CharSequence) {
        val runnable = Runnable {
            view.snackBar(message, Snackbar.LENGTH_SHORT).also {
                currentSnackbar = it
            }.show()
        }
        if (isInMainThread()) {
            runnable.run()
        } else {
            handler.post(runnable)
        }
    }

    override fun show(messageId: Int) {
        val runnable = Runnable {
            view.snackBar(messageId, Snackbar.LENGTH_SHORT).also {
                currentSnackbar = it
            }.show()
        }
        if (isInMainThread()) {
            runnable.run()
        } else {
            handler.post(runnable)
        }
    }

    override fun dismiss() {
        currentSnackbar?.dismiss()
    }

}

internal class SnackTipViewFactory(private val view: View) : TipViewFactory {

    override fun create(): TipView {
        return SnackTipView(view)
    }

}

val View.defaultToastTipViewFactory: TipViewFactory
    get() = ToastTipViewFactory(context)

val Context.defaultToastTipViewFactory: TipViewFactory
    get() = ToastTipViewFactory(this)

val Fragment.defaultToastTipViewFactory: TipViewFactory
    get() = requireActivity().defaultToastTipViewFactory

val View.defaultSnackTipViewFactory: TipViewFactory
    get() = SnackTipViewFactory(this)

val Activity.defaultSnackTipViewFactory: TipViewFactory
    get() = window.findViewById<View>(android.R.id.content).defaultSnackTipViewFactory

val Context.defaultSnackTipViewFactory: TipViewFactory
    get() {
        val activity = this.activity
            ?: throw IllegalStateException("The application or service context has no TipViewFactory")
        return activity.defaultSnackTipViewFactory
    }

val Fragment.defaultSnackTipViewFactory: TipViewFactory
    get() = requireActivity().defaultSnackTipViewFactory


fun View.tipViews(factoryProducer: (() -> TipViewFactory)? = null): Lazy<TipView> {
    val factoryPromise = factoryProducer ?: { defaultToastTipViewFactory }
    return TipViewLazy(factoryPromise)
}

fun Context.tipViews(factoryProducer: (() -> TipViewFactory)? = null): Lazy<TipView> {
    val factoryPromise = factoryProducer ?: { defaultToastTipViewFactory }
    return TipViewLazy(factoryPromise)
}

fun Fragment.tipViews(factoryProducer: (() -> TipViewFactory)? = null): Lazy<TipView> {
    val factoryPromise = factoryProducer ?: { defaultToastTipViewFactory }
    return TipViewLazy(factoryPromise)
}