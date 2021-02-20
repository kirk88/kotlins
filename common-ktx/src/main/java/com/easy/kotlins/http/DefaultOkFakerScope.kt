package com.easy.kotlins.http

class DefaultOkFakerScope : OkFakerScope {

    private var resources: MutableList<OkFaker<*>>? = null

    override fun add(manager: OkFaker<*>) {
        synchronized(this) {
            var list = resources

            if (list == null) {
                list = mutableListOf()
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