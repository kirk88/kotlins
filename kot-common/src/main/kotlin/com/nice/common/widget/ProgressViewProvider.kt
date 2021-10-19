@file:Suppress("unused")

package com.nice.common.widget

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
import com.nice.common.R
import com.nice.common.helper.activity
import com.nice.common.helper.findViewById
import com.nice.common.helper.ifNullOrEmpty
import com.nice.common.helper.weak

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

internal class DefaultProgressView(parent: View) : ProgressView {

    private val view: View? by weak { parent.rootView }

    private val popup: PopupWindow by lazy {
        PopupWindow().apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            inputMethodMode = PopupWindow.INPUT_METHOD_NOT_NEEDED
            animationStyle = R.style.Animation_Progress_Popup
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            contentView = View.inflate(parent.context, R.layout.abc_dialog_progress_view, null)
        }
    }

    private var messageText: CharSequence? = null

    private var startTime: Long = -1L
    private var isDismissed: Boolean = false
    private var isPostShow: Boolean = false
    private val showRunnable: Runnable = Runnable {
        isPostShow = false
        if (!isDismissed) {
            startTime = System.currentTimeMillis()
            showInternal()
        }
    }

    private var isPostHide: Boolean = false
    private val hideRunnable: Runnable = Runnable {
        isPostHide = false
        startTime = -1L
        dismissInternal()
    }

    override fun show(message: CharSequence?) {
        delayShow(message)
    }

    override fun show(messageId: Int) {
        delayShow(view?.context?.getText(messageId))
    }

    override fun dismiss() {
        delayHide()
    }

    private fun delayShow(message: CharSequence?) {
        messageText = message

        startTime = -1L
        isDismissed = false
        removeCallbacks(hideRunnable)
        isPostHide = false
        if (!isPostShow) {
            postDelayed(showRunnable, MIN_DELAY)
            isPostShow = true
        }
    }

    private fun delayHide() {
        isDismissed = true
        removeCallbacks(showRunnable)
        isPostShow = false
        val diff: Long = System.currentTimeMillis() - startTime
        if (diff >= MIN_SHOW_TIME || startTime == -1L) {
            postDelayed(hideRunnable, 0L)
        } else {
            if (!isPostHide) {
                postDelayed(hideRunnable, MIN_SHOW_TIME - diff)
                isPostHide = true
            }
        }
    }

    private fun showInternal() {
        popup.findViewById<TextView>(R.id.message)?.apply {
            text = messageText.ifNullOrEmpty {
                context.getText(R.string.loader_progress_message)
            }
        }
        view?.let { popup.showAtLocation(it, Gravity.CENTER, 0, 0) }
    }

    private fun dismissInternal() {
        popup.dismiss()
    }

    private fun removeCallbacks(runnable: Runnable) {
        view?.removeCallbacks(runnable)
    }

    private fun postDelayed(runnable: Runnable, delayMillis: Long) {
        view?.postDelayed(runnable, delayMillis)
    }

    companion object {
        private const val MIN_SHOW_TIME: Long = 400L
        private const val MIN_DELAY: Long = 400L
    }

}

val View.progressViewFactory: ProgressViewFactory
    get() = DefaultProgressViewFactory(this)

val Context.progressViewFactory: ProgressViewFactory
    get() {
        val activity = this.activity
            ?: throw IllegalStateException("The application or service context has no ProgressViewFactory")
        return activity.progressViewFactory
    }

val Activity.progressViewFactory: ProgressViewFactory
    get() = window.decorView.progressViewFactory

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
