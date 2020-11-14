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


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layoutResId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ViewModelEvents.observe(this)
        onBind(savedInstanceState)
        onBindView(savedInstanceState)
        onBindEvent(savedInstanceState)
        view.post { onBindData(savedInstanceState) }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        isCallActivityCreated = true
        if (!isCallUserVisibleHint) isVisibleToUser = !isHidden
        onLazyBindDataInternal(savedInstanceState)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        isVisibleToUser = !hidden
        onLazyBindDataInternal()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        this.isVisibleToUser = isVisibleToUser
        isCallUserVisibleHint = true
        onLazyBindDataInternal()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        resetState()
    }

    private fun onLazyBindDataInternal(savedInstanceState: Bundle? = null) {
        if (!isLoaded && isVisibleToUser && isCallActivityCreated) {
            onLazyBindData(savedInstanceState)
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

    open fun onBindData(savedInstanceState: Bundle?) {}

    open fun onLazyBindData(savedInstanceState: Bundle?) {}
}