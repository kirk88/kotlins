package com.nice.common.http

fun interface OkMapper<T, R> {

    fun map(value: T): R

}