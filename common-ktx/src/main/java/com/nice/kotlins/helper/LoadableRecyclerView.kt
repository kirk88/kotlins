package com.nice.kotlins.helper

import com.nice.kotlins.widget.LoadableRecyclerView

inline fun LoadableRecyclerView.doOnRefresh(crossinline block: () -> Unit) = setOnRefreshListener {
    block()
}

inline fun LoadableRecyclerView.doOnLoadMore(crossinline block: () -> Unit) = setOnLoadMoreListener {
    block()
}