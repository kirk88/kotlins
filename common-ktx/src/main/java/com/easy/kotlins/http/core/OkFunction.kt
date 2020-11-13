package com.easy.kotlins.http.core

/**
 * Create by LiZhanPing on 2020/4/29
 */
interface OkFunction<T, R> {
    @Throws(Exception::class)
    fun apply(r: T): OkResult<R>
}