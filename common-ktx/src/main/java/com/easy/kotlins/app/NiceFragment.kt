package com.easy.kotlins.app

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

abstract class NiceFragment(@LayoutRes layoutResId: Int = 0) : Fragment(layoutResId), NiceView {

    private var isLoaded = false
    private var isCallResume = false

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onBind(savedInstanceState)
        onBindView(savedInstanceState)
        onBindEvent(savedInstanceState)
        onPrepared(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        isCallResume = true
        onLazyPreparedInternal()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isLoaded = false
        isCallResume = false
    }

    private fun onLazyPreparedInternal() {
        if (!isLoaded && isCallResume) {
            requireView().post { onLazyPrepared() }
            isLoaded = true
        }
    }

}