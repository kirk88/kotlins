package com.nice.kotlins.widget

interface RefreshView {

    fun finishRefresh()

    fun finishLoadMore(hasMore: Boolean = true)

}