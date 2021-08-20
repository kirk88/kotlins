@file:Suppress("unused")

package com.nice.kothttp

class DefaultRequestScope : RequestScope {

    private var resources: MutableList<OkRequest<*>>? = null

    override fun add(request: OkRequest<*>) {
        synchronized(this) {
            var list = resources

            if (list == null) {
                list = mutableListOf()
                resources = list
            }

            list.add(request)
        }
    }

    override fun delete(request: OkRequest<*>): Boolean {
        synchronized(this) {
            val list = resources ?: return false

            return list.remove(request)
        }
    }

    override fun remove(request: OkRequest<*>): Boolean {
        if (delete(request)) {
            request.cancel()
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
        var list: List<OkRequest<*>>?
        synchronized(this) {
            list = resources
        }
        return list?.size ?: 0
    }

    override fun iterator(): Iterator<OkRequest<*>> {
        val list: List<OkRequest<*>> = resources ?: emptyList()
        return list.iterator()
    }

}