package com.hao.reader.test

fun main() {

    val sa = String() + "a"
    val sb = String() + "a"
    println(sa === sb)

    val a = 0x1
    val b = 0x1 shl 1
    val c = 0x1 shl 2

    var flag = 0

    flag = flag or a
    println(flag)
    println("=============")
    println(flag and a)
    println(flag and b)
    println(flag and c)

    println("=============")
    println(flag)
    println("=============")
    flag = flag or c

    println(flag and a)
    println(flag and b)
    println(flag and c)
    println("=============")

    println(flag)
    println("=============")
    flag = flag or b

    println(flag and a)
    println(flag and b)
    println(flag and c)
}
