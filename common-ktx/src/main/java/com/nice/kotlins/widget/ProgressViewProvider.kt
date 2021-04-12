package com.nice.kotlins.widget

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.fragment.app.Fragment
import com.nice.kotlins.R
import com.nice.kotlins.databinding.DialogProgressViewBinding
import com.nice.kotlins.helper.activity
import com.nice.kotlins.helper.installTo
import com.nice.kotlins.helper.viewBinding
import com.nice.kotlins.helper.weak

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

    private val binding: DialogProgressViewBinding = viewBinding(LayoutInflater.from(parent.context))

    private var messageText: CharSequence? = null

    private var startTime: Long = -1L
    private var isDismissed: Boolean = false
    private var isPostShow: Boolean = false
    private val showRunnable: Runnable = Runnable {
        isPostShow = false
        if (!isDismissed) {
            startTime = System.currentTimeMillis()
            show()
        }
    }

    private var isPostHide: Boolean = false
    private val hideRunnable: Runnable = Runnable {
        isPostHide = false
        startTime = -1L
        dismiss()
    }

    init {
        width = WindowManager.LayoutParams.WRAP_CONTENT
        height = WindowManager.LayoutParams.WRAP_CONTENT
        inputMethodMode = INPUT_METHOD_NOT_NEEDED
        animationStyle = R.style.Animation_Progress_Popup
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.installTo(this)
    }

    override fun showProgress(message: CharSequence?) {
        messageText = message
        delayShow()
    }

    override fun showProgress(messageId: Int) {
        messageText = view?.context?.getText(messageId)
        delayShow()
    }

    override fun dismissProgress() {
        delayHide()
    }

    private fun delayShow() {
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

    private fun show() {
        binding.message.apply {
            text = messageText ?: context.getText(R.string.loader_progress_tip)
        }
        view?.let { showAtLocation(it, Gravity.CENTER, 0, 0) }
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
