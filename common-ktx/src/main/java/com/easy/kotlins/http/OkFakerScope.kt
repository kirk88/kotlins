package com.easy.kotlins.http

/**
 * Create by LiZhanPing on 2020/8/24
 */
interface OkFakerScope {

    fun add(tag: Any, faker: OkFaker): OkFaker

    fun deleteByTag(tag: Any)

    fun removeByTag(tag: Any)

    fun remove(faker: OkFaker): Boolean

    fun delete(faker: OkFaker): Boolean

    fun getByTag(tag: Any): List<OkFaker>

    fun clear()

    fun size(): Int
}