package com.easy.kotlins.helper

import com.google.gson.internal.`$Gson$Types`
import java.lang.reflect.Type

/**
 * Create by LiZhanPing on 2020/10/11
 */

fun listType(type: Type): Type? {
    return type(MutableList::class.java, type)
}

fun setType(type: Type): Type? {
    return type(MutableSet::class.java, type)
}

fun mapType(keyType: Type, valueType: Type): Type? {
    return type(MutableMap::class.java, keyType, valueType)
}

fun type(rawType: Type, vararg typeArguments: Type): Type? {
    return `$Gson$Types`.canonicalize(`$Gson$Types`.newParameterizedTypeWithOwner(null, rawType, *typeArguments))
}

fun arrayType(type: Type): Type? {
    return `$Gson$Types`.canonicalize(`$Gson$Types`.arrayOf(type))
}