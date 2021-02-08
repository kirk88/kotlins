package com.easy.kotlins.http

import okhttp3.OkHttpClient
import okhttp3.Request

class DefaultOkFakerScope: OkFakerScope, Iterable<OkFaker<*>> {

    private var resources: MutableList<OkFaker<*>>? = null

    override fun add(manager: OkFaker<*>) {
        synchronized(this) {
            var list = resources

            if (list == null) {
                list = arrayListOf()
                resources = list
            }

            list.add(manager)
        }
    }

    override fun delete(manager: OkFaker<*>): Boolean {
        synchronized(this) {
            val list = resources ?: return false

            return list.remove(manager)
        }
    }

    override fun remove(manager: OkFaker<*>): Boolean {
        if (delete(manager)) {
            manager.cancel()
            return true
        }
        return false
    }

    override fun clear() {
        synchronized(this) {
            val list = resources
            resources = null

            list?.forEach {
                it.cancel()
            }
        }
    }

    override fun size(): Int {
        var list: List<OkFaker<*>>?
        synchronized(this) {
            list = resources
        }
        return list?.size ?: 0
    }

    override fun iterator(): Iterator<OkFaker<*>> {
        val list: List<OkFaker<*>> = resources ?: emptyList()
        return list.iterator()
    }

}