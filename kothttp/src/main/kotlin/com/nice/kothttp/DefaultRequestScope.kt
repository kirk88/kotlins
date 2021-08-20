@file:Suppress("unused")

package com.nice.kothttp

class DefaultRequestScope : RequestScope {

    private var resources: MutableList<OkCall<*>>? = null

    override fun add(call: OkCall<*>) {
        synchronized(this) {
            var list = resources

            if (list == null) {
                list = mutableListOf()
                resources = list
            }

            list.add(call)
        }
    }

    override fun delete(call: OkCall<*>): Boolean {
        synchronized(this) {
            val list = resources ?: return false

            return list.remove(call)
        }
    }

    override fun remove(call: OkCall<*>): Boolean {
        if (delete(call)) {
            call.cancel()
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
        var list: List<OkCall<*>>?
        synchronized(this) {
            list = resources
        }
        return list?.size ?: 0
    }

    override fun iterator(): Iterator<OkCall<*>> {
        val list: List<OkCall<*>> = resources ?: emptyList()
        return list.iterator()
    }

}