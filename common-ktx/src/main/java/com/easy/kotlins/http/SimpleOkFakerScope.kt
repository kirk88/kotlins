package com.easy.kotlins.http


/**
 * Create by LiZhanPing on 2020/8/24
 */
class SimpleOkFakerScope : OkFakerScope, Iterable<OkFaker> {
    private var resources: MutableList<OkFaker>? = null

    override fun addRequest(faker: OkFaker) {
        synchronized(this) {
            var list = resources

            if (list == null) {
                list = arrayListOf()
                resources = list
            }

            list.add(faker)
        }
    }

    override fun cancelByTag(tag: Any) {
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

    override fun cancel(faker: OkFaker): Boolean {
        synchronized(this) {
            val list = resources
            if (list != null && list.remove(faker)) {
                faker.cancel()
                return true
            }
            return false
        }
    }

    override fun cancelAll(){
        synchronized(this) {
            val list = resources
            resources = null

            list?.forEach {
                it.cancel()
            }
        }
    }

    override fun count(): Int {
        synchronized(this) {
            val list: List<OkFaker>? = resources
            return list?.size ?: 0
        }
    }

    override fun iterator(): Iterator<OkFaker> {
        val okFakers: List<OkFaker> = resources ?: listOf()
        return okFakers.iterator()
    }

}