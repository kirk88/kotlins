package com.easy.kotlins.http

/**
 * Create by LiZhanPing on 2020/8/24
 */
interface OkFakerScope {

    fun addRequest(faker: OkFaker)

    fun cancelByTag(tag: Any)

    fun cancel(faker: OkFaker): Boolean

    fun cancelAll()

    fun count(): Int
}