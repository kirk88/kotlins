package com.nice.kotlins.widget

interface LoadableView {

    fun setRefreshState(state: LoadState)

    fun setLoadMoreState(state: LoadState)

}

enum class LoadState {
    STATE_IDLE, STATE_RUNNING, STATE_FAILED, STATE_COMPLETED
}