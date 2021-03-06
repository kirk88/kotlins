package com.nice.kotlins.http

interface OkFakerScope : Iterable<OkFaker<*>> {

    fun add(manager: OkFaker<*>)

    fun delete(manager: OkFaker<*>): Boolean

    fun remove(manager: OkFaker<*>): Boolean

    fun clear()

    fun size(): Int
}