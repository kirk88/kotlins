package com.nice.kotlins.widget

interface InfiniteView {

    fun setRefreshState(state: InfiniteState)

    fun setLoadMoreState(state: InfiniteState)

}

enum class InfiniteState {
    STATE_IDLE, STATE_RUNNING, STATE_FAILED, STATE_COMPLETED
}