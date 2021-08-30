@file:Suppress("unused")

package com.nice.common.helper

import android.content.Context
import android.graphics.Rect
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.PopupWindow
import androidx.core.view.ViewCompat
import androidx.core.view.doOnAttach
import com.nice.common.R

class ImeChangeObserver {

    private var popup: ObservablePopupWindow? = null
    private var receiver: ImeChangeReceiver? = null

    fun register(view: View, receiver: ImeChangeReceiver) {
        if (ViewCompat.isAttachedToWindow(view)) {
            registerInternal(view, receiver)
        } else {
            view.doOnAttach {
                registerInternal(it, receiver)
            }
        }
    }

    fun unregister() {
        if (receiver != null) {
            popup?.removeReceiver(receiver!!)
            receiver = null
        }
        popup?.dismiss()
        popup = null
    }

    private fun registerInternal(view: View, receiver: ImeChangeReceiver) {
        this.receiver = receiver

        val rootView = view.rootView

        val popup = rootView.getTag(R.id.ime_change_observer_popup_id) as? ObservablePopupWindow
            ?: ObservablePopupWindow(view.context).also {
                rootView.setTag(R.id.ime_change_observer_popup_id, it)
            }

        this.popup = popup.apply {
            addReceiver(receiver)
            show(rootView)
        }
    }


    private class ObservablePopupWindow(context: Context) : PopupWindow(),
        ViewTreeObserver.OnGlobalLayoutListener {

        private val receivers = mutableSetOf<ImeChangeReceiver>()

        private val contentRect = Rect().apply {
            bottom = context.resources.displayMetrics.heightPixels
        }
        private var contentBottom: Int = contentRect.bottom
        private var currentHeight: Int = 0

        private var viewTreeObserver: ViewTreeObserver? = null

        init {
            width = 0
            height = ViewGroup.LayoutParams.MATCH_PARENT
            inputMethodMode = INPUT_METHOD_NEEDED
            contentView = View(context)

            doOnDismiss { removeListener(this) }
        }

        override fun onGlobalLayout() {
            contentView.getWindowVisibleDisplayFrame(contentRect)
            if (contentRect.bottom > contentBottom) {
                contentBottom = contentRect.bottom
            }

            val height = contentBottom - contentRect.bottom
            if (currentHeight != height) {
                currentHeight = height
                for (receiver in receivers) {
                    receiver.onReceive(height)
                }
            }
        }

        override fun showAtLocation(parent: View, gravity: Int, x: Int, y: Int) {
            if (!parent.isAttachedToWindow) {
                return
            }
            super.showAtLocation(parent, gravity, x, y)
            removeListener(this)
            addListener(this)
        }

        fun show(parent: View) = showAtLocation(parent, Gravity.NO_GRAVITY, 0, 0)

        fun addReceiver(receiver: ImeChangeReceiver) {
            if (receivers.add(receiver) && currentHeight > 0) {
                receiver.onReceive(currentHeight)
            }
        }

        fun removeReceiver(receiver: ImeChangeReceiver) {
            receivers.remove(receiver)
        }

        private fun addListener(listener: ViewTreeObserver.OnGlobalLayoutListener) {
            viewTreeObserver = contentView.viewTreeObserver.also {
                it.addOnGlobalLayoutListener(listener)
            }
        }

        private fun removeListener(listener: ViewTreeObserver.OnGlobalLayoutListener) {
            val vto = viewTreeObserver.let {
                if (it != null && it.isAlive) it
                else contentView.viewTreeObserver
            }
            vto.removeOnGlobalLayoutListener(listener)
        }

    }

    fun interface ImeChangeReceiver {

        fun onReceive(height: Int)

    }

    companion object {

        fun register(view: View, receiver: ImeChangeReceiver): ImeChangeObserver =
            ImeChangeObserver().also {
                it.register(view, receiver)
            }

    }

}