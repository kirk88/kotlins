package com.easy.kotlins.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.easy.kotlins.viewmodel.ViewModelEvents

/**
 * Create by LiZhanPing on 2020/8/22
 */
abstract class NiceFragment(private val layoutResId: Int) : Fragment() {

    private var isLoaded = false
    private var isVisibleToUser = false
    private var isCallActivityCreated = false
    private var isCallUserVisibleHint = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layoutResId, container, false)
    }

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onBind(savedInstanceState)
        onBindView(savedInstanceState)
        onBindEvent(savedInstanceState)
        view.post { onPrepared(savedInstanceState) }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        isCallActivityCreated = true
        if (!isCallUserVisibleHint) isVisibleToUser = !isHidden
        onLazyPreparedInternal(savedInstanceState)
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
        resetState()
    }

    private fun onLazyPreparedInternal(savedInstanceState: Bundle? = null) {
        if (!isLoaded && isVisibleToUser && isCallActivityCreated) {
            onLazyPrepared(savedInstanceState)
            isLoaded = true
        }
    }

    private fun resetState() {
        isLoaded = false
        isVisibleToUser = false
        isCallActivityCreated = false
        isCallUserVisibleHint = false
    }

    open fun onBind(savedInstanceState: Bundle?) {}

    open fun onBindView(savedInstanceState: Bundle?) {}

    open fun onBindEvent(savedInstanceState: Bundle?) {}

    open fun onPrepared(savedInstanceState: Bundle?) {}

    open fun onLazyPrepared(savedInstanceState: Bundle?) {}
}