package com.easy.kotlins.http.core


/**
 * Create by LiZhanPing on 2020/4/29
 */
class OkSource<T> {
    var source: T? = null
    var error: Throwable? = null
        private set

    private constructor(source: T) {
        this.source = source
    }

    private constructor(error: Throwable) {
        this.error = error
    }

    companion object {
        fun <T> just(source: T): OkSource<T> {
            return OkSource(requireNotNull(source))
        }

        fun <T> error(error: Throwable): OkSource<T> {
            return OkSource(error)
        }
    }
}