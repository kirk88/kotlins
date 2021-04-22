package com.nice.kotlins.viewmodel

import com.nice.kotlins.http.OkFaker
import com.nice.kotlins.http.OkFakerScope
import com.nice.kotlins.http.OkRequestMethod

interface ViewModelDataSource : OkFakerScope {

    fun <T> get(block: OkFaker.Builder<T>.() -> Unit): OkFaker<T> {
        return OkFaker.method(OkRequestMethod.GET, block).build().also { add(it) }
    }

    fun <T> post(block: OkFaker.Builder<T>.() -> Unit): OkFaker<T> {
        return OkFaker.method(OkRequestMethod.POST, block).build().also { add(it) }
    }

    fun <T> delete(block: OkFaker.Builder<T>.() -> Unit): OkFaker<T> {
        return OkFaker.method(OkRequestMethod.DELETE, block).build().also { add(it) }
    }

    fun <T> put(block: OkFaker.Builder<T>.() -> Unit): OkFaker<T> {
        return OkFaker.method(OkRequestMethod.PUT, block).build().also { add(it) }
    }

    fun <T> head(block: OkFaker.Builder<T>.() -> Unit): OkFaker<T> {
        return OkFaker.method(OkRequestMethod.HEAD, block).build().also { add(it) }
    }

    fun <T> patch(block: OkFaker.Builder<T>.() -> Unit): OkFaker<T> {
        return OkFaker.method(OkRequestMethod.PATCH, block).build().also { add(it) }
    }

}