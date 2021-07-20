package com.nice.common.helper

import com.nice.common.widget.InfiniteRecyclerView

inline fun InfiniteRecyclerView.doOnRefresh(crossinline action: () -> Unit) = setOnRefreshListener {
    action()
}

inline fun InfiniteRecyclerView.doOnLoadMore(crossinline action: () -> Unit) = setOnLoadMoreListener {
    action()
}