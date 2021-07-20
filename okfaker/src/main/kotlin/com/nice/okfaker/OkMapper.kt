package com.nice.okfaker

fun interface OkMapper<T, R> {

    fun map(value: T): R

}