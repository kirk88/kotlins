package com.nice.kothttp

interface CallManager : Iterable<OkCall<*>> {

    fun add(call: OkCall<*>)

    fun delete(call: OkCall<*>): Boolean

    fun deleteByTag(tag: Any): Boolean

    fun remove(call: OkCall<*>): Boolean

    fun removeByTag(tag: Any): Boolean

    fun clear()

    fun size(): Int
}