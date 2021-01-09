package com.easy.kotlins.http

/**
 * Create by LiZhanPing on 2020/8/24
 */
interface OkManagerScope {

    fun add(manager: OkManager<*, *>)

    fun delete(manager: OkManager<*, *>): Boolean

    fun remove(manager: OkManager<*, *>): Boolean

    fun clear()

    fun size(): Int
}