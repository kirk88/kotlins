@file:Suppress("UNUSED")

package com.nice.common.helper

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

fun View.snackBar(message: CharSequence, duration: Int = Snackbar.LENGTH_SHORT): Snackbar =
    Snackbar.make(this, message, duration).apply {
        addCallback(SnackBarLocationChangedCallback())
    }

fun Activity.snackBar(message: CharSequence, duration: Int = Snackbar.LENGTH_SHORT): Snackbar =
    window.findViewById<View>(android.R.id.content).snackBar(message, duration)

fun Fragment.snackBar(message: CharSequence, duration: Int = Snackbar.LENGTH_SHORT): Snackbar =
    requireActivity().snackBar(message, duration)

fun View.snackBar(@StringRes resId: Int, duration: Int = Snackbar.LENGTH_SHORT): Snackbar =
    Snackbar.make(this, resId, duration).apply {
        addCallback(SnackBarLocationChangedCallback())
    }

fun Activity.snackBar(@StringRes resId: Int, duration: Int = Snackbar.LENGTH_SHORT): Snackbar =
    window.findViewById<View>(android.R.id.content).snackBar(resId, duration)

fun Fragment.snackBar(@StringRes resId: Int, duration: Int = Snackbar.LENGTH_SHORT): Snackbar =
    requireActivity().snackBar(resId, duration)

fun View.showSnackBar(message: CharSequence, duration: Int = Snackbar.LENGTH_SHORT) =
    snackBar(message, duration).show()

fun Fragment.showSnackBar(message: CharSequence, duration: Int = Snackbar.LENGTH_SHORT) =
    snackBar(message, duration).show()

fun Activity.showSnackBar(message: CharSequence, duration: Int = Snackbar.LENGTH_SHORT) =
    snackBar(message, duration).show()

fun View.showSnackBar(@StringRes resId: Int, duration: Int = Snackbar.LENGTH_SHORT) =
    snackBar(resId, duration).show()

fun Fragment.showSnackBar(@StringRes resId: Int, duration: Int = Snackbar.LENGTH_SHORT) =
    snackBar(resId, duration).show()

fun Activity.showSnackBar(@StringRes resId: Int, duration: Int = Snackbar.LENGTH_SHORT) =
    snackBar(resId, duration).show()

private class SnackBarLocationChangedCallback : BaseTransientBottomBar.BaseCallback<Snackbar>() {

    private var originalBottomMargin = -1
    private var bottomMarginRunnable: Runnable? = null

    private val observer = ImeChangeObserver()

    override fun onDismissed(transientBottomBar: Snackbar, event: Int) {
        observer.unregister()
    }

    override fun onShown(transientBottomBar: Snackbar) {
        val view = transientBottomBar.view
        observer.register(view) { height ->
            bottomMarginRunnable?.let { view.removeCallbacks(it) }
            view.post(BottomMarginRunnable(view, height).also {
                bottomMarginRunnable = it
            })
        }
    }

    private inner class BottomMarginRunnable(
        private val view: View,
        private val extraBottomMargin: Int
    ) : Runnable {
        override fun run() {
            val layoutParams = view.layoutParams as? ViewGroup.MarginLayoutParams ?: return
            if (originalBottomMargin == -1) {
                originalBottomMargin = layoutParams.bottomMargin
            }

            layoutParams.bottomMargin = originalBottomMargin + extraBottomMargin
            view.requestLayout()
        }
    }

}