package com.nice.kothttp

interface CallScope : Iterable<OkCall<*>> {

    fun add(call: OkCall<*>)

    fun delete(call: OkCall<*>): Boolean

    fun remove(call: OkCall<*>): Boolean

    fun clear()

    fun size(): Int
}