package com.nice.kothttp

interface RequestScope : Iterable<OkCall<*>> {

    fun add(call: OkCall<*>)

    fun delete(call: OkCall<*>): Boolean

    fun remove(call: OkCall<*>): Boolean

    fun clear()

    fun size(): Int
}