package com.easy.kotlins.http

/**
 * Create by LiZhanPing on 2020/8/24
 */
interface OkFakerScope {

    fun add(faker: OkFaker)

    fun remove(faker: OkFaker)

    fun removeByTag(tag: Any)

    fun cancelByTag(tag: Any)

    fun cancel(faker: OkFaker): Boolean

    fun cancelAll()

    fun count(): Int
}