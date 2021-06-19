@file:Suppress("unused")

package com.nice.kotlins.widget

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment

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

internal class DefaultTipView(context: Context) : TipView {

    private val handler = Handler(Looper.getMainLooper())

    private val toast: Toast by lazy {
        Toast.makeText(context, "", Toast.LENGTH_SHORT)
    }

    override fun show(message: CharSequence) {
        handler.post {
            toast.setText(message)
            toast.show()
        }
    }

    override fun show(messageId: Int) {
        handler.post {
            toast.setText(messageId)
            toast.show()
        }
    }

    override fun dismiss() {
        toast.cancel()
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
