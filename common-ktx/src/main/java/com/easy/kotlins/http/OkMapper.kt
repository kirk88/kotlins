package com.easy.kotlins.http

/**
 * Create by LiZhanPing on 2020/4/29
 */
fun interface OkMapper<T, R> {
    @Throws(Exception::class)
    fun transform(value: T): OkResult<out R>
}