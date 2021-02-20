package com.easy.kotlins.http

/**
 * Create by LiZhanPing on 2020/8/24
 */
interface OkFakerScope : Iterable<OkFaker<*>> {

    fun add(manager: OkFaker<*>)

    fun delete(manager: OkFaker<*>): Boolean

    fun remove(manager: OkFaker<*>): Boolean

    fun clear()

    fun size(): Int
}