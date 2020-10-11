package com.easy.kotlins.http


/**
 * Create by LiZhanPing on 2020/8/24
 */
class SimpleNiceOkFakerScope : NiceOkFakerScope, Iterable<NiceOkFaker> {
    private var resources: MutableList<NiceOkFaker>? = null

    override fun add(tag: Any, faker: NiceOkFaker): NiceOkFaker {
        synchronized(this) {
            var list = resources

            if (list == null) {
                list = arrayListOf()
                resources = list
            }

            list.add(faker.apply { tag(tag) })
        }
        return faker
    }

    override fun removeByTag(tag: Any) {
        synchronized(this) {
            val list = resources ?: return
            val it = list.iterator()
            while (it.hasNext()) {
                val faker = it.next()
                if (tag === faker.tag) {
                    it.remove()
                    faker.cancel()
                }
            }
        }
    }

    override fun deleteByTag(tag: Any) {
        synchronized(this) {
            val list = resources ?: return
            val it = list.iterator()
            while (it.hasNext()) {
                val faker = it.next()
                if (tag === faker.tag) {
                    it.remove()
                }
            }
        }
    }

    override fun remove(faker: NiceOkFaker): Boolean {
        if (delete(faker)) {
            faker.cancel()
            return true
        }
        return false
    }

    override fun delete(faker: NiceOkFaker): Boolean {
        synchronized(this) {
            val list = resources
            if (list == null || !list.remove(faker)) {
                return false
            }
        }
        return true
    }

    override fun getByTag(tag: Any): List<NiceOkFaker> {
        synchronized(this) {
            val list = resources ?: return emptyList()
            val fakers: ArrayList<NiceOkFaker> = arrayListOf()
            list.forEach {
                if (tag === it.tag) {
                    fakers.add(it)
                }
            }
            return fakers
        }
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
        synchronized(this) {
            val list: List<NiceOkFaker>? = resources
            return list?.size ?: 0
        }
    }

    override fun iterator(): Iterator<NiceOkFaker> {
        val okFakers: List<NiceOkFaker> = resources ?: listOf()
        return okFakers.iterator()
    }

}