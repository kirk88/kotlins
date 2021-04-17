@file:Suppress("unused")

package com.nice.kotlins.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

abstract class NiceFragment(@LayoutRes contentLayoutId: Int = 0) : Fragment(),
    NiceFragmentDelegate.Callback {

    private val delegate: NiceFragmentDelegate by lazy {
        NiceFragmentDelegate(this, this, contentLayoutId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        delegate.onCreate()
    }

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = delegate.onCreateView()
        view.post {
            onPostCreate(savedInstanceState)
        }
        return view
    }

    open fun onPostCreate(savedInstanceState: Bundle?) {
    }

    override fun getView(): View? {
        return delegate.getView()
    }

    fun <T : View> findViewById(@IdRes id: Int): T? {
        return delegate.findViewById(id)
    }

    fun setContentView(view: View) {
        delegate.setContentView(view)
    }

    fun setContentView(@LayoutRes id: Int) {
        delegate.setContentView(id)
    }

    fun setContentView(view: View, params: ViewGroup.LayoutParams) {
        delegate.setContentView(view, params)
    }

    fun addContentView(view: View, params: ViewGroup.LayoutParams) {
        delegate.addContentView(view, params)
    }

    fun setTitle(title: CharSequence?) {
        activity?.title = title
    }

    fun setTitle(titleId: Int) {
        setTitle(getText(titleId))
    }

    fun getTitle(): CharSequence? {
        return activity?.title
    }

    fun setSubtitle(subtitle: CharSequence?) {
        activity?.let {
            if (it is NiceActivity) {
                it.subtitle = subtitle
            } else if (it is AppCompatActivity) {
                it.supportActionBar?.subtitle = subtitle
            }
        }
    }

    fun setSubtitle(subtitleId: Int) {
        setSubtitle(getText(subtitleId))
    }

    fun getSubtitle(): CharSequence? {
        return activity?.let {
            when (it) {
                is NiceActivity -> it.subtitle
                is AppCompatActivity -> it.supportActionBar?.subtitle
                else -> null
            }
        }
    }

    override fun onContentChanged() {

    }

}

var NiceFragment.title: CharSequence?
    get() = getTitle()
    set(title) {
        setTitle(title)
    }

var NiceFragment.subtitle: CharSequence?
    get() = getSubtitle()
    set(subtitle) {
        setSubtitle(subtitle)
    }