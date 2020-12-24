package com.easy.kotlins.helper

import com.google.gson.internal.`$Gson$Types`
import java.lang.reflect.Type

/**
 * Create by LiZhanPing on 2020/10/11
 */

fun typeOfList(type: Type): Type? {
    return type(MutableList::class.java, type)
}

fun typeOfSet(type: Type): Type? {
    return type(MutableSet::class.java, type)
}

fun typeOfMap(keyType: Type, valueType: Type): Type? {
    return type(MutableMap::class.java, keyType, valueType)
}

fun typeOfArray(type: Type): Type? {
    return `$Gson$Types`.canonicalize(`$Gson$Types`.arrayOf(type))
}

fun type(rawType: Type, vararg typeArguments: Type): Type? {
    return `$Gson$Types`.canonicalize(`$Gson$Types`.newParameterizedTypeWithOwner(null, rawType, *typeArguments))
}