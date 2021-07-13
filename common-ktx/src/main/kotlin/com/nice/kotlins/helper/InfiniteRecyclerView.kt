package com.nice.kotlins.helper

import com.nice.kotlins.widget.InfiniteRecyclerView

inline fun InfiniteRecyclerView.doOnRefresh(crossinline action: () -> Unit) = setOnRefreshListener {
    action()
}

inline fun InfiniteRecyclerView.doOnLoadMore(crossinline action: () -> Unit) = setOnLoadMoreListener {
    action()
}