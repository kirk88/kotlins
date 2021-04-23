package com.nice.kotlins.widget

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment

class TipsViewLazy(private val factoryProducer: () -> TipsViewFactory) :
    Lazy<TipsView> {

    private var cached: TipsView? = null

    override val value: TipsView
        get() = cached ?: factoryProducer().create().also {
            cached = it
        }

    override fun isInitialized(): Boolean = cached != null

}


interface TipsViewFactory {

    fun create(): TipsView

}

internal class DefaultTipsViewFactory(private val context: Context) : TipsViewFactory {

    override fun create(): TipsView {
        return DefaultTipsView(context)
    }

}

internal class DefaultTipsView(context: Context) : TipsView {

    private val toast: Toast by lazy {
        Toast(context).apply {
            duration = Toast.LENGTH_SHORT
        }
    }

    override fun show(message: CharSequence) {
        toast.setText(message)
        toast.show()
    }

    override fun show(messageId: Int) {
        toast.setText(messageId)
        toast.show()
    }

    override fun dismiss() {
        toast.cancel()
    }

}

val View.tipsViewFactory: TipsViewFactory
    get() = DefaultTipsViewFactory(context)

val Context.tipsViewFactory: TipsViewFactory
    get() = DefaultTipsViewFactory(this)

val Fragment.tipsViewFactory: TipsViewFactory
    get() = requireActivity().tipsViewFactory

fun View.tipsViews(factoryProducer: (() -> TipsViewFactory)? = null): Lazy<TipsView> {
    val factoryPromise = factoryProducer ?: { tipsViewFactory }
    return TipsViewLazy(factoryPromise)
}

fun Context.tipsViews(factoryProducer: (() -> TipsViewFactory)? = null): Lazy<TipsView> {
    val factoryPromise = factoryProducer ?: { tipsViewFactory }
    return TipsViewLazy(factoryPromise)
}

fun Fragment.tipsViews(factoryProducer: (() -> TipsViewFactory)? = null): Lazy<TipsView> {
    val factoryPromise = factoryProducer ?: { tipsViewFactory }
    return TipsViewLazy(factoryPromise)
}
