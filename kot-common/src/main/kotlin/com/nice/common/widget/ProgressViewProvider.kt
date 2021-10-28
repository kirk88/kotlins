@file:Suppress("UNUSED")

package com.nice.common.widget

import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.nice.common.R
import com.nice.common.helper.ifNullOrEmpty

class ProgressViewLazy(private val factoryProducer: () -> ProgressViewFactory) :
    Lazy<ProgressView> {

    private var cached: ProgressView? = null

    override val value: ProgressView
        get() = cached ?: factoryProducer().create().also {
            cached = it
        }

    override fun isInitialized(): Boolean = cached != null

}


fun interface ProgressViewFactory {

    fun create(): ProgressView

}

internal class DefaultProgressViewFactory(private val context: Context) : ProgressViewFactory {

    override fun create(): ProgressView {
        return DefaultProgressView(context)
    }

}

internal class DefaultProgressView(context: Context) : ProgressView {

    private val handler: Handler by lazy { Handler(Looper.getMainLooper()) }

    private val dialog: Dialog by lazy {
        Dialog(context, R.style.Theme_Progress_Dialog).apply {
            setCancelable(true)
            setCanceledOnTouchOutside(false)
            setContentView(R.layout.abc_dialog_progress_view)
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
        delayShow(dialog.context.getText(messageId))
    }

    override fun dismiss() {
        delayHide()
    }

    private fun delayShow(message: CharSequence?) {
        messageText = message

        startTime = -1L
        isDismissed = false
        handler.removeCallbacks(hideRunnable)
        isPostHide = false
        if (!isPostShow) {
            handler.postDelayed(showRunnable, MIN_DELAY)
            isPostShow = true
        }
    }

    private fun delayHide() {
        isDismissed = true
        handler.removeCallbacks(showRunnable)
        isPostShow = false
        val diff: Long = System.currentTimeMillis() - startTime
        if (diff >= MIN_SHOW_TIME || startTime == -1L) {
            handler.postDelayed(hideRunnable, 0L)
        } else {
            if (!isPostHide) {
                handler.postDelayed(hideRunnable, MIN_SHOW_TIME - diff)
                isPostHide = true
            }
        }
    }

    private fun showInternal() {
        dialog.findViewById<TextView>(R.id.message)?.apply {
            text = messageText.ifNullOrEmpty { context.getText(R.string.loader_progress_message) }
        }
        dialog.show()
    }

    private fun dismissInternal() {
        dialog.dismiss()
    }

    companion object {
        private const val MIN_SHOW_TIME: Long = 400L
        private const val MIN_DELAY: Long = 400L
    }

}

val View.defaultProgressViewFactory: ProgressViewFactory
    get() = DefaultProgressViewFactory(context)

val Context.defaultProgressViewFactory: ProgressViewFactory
    get() = DefaultProgressViewFactory(this)

val Fragment.defaultProgressViewFactory: ProgressViewFactory
    get() = requireActivity().defaultProgressViewFactory

fun View.progressViews(factoryProducer: (() -> ProgressViewFactory)? = null): Lazy<ProgressView> {
    val factoryPromise = factoryProducer ?: { defaultProgressViewFactory }
    return ProgressViewLazy(factoryPromise)
}

fun Context.progressViews(factoryProducer: (() -> ProgressViewFactory)? = null): Lazy<ProgressView> {
    val factoryPromise = factoryProducer ?: { defaultProgressViewFactory }
    return ProgressViewLazy(factoryPromise)
}

fun Fragment.progressViews(factoryProducer: (() -> ProgressViewFactory)? = null): Lazy<ProgressView> {
    val factoryPromise = factoryProducer ?: { defaultProgressViewFactory }
    return ProgressViewLazy(factoryPromise)
}
