package com.nice.kotlins.viewmodel

import com.nice.kotlins.http.DefaultOkFakerScope
import com.nice.kotlins.http.OkFaker
import com.nice.kotlins.http.OkFakerScope
import com.nice.kotlins.http.OkRequestMethod

abstract class ViewModelDataSource : OkFakerScope by DefaultOkFakerScope() {

    fun <T> get(block: OkFaker.Builder<T>.() -> Unit): OkFaker<T> {
        return OkFaker.builder(OkRequestMethod.GET, OkFaker.globalConfig, block).build()
            .also { add(it) }
    }

    fun <T> post(block: OkFaker.Builder<T>.() -> Unit): OkFaker<T> {
        return OkFaker.builder(OkRequestMethod.POST, OkFaker.globalConfig, block).build()
            .also { add(it) }
    }

    fun <T> delete(block: OkFaker.Builder<T>.() -> Unit): OkFaker<T> {
        return OkFaker.builder(OkRequestMethod.DELETE, OkFaker.globalConfig, block).build()
            .also { add(it) }
    }

    fun <T> put(block: OkFaker.Builder<T>.() -> Unit): OkFaker<T> {
        return OkFaker.builder(OkRequestMethod.PUT, OkFaker.globalConfig, block).build()
            .also { add(it) }
    }

    fun <T> head(block: OkFaker.Builder<T>.() -> Unit): OkFaker<T> {
        return OkFaker.builder(OkRequestMethod.HEAD, OkFaker.globalConfig, block).build()
            .also { add(it) }
    }

    fun <T> patch(block: OkFaker.Builder<T>.() -> Unit): OkFaker<T> {
        return OkFaker.builder(OkRequestMethod.PATCH, OkFaker.globalConfig, block).build()
            .also { add(it) }
    }

}