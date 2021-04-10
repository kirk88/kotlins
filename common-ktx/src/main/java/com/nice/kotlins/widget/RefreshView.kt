package com.nice.kotlins.widget

interface RefreshView {

    fun finishRefresh()

    fun refreshFailed()

    fun finishLoadMore(hasMore: Boolean = true)

    fun loadMoreFailed()

}