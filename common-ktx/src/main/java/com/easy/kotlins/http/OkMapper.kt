package com.easy.kotlins.http

fun interface OkMapper<T, R> {

    fun map(value: T): R

}