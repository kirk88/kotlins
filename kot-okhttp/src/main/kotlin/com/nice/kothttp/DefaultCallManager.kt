@file:Suppress("UNUSED")

package com.nice.kothttp

class DefaultCallManager : CallManager {

    private var resources: MutableSet<OkCall<*>>? = null

    override val size: Int
        get() = resources?.size ?: 0

    override fun add(call: OkCall<*>) {
        synchronized(this) {
            var list = resources

            if (list == null) {
                list = mutableSetOf()
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

    override fun deleteByTag(tag: Any): Boolean {
        val call: OkCall<*>?
        synchronized(this) {
            call = resources?.find { it.tag() === tag }
        }
        call ?: return false
        return delete(call)
    }

    override fun remove(call: OkCall<*>): Boolean {
        if (delete(call)) {
            call.cancel()
            return true
        }
        return false
    }

    override fun removeByTag(tag: Any): Boolean {
        val call: OkCall<*>?
        synchronized(this) {
            call = resources?.find { it.tag() === tag }
        }
        call ?: return false
        return remove(call)
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

    override fun iterator(): Iterator<OkCall<*>> {
        val list: Set<OkCall<*>> = resources ?: emptySet()
        return list.iterator()
    }

}