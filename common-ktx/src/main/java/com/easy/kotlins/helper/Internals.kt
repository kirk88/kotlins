package com.easy.kotlins.helper

internal object Internals {

    const val NO_GETTER_MESSAGE: String = "NO_GETTER"

    val NO_GETTER: Nothing = error("No getter")

}