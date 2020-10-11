package com.easy.kotlins.http

/**
 * Create by LiZhanPing on 2020/8/24
 */
interface OkFakerScope {

    fun add(tag: Any, faker: NiceOkFaker): NiceOkFaker

    fun deleteByTag(tag: Any)

    fun removeByTag(tag: Any)

    fun remove(faker: NiceOkFaker): Boolean

    fun delete(faker: NiceOkFaker): Boolean

    fun getByTag(tag: Any): List<NiceOkFaker>

    fun clear()

    fun size(): Int
}