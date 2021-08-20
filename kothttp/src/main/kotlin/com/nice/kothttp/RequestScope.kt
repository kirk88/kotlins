package com.nice.kothttp

interface RequestScope : Iterable<OkRequest<*>> {

    fun add(request: OkRequest<*>)

    fun delete(request: OkRequest<*>): Boolean

    fun remove(request: OkRequest<*>): Boolean

    fun clear()

    fun size(): Int
}