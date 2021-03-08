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

internal class SimpleProgressViewFactory(private val view: View) : ProgressViewFactory {

    override fun create(): ProgressView {
        return SimpleProgressView(view)
    }

}

internal class SimpleProgressView(view: View) : PopupWindow(), ProgressView {

    private val view: View? by weak { view }

    init {
        isFocusable = true
        isOutsideTouchable = true
        width = WindowManager.LayoutParams.WRAP_CONTENT
        height = WindowManager.LayoutParams.WRAP_CONTENT
        contentView = View.inflate(view.context, R.layout.layout_progress, null)
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun showProgress(message: CharSequence?) {
        contentView?.findViewById<TextView>(R.id.progress_text)?.text = message
        if (!isShowing) {
            val view = view ?: return
            view.post { showAtLocation(view, Gravity.CENTER, 0, 0) }
        }
    }

    override fun showProgress(messageId: Int) {
        contentView?.findViewById<TextView>(R.id.progress_text)?.textResource = messageId
        if (!isShowing) {
            val view = view ?: return
            view.post { showAtLocation(view.rootView, Gravity.CENTER, 0, 0) }
        }
    }

    override fun dismissProgress() {
        dismiss()
    }

}

val View.progressViewFactory: ProgressViewFactory
    get() = SimpleProgressViewFactory(this)

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
