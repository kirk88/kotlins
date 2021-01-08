package com.easy.kotlins.widget

/**
 * Create by LiZhanPing on 2020/10/11
 */

interface RefreshView {

    fun finishRefresh()

    fun refreshFailed()

    fun finishLoadMore(hasMore: Boolean = true)

    fun loadMoreFailed()

}