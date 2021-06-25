package com.nice.kotlins.helper

import com.nice.kotlins.widget.InfiniteRecyclerView

inline fun InfiniteRecyclerView.doOnRefresh(crossinline block: () -> Unit) = setOnRefreshListener {
    block()
}

inline fun InfiniteRecyclerView.doOnLoadMore(crossinline block: () -> Unit) = setOnLoadMoreListener {
    block()
}