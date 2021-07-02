package com.nice.kotlins.helper

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

inline fun SwipeRefreshLayout.doOnRefresh(crossinline action: () -> Unit) = setOnRefreshListener {
    action()
}