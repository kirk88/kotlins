package com.nice.kotlins.helper

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

inline fun SwipeRefreshLayout.doOnRefresh(crossinline block: () -> Unit) = setOnRefreshListener {
    block()
}