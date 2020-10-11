package com.easy.kotlins.adapter

/**
 * Create by LiZhanPing on 2020/8/31
 */
interface MultipleItem {

    val type: Int

}

interface MultiplyItem: MultipleItem {

    val original: Any?

}