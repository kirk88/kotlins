@file:Suppress("unused")

package com.nice.common.widget

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.nice.common.helper.isMainThread

class TipViewLazy(private val factoryProducer: () -> TipViewFactory) :
    Lazy<TipView> {

    private var cached: TipView? = null

    override val value: TipView
        get() = cached ?: factoryProducer().create().also {
            cached = it
        }

    override fun isInitialized(): Boolean = cached != null

}


interface TipViewFactory {

    fun create(): TipView

}

internal class DefaultTipViewFactory(private val context: Context) : TipViewFactory {

    override fun create(): TipView {
        return DefaultTipView(context)
    }

}

internal class DefaultTipView(private val context: Context) : TipView {

    private val handler = Handler(Looper.getMainLooper())

    private var currentToast: Toast? = null

    override fun show(message: CharSequence) {
        val runnable = Runnable {
            val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT).also {
                currentToast = it
            }
            toast.show()
        }
        if (isMainThread) {
            runnable.run()
        } else {
            handler.post(runnable)
        }
    }

    override fun show(messageId: Int) {
        val runnable = Runnable {
            val toast = Toast.makeText(context, messageId, Toast.LENGTH_SHORT).also {
                currentToast = it
            }
            toast.show()
        }
        if (isMainThread) {
            runnable.run()
        } else {
            handler.post(runnable)
        }
    }

    override fun dismiss() {
        currentToast?.cancel()
    }

}

val View.tipViewFactory: TipViewFactory
    get() = DefaultTipViewFactory(context)

val Context.tipViewFactory: TipViewFactory
    get() = DefaultTipViewFactory(this)

val Fragment.tipViewFactory: TipViewFactory
    get() = requireActivity().tipViewFactory

fun View.tipViews(factoryProducer: (() -> TipViewFactory)? = null): Lazy<TipView> {
    val factoryPromise = factoryProducer ?: { tipViewFactory }
    return TipViewLazy(factoryPromise)
}

fun Context.tipViews(factoryProducer: (() -> TipViewFactory)? = null): Lazy<TipView> {
    val factoryPromise = factoryProducer ?: { tipViewFactory }
    return TipViewLazy(factoryPromise)
}

fun Fragment.tipViews(factoryProducer: (() -> TipViewFactory)? = null): Lazy<TipView> {
    val factoryPromise = factoryProducer ?: { tipViewFactory }
    return TipViewLazy(factoryPromise)
}