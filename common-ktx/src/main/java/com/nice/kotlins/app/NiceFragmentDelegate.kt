package com.nice.kotlins.app

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

internal class NiceFragmentDelegate(
        private val fragment: Fragment,
        private val callback: Callback
) {

    private val context: Context
        get() = fragment.requireContext()

    private val activity: Activity?
        get() = fragment.activity

    private val layoutInflater: LayoutInflater
        get() = fragment.layoutInflater

    private var subDecor: ViewGroup? = null

    fun onCreate() {
        ensureSubDecor()
    }

    fun getSubDecor(): ViewGroup {
        ensureSubDecor()
        return subDecor!!
    }

    fun <T : View?> findViewById(@IdRes id: Int): T {
        return subDecor?.findViewById(id) as T
    }

    fun setContentView(view: View) {
        ensureSubDecor()
        subDecor!!.let { decor ->
            decor.removeAllViews()
            decor.addView(view)
        }
        callback.onContentChanged()
    }

    fun setContentView(@LayoutRes id: Int) {
        ensureSubDecor()
        subDecor!!.let { decor ->
            decor.removeAllViews()
            layoutInflater.inflate(id, decor)
        }
        callback.onContentChanged()
    }

    fun setContentView(view: View, params: ViewGroup.LayoutParams) {
        ensureSubDecor()
        subDecor!!.let { decor ->
            decor.removeAllViews()
            decor.addView(view, params)
        }
        callback.onContentChanged()
    }

    fun addContentView(view: View, params: ViewGroup.LayoutParams) {
        ensureSubDecor()
        subDecor!!.addView(view, params)
        callback.onContentChanged()
    }

    fun getView(): View? {
        return subDecor
    }

    fun setTitle(title: CharSequence?) {
        activity?.title = title
    }

    fun getTitle(): CharSequence? {
        return activity?.title
    }

    fun setSubtitle(subtitle: CharSequence?) {
        activity?.let {
            when (it) {
                is HasActionBarSubtitle -> it.subtitle = subtitle
                is AppCompatActivity -> it.supportActionBar?.subtitle = subtitle
                else -> it.actionBar?.subtitle = subtitle
            }
        }
    }

    fun getSubtitle(): CharSequence? {
        return activity?.let {
            when (it) {
                is HasActionBarSubtitle -> it.subtitle
                is AppCompatActivity -> it.supportActionBar?.subtitle
                else -> it.actionBar?.subtitle
            }
        }
    }

    private fun ensureSubDecor() {
        if (subDecor != null) {
            return
        }

        subDecor = FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(-1, -1)
        }
    }

    interface Callback {

        fun onContentChanged()

    }

}