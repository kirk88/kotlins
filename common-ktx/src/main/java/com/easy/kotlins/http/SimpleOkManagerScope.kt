package com.easy.kotlins.http


/**
 * Create by LiZhanPing on 2020/8/24
 */
class SimpleOkManagerScope : OkManagerScope, Iterable<OkManager<*, *>> {
    private var resources: MutableList<OkManager<*, *>>? = null

    override fun add(manager: OkManager<*, *>) {
        synchronized(this) {
            var list = resources

            if (list == null) {
                list = arrayListOf()
                resources = list
            }

            list.add(manager)
        }
    }

    override fun delete(manager: OkManager<*, *>): Boolean {
        synchronized(this) {
            val list = resources ?: return false

            return list.remove(manager)
        }
    }

    override fun remove(manager: OkManager<*, *>): Boolean {
        if (delete(manager)) {
            manager.cancel()
            return true
        }
        return false
    }

    override fun cancelAll() {
        synchronized(this) {
            val list = resources
            resources = null

            list?.forEach {
                it.cancel()
            }
        }
    }

    override fun size(): Int {
        var list: List<OkManager<*, *>>?
        synchronized(this) {
            list = resources
        }
        return list?.size ?: 0
    }

    override fun iterator(): Iterator<OkManager<*, *>> {
        val okFakers: List<OkManager<*, *>> = resources ?: emptyList()
        return okFakers.iterator()
    }

}