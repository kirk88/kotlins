package com.nice.kothttp

interface CallManager : Iterable<OkCall<*>> {

    val size: Int

    fun add(call: OkCall<*>)

    fun delete(call: OkCall<*>): Boolean

    fun deleteByTag(tag: Any): Boolean

    fun remove(call: OkCall<*>): Boolean

    fun removeByTag(tag: Any): Boolean

    fun clear()

}