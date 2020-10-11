package com.easy.kotlins.helper

import java.io.Serializable

/**
 * Create by LiZhanPing on 2020/8/25
 */

inline fun <reified T> paged(page: Int, crossinline data: () -> T?): Paged<T> =
    Paged.create(data(), page)

inline fun <reified T> paged(page: Int, data: T?): Paged<T> = Paged.create(data, page)

inline fun <reified T> pagedList(page: Int, crossinline dataList: () -> List<T>?): PagedList<T> =
    PagedList.create(dataList(), page)

inline fun <reified T> pagedList(page: Int, dataList: List<T>?): PagedList<T> =
    PagedList.create(dataList, page)

fun Paged<*>.isEmpty() = this.data == null || (data is Collection<*> && data.isEmpty())

fun Paged<*>?.isNullOrEmpty() = this?.isEmpty() ?: true

fun Paged<*>.isNotEmpty() = !this.isEmpty()

open class Paged<T> internal constructor(
        val page: Int,
        val data: T?
) : Serializable {

    companion object {

        fun <T> create(data: T?, page: Int): Paged<T> = Paged(page, data)

    }

}

class PagedList<T>(page: Int, data: List<T>?) : Paged<List<T>>(page, data) {
    companion object {

        fun <T> create(data: List<T>?, page: Int): PagedList<T> = PagedList(page, data)

    }
}