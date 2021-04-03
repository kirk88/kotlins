package com.easy.kotlins.viewmodel

import com.easy.kotlins.http.OkFaker
import com.easy.kotlins.http.OkFakerScope

interface ViewModelDataSource : OkFakerScope {

    fun <T> get(block: OkFaker.Builder<T>.() -> Unit): OkFaker<T> {
        return OkFaker.get(block).build().also { add(it) }
    }

    fun <T> post(block: OkFaker.Builder<T>.() -> Unit): OkFaker<T> {
        return OkFaker.post(block).build().also { add(it) }
    }

    fun <T> delete(block: OkFaker.Builder<T>.() -> Unit): OkFaker<T> {
        return OkFaker.delete(block).build().also { add(it) }
    }

    fun <T> put(block: OkFaker.Builder<T>.() -> Unit): OkFaker<T> {
        return OkFaker.put(block).build().also { add(it) }
    }

    fun <T> head(block: OkFaker.Builder<T>.() -> Unit): OkFaker<T> {
        return OkFaker.head(block).build().also { add(it) }
    }

    fun <T> patch(block: OkFaker.Builder<T>.() -> Unit): OkFaker<T> {
        return OkFaker.patch(block).build().also { add(it) }
    }

}