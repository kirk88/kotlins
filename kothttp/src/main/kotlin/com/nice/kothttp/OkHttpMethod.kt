@file:Suppress("unused")

package com.nice.kothttp

enum class OkHttpMethod {
    Get, Post, Delete, Put, Head, Patch;

    override fun toString(): String = name.uppercase()
}