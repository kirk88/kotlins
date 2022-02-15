@file:Suppress("UNUSED")

package com.nice.kothttp

enum class RequestMethod {
    Get, Post, Delete, Put, Head, Patch;

    override fun toString(): String = name.uppercase()
}