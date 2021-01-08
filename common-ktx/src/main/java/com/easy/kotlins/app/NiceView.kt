package com.easy.kotlins.app

import android.os.Bundle

internal interface NiceView {

    fun onBind(savedInstanceState: Bundle?) {}

    fun onBindView(savedInstanceState: Bundle?) {}

    fun onBindEvent(savedInstanceState: Bundle?) {}

    fun onPrepared(savedInstanceState: Bundle?) {}

    fun onLazyPrepared() {}

}