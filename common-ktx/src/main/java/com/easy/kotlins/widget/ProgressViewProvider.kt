package com.easy.kotlins.widget

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.easy.kotlins.R
import com.easy.kotlins.helper.activity
import com.easy.kotlins.helper.isVisible
import com.easy.kotlins.helper.textResource
import com.easy.kotlins.helper.weak

class ProgressViewLazy(private val factoryProducer: () -> ProgressViewFactory) :
    Lazy<ProgressView> {

    private var cached: ProgressView? = null

    override val value: ProgressView
        get() = cached ?: factoryProducer().create().also {
            cached = it
        }

    override fun isInitialized(): Boolean = cached != null

}


interface ProgressViewFactory {

    fun create(): ProgressView

}

internal class SimpleProgressViewFactory(private val parentProducer: () -> View) : ProgressViewFactory {

    override fun create(): ProgressView {
        return SimpleProgressView(parentProducer())
    }

}

internal class SimpleProgressView(parent: View) : PopupWindow(), ProgressView {

    private val parent: View? by weak { parent }

    init {
        isFocusable = true
        isOutsideTouchable = true
        width = WindowManager.LayoutParams.WRAP_CONTENT
        height = WindowManager.LayoutParams.WRAP_CONTENT
        contentView = View.inflate(parent.context, R.layout.layout_progress, null)
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun showProgress(message: CharSequence?) {
        contentView?.findViewById<TextView>(R.id.progress_text)?.text = message
        if (!isShowing) {
            val view = parent ?: return
            view.post { showAtLocation(view, Gravity.CENTER, 0, 0) }
        }
    }

    override fun showProgress(messageId: Int) {
        contentView?.findViewById<TextView>(R.id.progress_text)?.textResource = messageId
        if (!isShowing) {
            val view = parent ?: return
            view.post { showAtLocation(view, Gravity.CENTER, 0, 0) }
        }
    }

    override fun dismissProgress() {
        dismiss()
    }

}

val View.progressViewFactory: ProgressViewFactory
    get() = SimpleProgressViewFactory { this }

val Context.progressViewFactory: ProgressViewFactory
    get() {
        val activity = this.activity
            ?: throw IllegalStateException("Can not create ProgressView for a application or service context")
        return activity.progressViewFactory
    }

val Activity.progressViewFactory: ProgressViewFactory
    get() {
        val parent = window?.decorView
            ?: throw IllegalStateException("Can not create ProgressView for a activity not attach to window")
        return parent.progressViewFactory
    }

val Fragment.progressViewFactory: ProgressViewFactory
    get() = requireActivity().progressViewFactory


fun View.progressViews(factoryProducer: (() -> ProgressViewFactory)? = null): Lazy<ProgressView> {
    val factoryPromise = factoryProducer ?: { progressViewFactory }
    return ProgressViewLazy(factoryPromise)
}

fun Context.progressViews(factoryProducer: (() -> ProgressViewFactory)? = null): Lazy<ProgressView> {
    val factoryPromise = factoryProducer ?: { progressViewFactory }
    return ProgressViewLazy(factoryPromise)
}

fun Activity.progressViews(factoryProducer: (() -> ProgressViewFactory)? = null): Lazy<ProgressView> {
    val factoryPromise = factoryProducer ?: { progressViewFactory }
    return ProgressViewLazy(factoryPromise)
}

fun Fragment.progressViews(factoryProducer: (() -> ProgressViewFactory)? = null): Lazy<ProgressView> {
    val factoryPromise = factoryProducer ?: { progressViewFactory }
    return ProgressViewLazy(factoryPromise)
}
