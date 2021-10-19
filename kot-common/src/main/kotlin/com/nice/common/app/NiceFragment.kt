@file:Suppress("unused")

package com.nice.common.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

abstract class NiceFragment(@LayoutRes private val contentLayoutId: Int = 0) : Fragment(),
        NiceFragmentDelegate.Callback, HasActionBarTitle, HasActionBarSubtitle {

    private val delegate: NiceFragmentDelegate by lazy {
        NiceFragmentDelegate(this, this)
    }

    val activityForResultLauncher = PocketActivityResultLauncher(ActivityResultContracts.StartActivityForResult())

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

    override fun setTitle(title: CharSequence?) {
        delegate.setTitle(title)
    }

    override fun setTitle(resId: Int) {
        setTitle(getText(resId))
    }

    override fun getTitle(): CharSequence? {
        return delegate.getTitle()
    }

    override fun setSubtitle(subtitle: CharSequence?) {
        delegate.setSubtitle(subtitle)
    }

    override fun setSubtitle(resId: Int) {
        setSubtitle(getText(resId))
    }

    override fun getSubtitle(): CharSequence? {
        return delegate.getSubtitle()
    }

    fun <T : View?> findViewById(@IdRes id: Int): T {
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

    override fun onContentChanged() {

    }

}