@file:Suppress("unused")

package com.nice.kotlins.helper

import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.PopupWindow

class ImeHeightObserver : ViewTreeObserver.OnGlobalLayoutListener {

    private val popup = PopupWindow().apply {
        width = 0
        height = ViewGroup.LayoutParams.MATCH_PARENT
        inputMethodMode = PopupWindow.INPUT_METHOD_NEEDED

        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private val contentRect = Rect().apply {
        bottom = Resources.getSystem().displayMetrics.heightPixels
    }
    private var contentBottom: Int = contentRect.bottom

    private var observer: ((Int) -> Unit)? = null

    override fun onGlobalLayout() {
        popup.contentView.getWindowVisibleDisplayFrame(contentRect)
        if (contentRect.bottom > contentBottom) {
            contentBottom = contentRect.bottom
        }

        val imeHeight = contentBottom - contentRect.bottom
        observer?.invoke(imeHeight)
    }

    fun register(view: View, observer: (Int) -> Unit) {
        this.observer = observer

        if (popup.contentView == null) {
            popup.contentView = View(view.context).also {
                it.viewTreeObserver.addOnGlobalLayoutListener(this)
            }
        }
        view.post {
            popup.showAtLocation(view, Gravity.NO_GRAVITY, 0, 0)
        }
    }

    fun unregister() {
        popup.contentView.viewTreeObserver.removeOnGlobalLayoutListener(this)
        popup.dismiss()
    }

    companion object {

        fun register(view: View, observer: (Int) -> Unit): ImeHeightObserver = ImeHeightObserver().apply {
            register(view, observer)
        }

    }

}