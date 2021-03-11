package com.easy.kotlins.widget

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.easy.kotlins.R
import com.easy.kotlins.helper.activity
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

internal class DefaultProgressViewFactory(private val parent: View) : ProgressViewFactory {

    override fun create(): ProgressView {
        return DefaultProgressView(parent)
    }

}

internal class DefaultProgressView(parent: View) : PopupWindow(), ProgressView {

    private val view: View? by weak { parent.rootView }

    private var isPostShow: Boolean = false
    private val showRunnable: Runnable = Runnable {
        isPostShow = false
        view?.let { showAtLocation(it, Gravity.CENTER, 0, 0) }
    }

    private var isPostHide: Boolean = false
    private val hideRunnable: Runnable = Runnable {
        isPostHide = false
        dismiss()
    }

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
        view?.let { show(it) }
    }

    override fun showProgress(messageId: Int) {
        contentView?.findViewById<TextView>(R.id.progress_text)?.textResource = messageId
        view?.let { show(it) }
    }

    override fun dismissProgress() {
        view?.let { hide(it) }
    }

    private fun show(view: View) {
        view.removeCallbacks(hideRunnable)
        isPostHide = false
        if (!isPostShow) {
            view.post(showRunnable)
            isPostShow = true
        }
    }

    private fun hide(view: View) {
        view.removeCallbacks(showRunnable)
        isPostShow = false
        if (!isPostHide) {
            view.post(hideRunnable)
            isPostHide = true
        }
    }

}

val View.progressViewFactory: ProgressViewFactory
    get() = DefaultProgressViewFactory(this)

val Context.progressViewFactory: ProgressViewFactory
    get() {
        val activity = this.activity
            ?: throw IllegalStateException("Can not create ProgressView for a application or service context")
        return activity.progressViewFactory
    }

val Activity.progressViewFactory: ProgressViewFactory
    get() {
        val view = window?.decorView
            ?: throw IllegalStateException("Can not create ProgressView for a activity not attach to window")
        return view.progressViewFactory
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
