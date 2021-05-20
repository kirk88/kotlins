package com.example.sample.page

import android.graphics.Rect
import androidx.collection.LruCache

object PdfPageCache {

    private val lruCache = LruCache<Int, PdfPage>(9)

    operator fun get(index: Int, bounds: Rect): PdfPage? {
        return lruCache[index.hashCode() + bounds.hashCode()]
    }

    operator fun set(index: Int, bounds: Rect, page: PdfPage) {
        lruCache.put(index.hashCode() + bounds.hashCode(), page)
    }

    fun clear() {
        for (page in lruCache.snapshot().values) {
            page.recycle()
        }
        lruCache.evictAll()
    }

}