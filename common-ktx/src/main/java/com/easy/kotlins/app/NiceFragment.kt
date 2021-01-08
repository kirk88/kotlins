package com.easy.kotlins.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

/**
 * Create by LiZhanPing on 2020/8/22
 */
abstract class NiceFragment(@LayoutRes private val layoutResId: Int) : Fragment(), NiceView {

    private var isLoaded = false
    private var isVisibleToUser = false
    private var isCallResume = false
    private var isCallUserVisibleHint = false

    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layoutResId, container, false)
    }

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onBind(savedInstanceState)
        onBindView(savedInstanceState)
        onBindEvent(savedInstanceState)
        view.post { onPrepared(savedInstanceState) }
    }

    override fun onResume() {
        super.onResume()
        isCallResume = true
        if (!isCallUserVisibleHint) isVisibleToUser = !isHidden
        onLazyPreparedInternal()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        isVisibleToUser = !hidden
        onLazyPreparedInternal()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        this.isVisibleToUser = isVisibleToUser
        isCallUserVisibleHint = true
        onLazyPreparedInternal()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isLoaded = false
        isVisibleToUser = false
        isCallResume = false
        isCallUserVisibleHint = false
    }

    private fun onLazyPreparedInternal() {
        if (!isLoaded && isVisibleToUser && isCallResume) {
            onLazyPrepared()
            isLoaded = true
        }
    }
}