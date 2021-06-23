package com.nice.kotlins.widget

interface RefreshView {

    fun finishRefresh(state: Int = 0)

    fun finishLoadMore(state: Int = 0)

}