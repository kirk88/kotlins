package com.nice.kotlins.app

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

internal class NiceFragmentDelegate(
    private val fragment: Fragment,
    private val callback: Callback,
    @LayoutRes private val contentLayoutId: Int
) {

    private val context: Context
        get() = fragment.requireContext()

    private val layoutInflater: LayoutInflater
        get() = fragment.layoutInflater

    private var subDecor: ViewGroup? = null

    fun onCreate(){
        if(contentLayoutId != 0){
            setContentView(contentLayoutId)
        }
    }

    fun getSubDecor(): ViewGroup {
        ensureSubDecor()
        return subDecor!!
    }

    fun <T : View> findViewById(@IdRes id: Int): T? {
        return subDecor?.findViewById(id)
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

    fun getView(): View?{
        return subDecor
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