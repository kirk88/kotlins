@file:Suppress("unused")

package com.nice.kotlins.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

abstract class NiceFragment(@LayoutRes private val contentLayoutId: Int = 0) : Fragment(),
    NiceFragmentDelegate.Callback {

    private val delegate: NiceFragmentDelegate by lazy {
        NiceFragmentDelegate(this, this)
    }

    val activityForResultLauncher =
        PocketActivityResultLauncher(ActivityResultContracts.StartActivityForResult())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityForResultLauncher.register(this)
        delegate.onCreate()
        if (contentLayoutId != 0) {
            setContentView(contentLayoutId)
        }
    }

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val subDecor = delegate.getSubDecor()
        subDecor.post {
            onPostCreate(savedInstanceState)
        }
        return subDecor
    }

    open fun onPostCreate(savedInstanceState: Bundle?) {
    }

    override fun onDestroy() {
        super.onDestroy()
        activityForResultLauncher.unregister()
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
        delegate.setTitle(title)
    }

    fun setTitle(titleId: Int) {
        setTitle(getText(titleId))
    }

    fun getTitle(): CharSequence? {
        return delegate.getTitle()
    }

    fun setSubtitle(subtitle: CharSequence?) {
        delegate.setSubtitle(subtitle)
    }

    fun setSubtitle(subtitleId: Int) {
        setSubtitle(getText(subtitleId))
    }

    fun getSubtitle(): CharSequence? {
        return delegate.getSubtitle()
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